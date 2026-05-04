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

    private final EmailService emailService;

    @KafkaListener(topics = "energy-alerts", groupId = "alert-service")
    public void listen(AlertingEvent event) {
        log.info("Processing alerting event for user: {}", event.userId());
        log.debug("Full event data: {}", event);

        String subject = "Energy Usage Alert for User " + event.userId();
        String message = "Alert: "
                + event.message()
                + "\nThreshold: "
                + event.threshold()
                + "\nEnergy Consumed: "
                + event.energyConsumed();

        try {
            emailService.sendMail(event.email(), subject, message, event.userId());
            log.info("Successfully processed alert for user: {}", event.userId());
        } catch (Exception e) {
            log.error("Failed to process alert for user: {}. Error: {}", event.userId(), e.getMessage());
            throw e;
        }
    }
}
