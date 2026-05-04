package com.enerlytics.usages.services;

import com.enerlytics.events.AlertingEvent;
import com.enerlytics.events.EnergyUsageEvent;
import com.enerlytics.usages.clients.DeviceClient;
import com.enerlytics.usages.clients.UserClient;
import com.enerlytics.usages.dtos.DeviceEnergy;
import com.enerlytics.usages.dtos.external.DeviceServiceResponse;
import com.enerlytics.usages.dtos.external.UserServiceResponse;
import com.enerlytics.usages.dtos.responses.DeviceUsageResponse;
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

    private final InfluxDBClient influxDBClient;
    private final DeviceClient deviceClient;
    private final UserClient userClient;
    private final KafkaTemplate<String, AlertingEvent> kafkaTemplate;

    @Value("${influx.bucket}")
    private String dbBucket;

    @Value("${influx.org}")
    private String dbOrg;

    @KafkaListener(topics = "energy-usage", groupId = "usage-service")
    public void processEnergyUsage(EnergyUsageEvent event) {
        log.debug("Processing energy usage event for device: {}", event.deviceId());

        Point point = Point.measurement("energy_usage")
                .addTag("deviceId", event.deviceId().toString())
                .addField("energyConsumed", event.energyConsumed())
                .time(event.timestamp(), WritePrecision.MS);

        try {
            influxDBClient.getWriteApiBlocking().writePoint(dbBucket, dbOrg, point);
            log.trace("Successfully recorded energy usage for device: {}", event.deviceId());
        } catch (Exception e) {
            log.error("Failed to write energy usage to InfluxDB for device {}: {}", event.deviceId(), e.getMessage());
        }
    }

    @Scheduled(cron = "*/10 * * * * *")
    public void aggregateAndCheckThresholds() {
        log.info("Starting scheduled energy usage aggregation and threshold check");
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

        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables;
        try {
            tables = queryApi.query(fluxQuery, dbOrg);
        } catch (Exception e) {
            log.error("Failed to query InfluxDB for aggregation: {}", e.getMessage());
            return;
        }

        List<DeviceEnergy> deviceEnergies = new ArrayList<>();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                String deviceIdStr = (String) record.getValueByKey("deviceId");
                Double energyConsumed = record.getValueByKey("_value") instanceof Number
                        ? ((Number) record.getValueByKey("_value")).doubleValue()
                        : 0.0;

                if (deviceIdStr != null) {
                    deviceEnergies.add(DeviceEnergy.builder()
                            .deviceId(Long.valueOf(deviceIdStr))
                            .energyConsumed(energyConsumed)
                            .build());
                }
            }
        }

        if (deviceEnergies.isEmpty()) {
            log.info("No energy usage data found for the last hour");
            return;
        }

        log.info("Aggregated usage for {} devices", deviceEnergies.size());

        for (DeviceEnergy deviceEnergy : deviceEnergies) {
            try {
                final DeviceServiceResponse deviceDetails = deviceClient.getDeviceById(deviceEnergy.getDeviceId());

                if (deviceDetails == null || deviceDetails.id() == null) {
                    log.warn("Device details not found for ID: {}", deviceEnergy.getDeviceId());
                    continue;
                }
                deviceEnergy.setUserId(deviceDetails.userId());
            } catch (Exception e) {
                log.warn("Failed to fetch device details for ID {}: {}", deviceEnergy.getDeviceId(), e.getMessage());
            }
        }

        // remove devices with null userId
        deviceEnergies.removeIf(deviceEnergy -> deviceEnergy.getUserId() == null);

        // Get user-device mapping and aggregate per user
        Map<Long, List<DeviceEnergy>> userDeviceEnergyMap =
                deviceEnergies.stream().collect(Collectors.groupingBy(DeviceEnergy::getUserId));

        log.debug("Grouped usage by {} users", userDeviceEnergyMap.size());

        // get users energy consumption thresholds
        List<Long> userIds = new ArrayList<>(userDeviceEnergyMap.keySet());
        final Map<Long, Double> userThresholdMap = new HashMap<>();
        final Map<Long, String> userEmailMap = new HashMap<>();

        for (final Long userId : userIds) {
            try {
                final UserServiceResponse userDetails = userClient.getUserById(userId);
                if (userDetails == null || userDetails.id() == null) {
                    log.warn("User details not found for ID: {}", userId);
                    continue;
                }

                if (!Boolean.TRUE.equals(userDetails.alerting())) {
                    log.debug("Alerting is disabled for user ID: {}", userId);
                    continue;
                }

                userThresholdMap.put(userId, userDetails.energyAlertingThreshold());
                userEmailMap.put(userId, userDetails.email());
            } catch (Exception e) {
                log.warn("Failed to fetch user preferences for ID {}: {}", userId, e.getMessage());
            }
        }

        // Check thresholds against aggregated usage
        for (final Long userId : userThresholdMap.keySet()) {
            final Double threshold = userThresholdMap.get(userId);
            final List<DeviceEnergy> devices = userDeviceEnergyMap.get(userId);

            final Double totalConsumption = devices.stream()
                    .mapToDouble(DeviceEnergy::getEnergyConsumed)
                    .sum();

            if (totalConsumption > threshold) {
                log.info(
                        "THRESHOLD EXCEEDED: User {} consumed {} units (Threshold: {})",
                        userId,
                        totalConsumption,
                        threshold);

                final AlertingEvent alertingEvent = new AlertingEvent(
                        userId,
                        "Energy consumption threshold exceeded",
                        threshold,
                        totalConsumption,
                        userEmailMap.get(userId));

                try {
                    kafkaTemplate.send("energy-alerts", alertingEvent);
                    log.info("Sent alerting event to Kafka for user: {}", userId);
                } catch (Exception e) {
                    log.error("Failed to send alerting event to Kafka for user {}: {}", userId, e.getMessage());
                }
            } else {
                log.debug("User {} within threshold: {}/{}", userId, totalConsumption, threshold);
            }
        }
        log.info("Finished energy usage aggregation and threshold check");
    }

    public UsageResponse getUsageForUserOverDays(Long userId, int days) {
        log.info("Fetching energy usage for user {} over the past {} days", userId, days);

        List<DeviceServiceResponse> deviceServiceResponses;
        try {
            deviceServiceResponses = deviceClient.getAllDevicesForUser(userId);
        } catch (Exception e) {
            log.error("Failed to fetch devices for user {}: {}", userId, e.getMessage());
            return new UsageResponse(userId, List.of());
        }

        if (deviceServiceResponses == null || deviceServiceResponses.isEmpty()) {
            log.info("No devices found for user {}", userId);
            return new UsageResponse(userId, List.of());
        }

        List<String> deviceIdStrings = deviceServiceResponses.stream()
                .map(DeviceServiceResponse::id)
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

        final Map<Long, Double> usageByDeviceMap = new HashMap<>();

        try {
            QueryApi queryApi = influxDBClient.getQueryApi();
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
                        usageByDeviceMap.put(deviceId, usageByDeviceMap.getOrDefault(deviceId, 0.0) + energyConsumed);
                    } catch (NumberFormatException nfe) {
                        log.warn("Failed to parse deviceId from record: {}", deviceIdStr);
                    }
                }
            }
        } catch (Exception e) {
            log.error("InfluxDB query failed for user {} over {} days: {}", userId, days, e.getMessage());
            // Return empty usage for all devices as fallback
            final List<DeviceUsageResponse> fallbackResponses = deviceServiceResponses.stream()
                    .map(device -> new DeviceUsageResponse(
                            device.id(), device.name(), device.deviceType(), device.location(), device.userId(), 0.0))
                    .toList();
            return new UsageResponse(userId, fallbackResponses);
        }

        log.debug("Aggregated energy consumption for user {}: {}", userId, usageByDeviceMap);

        List<DeviceUsageResponse> usageDevices = deviceServiceResponses.stream()
                .filter(Objects::nonNull)
                .map(device -> new DeviceUsageResponse(
                        device.id(),
                        device.name(),
                        device.deviceType(),
                        device.location(),
                        device.userId(),
                        device.id() == null ? 0.0 : usageByDeviceMap.getOrDefault(device.id(), 0.0)))
                .toList();

        return new UsageResponse(userId, usageDevices);
    }
}
