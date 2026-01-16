package com.enerlytics.usages.services;

import com.enerlytics.AlertingEvent;
import com.enerlytics.EnergyUsageEvent;
import com.enerlytics.usages.clients.DeviceClient;
import com.enerlytics.usages.clients.UserClient;
import com.enerlytics.usages.configs.InfluxDBProperties;
import com.enerlytics.usages.dtos.responses.DeviceResponse;
import com.enerlytics.usages.dtos.responses.UserResponse;
import com.enerlytics.usages.entities.DeviceEnergyUsage;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UsageService {

    private final String influxBucket;
    private final String influxOrg;
    private final InfluxDBClient influxDBClient;
    private final DeviceClient deviceClient;
    private final UserClient userClient;
    private final KafkaTemplate<String, AlertingEvent> kafkaTemplate;

    public UsageService(
            InfluxDBProperties influxDBProperties,
            InfluxDBClient influxDBClient,
            DeviceClient deviceClient,
            UserClient userClient,
            KafkaTemplate<String, AlertingEvent> kafkaTemplate) {
        this.influxBucket = influxDBProperties.bucket();
        this.influxOrg = influxDBProperties.org();
        this.influxDBClient = influxDBClient;
        this.deviceClient = deviceClient;
        this.userClient = userClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "energy-usage", groupId = "usage-service")
    public void handleEnergyUsageEvent(EnergyUsageEvent event) {
        try {
            log.debug("Received energy-usage event: {}", event);
            Point point = Point.measurement("energy_usage")
                    .addTag("deviceId", event.deviceId().toString())
                    .addField("energyConsumed", event.energyConsumed())
                    .time(event.timestamp(), WritePrecision.MS);

            influxDBClient.getWriteApiBlocking().writePoint(influxBucket, influxOrg, point);
            log.debug("Successfully wrote energy usage data to InfluxDB for device: {}", event.deviceId());
        } catch (Exception e) {
            log.error("Failed to process energy usage event for device {}: {}", event.deviceId(), e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void checkAndAlertEnergyThresholds() {
        log.info("Starting energy threshold check");

        List<DeviceEnergyUsage> deviceEnergies = queryDeviceEnergyUsage();
        if (deviceEnergies.isEmpty()) {
            log.warn("No device energy usage found for the last hour");
            return;
        }

        enrichDevicesWithUserInfo(deviceEnergies);
        Map<UUID, Double> userConsumption = aggregateEnergyByUser(deviceEnergies);
        sendAlertsForThresholdViolations(userConsumption);
    }

    private List<DeviceEnergyUsage> queryDeviceEnergyUsage() {
        final Instant now = Instant.now();
        final Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);

        String fluxQuery = String.format(
                """
                        from(bucket: "%s")
                        |> range(start: time(v: "%s"), stop: time(v: "%s"))
                        |> filter(fn: (r) => r.["_measurement"] == "energy_usage")
                        |> filter(fn: (r) => r.["_field"] == "energyConsumed")
                        |> group(columns: ["deviceId"])
                        |> sum(column: "_value")
                        """,
                influxBucket, oneHourAgo.toString(), now);

        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> queryResults = queryApi.query(fluxQuery, influxOrg);

        List<DeviceEnergyUsage> deviceEnergies = new ArrayList<>();
        for (FluxTable table : queryResults) {
            for (FluxRecord record : table.getRecords()) {
                UUID deviceId =
                        UUID.fromString(record.getValues().get("deviceId").toString());
                Double energyConsumed = Optional.ofNullable(record.getValueByKey("_value"))
                        .filter(Number.class::isInstance)
                        .map(Number.class::cast)
                        .map(Number::doubleValue)
                        .orElse(0.0);
                deviceEnergies.add(DeviceEnergyUsage.builder()
                        .deviceId(deviceId)
                        .energyConsumed(energyConsumed)
                        .build());
            }
        }
        log.debug("Queried device energy usage: {}", deviceEnergies);
        return deviceEnergies;
    }

    private void enrichDevicesWithUserInfo(List<DeviceEnergyUsage> deviceEnergies) {
        Set<UUID> deviceIds =
                deviceEnergies.stream().map(DeviceEnergyUsage::getDeviceId).collect(Collectors.toSet());

        CompletableFuture<Map<UUID, DeviceResponse>> deviceFuture = fetchDevicesAsync(deviceIds);

        Map<UUID, DeviceResponse> deviceMap = deviceFuture.join();

        for (DeviceEnergyUsage deviceEnergyUsage : deviceEnergies) {
            DeviceResponse deviceResponse = deviceMap.get(deviceEnergyUsage.getDeviceId());
            if (deviceResponse != null) {
                deviceEnergyUsage.setUserId(deviceResponse.userId());
            } else {
                log.warn("Device info not found for deviceId: {}", deviceEnergyUsage.getDeviceId());
            }
        }

        deviceEnergies.removeIf(deviceEnergyUsage -> deviceEnergyUsage.getUserId() == null);
        log.debug("Enriched device energy usage: {}", deviceEnergies);
    }

    private Map<UUID, Double> aggregateEnergyByUser(List<DeviceEnergyUsage> deviceEnergies) {
        return deviceEnergies.stream()
                .collect(Collectors.groupingBy(
                        DeviceEnergyUsage::getUserId, Collectors.summingDouble(DeviceEnergyUsage::getEnergyConsumed)));
    }

    private void sendAlertsForThresholdViolations(Map<UUID, Double> userConsumption) {
        Set<UUID> userIds = userConsumption.keySet();
        CompletableFuture<Map<UUID, UserResponse>> userFuture = fetchUsersAsync(userIds);

        Map<UUID, UserResponse> userMap = userFuture.join();

        for (Map.Entry<UUID, Double> entry : userConsumption.entrySet()) {
            UUID userId = entry.getKey();
            Double totalConsumption = entry.getValue();
            UserResponse userResponse = userMap.get(userId);

            if (userResponse == null || !userResponse.alerting()) {
                log.warn("User {} not found or alerting disabled", userId);
                continue;
            }

            Double threshold = userResponse.energyAlertingThreshold();
            if (totalConsumption > threshold) {
                log.warn(
                        "ALERT: User ID: {} has exceeded threshold: {}, Total Consumption: {}",
                        userId,
                        threshold,
                        totalConsumption);

                AlertingEvent alertingEvent = AlertingEvent.builder()
                        .userId(userId)
                        .message("Energy consumption threshold exceeded")
                        .threshold(threshold)
                        .energyConsumed(totalConsumption)
                        .email(userResponse.email())
                        .build();
                kafkaTemplate.send("energy-alerts", alertingEvent);
            } else {
                log.debug(
                        "User ID: {} is within threshold: {}. Total Consumption: {}",
                        userId,
                        threshold,
                        totalConsumption);
            }
        }
    }

    @Async("taskExecutor")
    public CompletableFuture<Map<UUID, DeviceResponse>> fetchDevicesAsync(Set<UUID> deviceIds) {
        return CompletableFuture.completedFuture(deviceClient.getDevicesByIds(deviceIds));
    }

    @Async("taskExecutor")
    public CompletableFuture<Map<UUID, UserResponse>> fetchUsersAsync(Set<UUID> userIds) {
        return CompletableFuture.completedFuture(userClient.getUsersByIds(userIds));
    }
}
