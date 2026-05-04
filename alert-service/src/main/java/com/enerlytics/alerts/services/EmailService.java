package com.enerlytics.alerts.services;

import com.enerlytics.alerts.entities.Alert;
import com.enerlytics.alerts.repositories.AlertRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final AlertRepository alertRepository;

    public void sendMail(String recipientEmail, String subject, String body, Long userId) {
        log.info("Attempting to send email to user {}: {}", userId, recipientEmail);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setFrom("noreply@enerlytics.com");
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            log.info("Email successfully sent to {}", recipientEmail);

            Alert alert = Alert.builder()
                    .userId(userId)
                    .sent(true)
                    .createdAt(Instant.now())
                    .build();
            alertRepository.saveAndFlush(alert);
        } catch (MailException e) {
            log.error("Failed to send email to {}. Error: {}", recipientEmail, e.getMessage());

            Alert alert = Alert.builder()
                    .userId(userId)
                    .sent(false)
                    .createdAt(Instant.now())
                    .build();
            alertRepository.saveAndFlush(alert);
        }
    }
}
