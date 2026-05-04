package com.enerlytics.ingestions.utils;

import com.enerlytics.ingestions.dtos.requests.IngestionRequest;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class DataSimulator implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();
    private ExecutorService executorService;

    @Value("${simulation.parallel-threads}")
    private int parallelThreads;

    @Value("${simulation.requests-per-interval}")
    private int requestsPerInterval;

    @Value("${simulation.ingestion-endpoint}")
    private String ingestionEndpoint;

    @Override
    public void run(String @NonNull ... args) {
        log.info(
                "Starting data simulation with {} threads and {} requests per interval",
                parallelThreads,
                requestsPerInterval);
        executorService = Executors.newFixedThreadPool(parallelThreads);
    }

    @Scheduled(fixedRateString = "${simulation.interval-ms}")
    public void sendMockData() {
        if (executorService == null) {
            return;
        }

        log.info("Simulating batch of {} requests...", requestsPerInterval);
        int batchSize = requestsPerInterval / parallelThreads;
        int remainder = requestsPerInterval % parallelThreads;

        for (int i = 0; i < parallelThreads; i++) {
            int requestsForThread = batchSize + (i < remainder ? 1 : 0);
            executorService.submit(() -> {
                for (int j = 0; j < requestsForThread; j++) {
                    IngestionRequest request = new IngestionRequest(
                            random.nextLong(1, 20),
                            Math.round(random.nextDouble(0.0, 2.0) * 100.0) / 100.0,
                            Instant.now());
                    try {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        HttpEntity<IngestionRequest> entity = new HttpEntity<>(request, headers);
                        restTemplate.postForEntity(ingestionEndpoint, entity, Void.class);
                        log.debug("Sent simulation request: {}", request);
                    } catch (Exception e) {
                        log.error("Error sending simulation request: {}", e.getMessage());
                    }
                }
            });
        }
    }

    @PreDestroy
    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
            log.info("Shutting down data simulator executor");
        }
    }
}
