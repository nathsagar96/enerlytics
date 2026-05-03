package com.enerlytics.usages.services;

import com.enerlytics.events.AlertingEvent;
import com.enerlytics.events.EnergyUsageEvent;
import com.enerlytics.usages.clients.DeviceClient;
import com.enerlytics.usages.clients.UserClient;
import com.enerlytics.usages.dtos.DeviceEnergy;
import com.enerlytics.usages.dtos.DeviceResponse;
import com.enerlytics.usages.dtos.UserResponse;
import com.enerlytics.usages.dtos.responses.UsageResponse;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsageService {

    private final InfluxDBClient dbClient;
    private final DeviceClient deviceClient;
    private final UserClient userClient;
    private final KafkaTemplate<String, AlertingEvent> template;

    @Value("${influx.bucket}")
    private String dbBucket;

    @Value("${influx.org}")
    private String dbOrg;

    @KafkaListener(topics = "energy-usage", groupId = "usage-service")
    public void listen(EnergyUsageEvent event) {
        log.info("Received event: {}", event);

        Point point = Point.measurement("energy_usage")
                .addTag("deviceId", event.deviceId().toString())
                .addField("energyConsumed", event.energyConsumed())
                .time(event.timestamp(), WritePrecision.MS);

        dbClient.getWriteApiBlocking().writePoint(dbBucket, dbOrg, point);
    }

    @Scheduled(cron = "*/10 * * * * *")
    public void aggregateDeviceEnergyUsage() {
        final Instant now = Instant.now();
        final Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);

        String fluxQuery = String.format("""
                from(bucket: "%s")
                  |> range(start: time(v: "%s"), stop: time(v: "%s"))
                  |> filter(fn: (r) => r["_measurement"] == "energy_usage")
                  |> filter(fn: (r) => r["_field"] == "energyConsumed")
                  |> group(columns: ["deviceId"])
                  |> sum(column: "_value")
                """, dbBucket, oneHourAgo.toString(), now);

        QueryApi queryApi = dbClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery, dbOrg);

        List<DeviceEnergy> deviceEnergies = new ArrayList<>();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                String deviceIdStr = (String) record.getValueByKey("deviceId");
                Double energyConsumed = record.getValueByKey("_value") instanceof Number
                        ? ((Number) record.getValueByKey("_value")).doubleValue()
                        : 0.0;

                deviceEnergies.add(DeviceEnergy.builder()
                        .deviceId(Long.valueOf(deviceIdStr))
                        .energyConsumed(energyConsumed)
                        .build());
            }
        }
        log.info("Aggregated device energies over the past hour: {}", deviceEnergies);

        for (DeviceEnergy deviceEnergy : deviceEnergies) {
            try {
                final DeviceResponse deviceResponse = deviceClient.getDeviceById(deviceEnergy.getDeviceId());

                if (deviceResponse == null || deviceResponse.id() == null) {
                    log.warn("DeviceResponse not found for ID: {}", deviceEnergy.getDeviceId());
                    continue;
                }
                deviceEnergy.setUserId(deviceResponse.userId());
            } catch (Exception e) {
                log.warn("Failed to fetch device for ID: {}", deviceEnergy.getDeviceId());
            }
        }

        // remove devices with null userId
        deviceEnergies.removeIf(de -> de.getUserId() == null);

        // Get user-device mapping and aggregate per user
        Map<Long, List<DeviceEnergy>> userDeviceEnergyMap =
                deviceEnergies.stream().collect(Collectors.groupingBy(DeviceEnergy::getUserId));

        log.info("User-DeviceResponse Energy Map: {}", userDeviceEnergyMap);

        // get users energy consumption thresholds
        List<Long> userIds = new ArrayList<>(userDeviceEnergyMap.keySet());
        final Map<Long, Double> userThresholdMap = new HashMap<>();
        final Map<Long, String> userEmailMap = new HashMap<>();

        for (final Long userId : userIds) {
            try {
                UserResponse user = userClient.getUserById(userId);
                if (user == null || user.id() == null || !user.alerting()) {
                    log.warn("User not found or alerting disabled for ID: {}", userId);
                    continue;
                }
                userThresholdMap.put(userId, user.energyAlertingThreshold());
                userEmailMap.put(userId, user.email());
            } catch (Exception e) {
                log.warn("Failed to fetch user for ID: {}", userId);
            }
        }
        log.info("User Threshold Map: {}", userThresholdMap);

        // Check thresholds against aggregated usage
        final List<Long> alertedUsers = new ArrayList<>(userThresholdMap.keySet());
        for (final Long userId : alertedUsers) {
            final Double threshold = userThresholdMap.get(userId);
            final List<DeviceEnergy> devices = userDeviceEnergyMap.get(userId);

            final Double totalConsumption = devices.stream()
                    .mapToDouble(DeviceEnergy::getEnergyConsumed)
                    .sum();

            if (totalConsumption > threshold) {
                log.info(
                        "ALERT: User ID {} has exceeded the energy threshold! "
                                + "Total Consumption: {}, Threshold: {}",
                        userId,
                        totalConsumption,
                        threshold);
                // Put message on kafka alert-topic
                final AlertingEvent alertingEvent = new AlertingEvent(
                        userId,
                        "Energy consumption threshold exceeded",
                        threshold,
                        totalConsumption,
                        userEmailMap.get(userId));
                template.send("energy-alerts", alertingEvent);
            } else {
                log.info(
                        "User ID {} is within the energy threshold. " + "Total Consumption: {}, Threshold: {}",
                        userId,
                        totalConsumption,
                        threshold);
            }
        }
    }

    public UsageResponse getXDaysUsageForUser(Long userId, int days) {
        log.info("Getting usage for userId {} over past {} days", userId, days);
        List<DeviceResponse> devices = deviceClient.getAllDevicesForUser(userId);

        if (devices == null || devices.isEmpty()) {
            return new UsageResponse(userId, devices);
        }

        List<String> deviceIdStrings = devices.stream()
                .map(DeviceResponse::id)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toList();

        Instant now = Instant.now();
        Instant start = now.minus(days, ChronoUnit.DAYS);

        final String deviceFilter = deviceIdStrings.stream()
                .map(idStr -> String.format("r[\"deviceId\"] == \"%s\"", idStr))
                .collect(Collectors.joining(" or "));

        String fluxQuery = String.format("""
                from(bucket: "%s")
                  |> range(start: time(v: "%s"), stop: time(v: "%s"))
                  |> filter(fn: (r) => r["_measurement"] == "energy_usage")
                  |> filter(fn: (r) => r["_field"] == "energyConsumed")
                  |> filter(fn: (r) => %s)
                  |> group(columns: ["deviceId"])
                  |> sum(column: "_value")
                """, dbBucket, start.toString(), now, deviceFilter);

        final Map<Long, Double> aggregatedMap = new HashMap<>();

        try {
            QueryApi queryApi = dbClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(fluxQuery, dbOrg);

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    Object deviceIdObj = record.getValueByKey("deviceId");
                    String deviceIdStr = deviceIdObj == null ? null : deviceIdObj.toString();
                    if (deviceIdStr == null) continue;

                    Double energyConsumed = record.getValueByKey("_value") instanceof Number
                            ? ((Number) record.getValueByKey("_value")).doubleValue()
                            : 0.0;

                    try {
                        Long deviceId = Long.valueOf(deviceIdStr);
                        aggregatedMap.put(deviceId, aggregatedMap.getOrDefault(deviceId, 0.0) + energyConsumed);
                    } catch (NumberFormatException nfe) {
                        log.warn("Failed to parse deviceId from flux record: {}", deviceIdStr);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to query InfluxDB for user {} usage over {} days: {}", userId, days, e.getMessage());
            // set aggregatedConsumption to 0.0 on error
            devices.forEach(d -> d.withEnergyConsumed(0.0));
            return new UsageResponse(userId, null);
        }

        // populate aggregated energy consumed per device
        for (DeviceResponse device : devices) {
            if (device == null || device.id() == null) continue;
            device.withEnergyConsumed(aggregatedMap.getOrDefault(device.id(), 0.0));
        }

        log.info("Aggregated energy consumption for userId {}: {}", userId, aggregatedMap);

        List<DeviceResponse> resultDevices = devices.stream()
                .map(d -> new DeviceResponse(
                        d.id(), d.name(), d.deviceType(), d.location(), d.userId(), d.energyConsumed()))
                .toList();

        return new UsageResponse(userId, resultDevices);
    }
}
