package com.enerlytics.ingestions.services;

import com.enerlytics.events.EnergyUsageEvent;
import com.enerlytics.ingestions.dtos.requests.IngestionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final KafkaTemplate<String, EnergyUsageEvent> kafkaTemplate;

    public void ingestData(IngestionRequest request) {
        log.info("Ingesting energy usage data for device: {}", request.deviceId());
        EnergyUsageEvent event =
                new EnergyUsageEvent(request.deviceId(), request.energyConsumed(), request.timestamp());

        kafkaTemplate.send("energy-usage", event);
        log.debug("Published energy usage event to Kafka: {}", event);
    }
}
