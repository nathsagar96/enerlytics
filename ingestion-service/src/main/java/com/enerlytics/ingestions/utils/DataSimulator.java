package com.enerlytics.ingestions.utils;

import com.enerlytics.ingestions.dtos.requests.IngestionRequest;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
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

    private final RestTemplate template = new RestTemplate();
    private final Random random = new Random();
    private final ExecutorService service = Executors.newCachedThreadPool();

    @Value("${simulation.parallel-threads}")
    private int parallelThreads;

    @Value("${simulation.requests-per-interval}")
    private int requestsPerInterval;

    @Value("${simulation.ingestion-endpoint}")
    private String ingestionEndpoint;

    @Override
    public void run(String @NonNull ... args) {
        log.info("Starting data simulation...");
        ((ThreadPoolExecutor) service).setCorePoolSize(parallelThreads);
    }

    @Scheduled(fixedRateString = "${simulation.interval-ms}")
    public void sendMockData() {
        int batchSize = requestsPerInterval / parallelThreads;
        int remainder = requestsPerInterval % parallelThreads;

        for (int i = 0; i < parallelThreads; i++) {
            int requestsForThread = batchSize + (i < remainder ? 1 : 0);
            service.submit(() -> {
                for (int j = 0; j < requestsForThread; j++) {
                    IngestionRequest request = new IngestionRequest(
                            random.nextLong(1, 200),
                            Math.round(random.nextDouble(0.0, 2.0) * 100.0) / 100.0,
                            Instant.now());
                    try {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        HttpEntity<IngestionRequest> entity = new HttpEntity<>(request, headers);
                        template.postForEntity(ingestionEndpoint, entity, Void.class);
                        log.info("Sent request: {}", request);
                    } catch (Exception e) {
                        log.error("Error sending request: {}", e.getMessage());
                    }
                }
            });
        }
    }

    @PreDestroy
    public void shutdown() {
        service.shutdown();
        log.info("Shutting down data simulator");
    }
}
