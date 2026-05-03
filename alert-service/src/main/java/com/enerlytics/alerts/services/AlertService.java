package com.enerlytics.alerts.services;

import com.enerlytics.events.AlertingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final EmailService mailService;

    @KafkaListener(topics = "energy-alerts", groupId = "alert-service")
    public void listen(AlertingEvent event) {
        log.info("Received alert event: {}", event);

        String subject = "Energy Usage Alert for User " + event.userId();
        String message = "Alert: " + event.message() + "\nThreshold: " + event.threshold() + "\nEnergy Consumed: "
                + event.energyConsumed();

        mailService.sendMail(event.email(), subject, message, event.userId());
    }
}
