package com.enerlytics.ingestions.services;

import com.enerlytics.events.EnergyUsageEvent;
import com.enerlytics.ingestions.dtos.requests.IngestionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IngestionService {

    private final KafkaTemplate<String, EnergyUsageEvent> template;

    public void ingestDate(IngestionRequest request) {
        EnergyUsageEvent event =
                new EnergyUsageEvent(request.deviceId(), request.energyConsumed(), request.timestamp());

        template.send("energy-usage", event);
    }
}
