package com.enerlytics.ingestions.services;

import com.enerlytics.ingestions.dtos.requests.EnergyUsageRequest;
import com.enerlytics.ingestions.events.EnergyUsageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final KafkaTemplate<String, EnergyUsageEvent> template;

    public void ingestEnergyUsage(EnergyUsageRequest request) {
        EnergyUsageEvent event =
                new EnergyUsageEvent(request.deviceId(), request.energyConsumed(), request.timestamp());

        template.send("energy-usage", event);
        log.info("Successfully ingested energy usage for device: {}", request.deviceId());
    }
}
