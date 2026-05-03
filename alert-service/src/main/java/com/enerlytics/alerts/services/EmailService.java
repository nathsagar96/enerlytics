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
    private final AlertRepository repository;

    public void sendMail(String to, String subject, String body, Long userId) {
        log.info("Sending email to: {}, subject:{}", to, subject);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom("noreply@enerlytics.com");
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);

            Alert alert = Alert.builder()
                    .userId(userId)
                    .sent(true)
                    .createdAt(Instant.now())
                    .build();
            repository.saveAndFlush(alert);
        } catch (MailException e) {
            log.error("Failed to send Email to : {}", to);

            Alert alert = Alert.builder()
                    .userId(userId)
                    .sent(false)
                    .createdAt(Instant.now())
                    .build();
            repository.saveAndFlush(alert);
        }
    }
}
