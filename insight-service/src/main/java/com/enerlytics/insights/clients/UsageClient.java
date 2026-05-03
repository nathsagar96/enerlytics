package com.enerlytics.insights.clients;

import com.enerlytics.insights.dtos.UsageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsageClient {
    private final RestTemplate restTemplate;

    @Value("${usage-service.base-url}")
    private String baseUrl;

    public UsageResponse getXDaysUsageForUser(Long userId, int days) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/{userId}")
                .queryParam("days", days)
                .buildAndExpand(userId)
                .toUriString();

        log.info("Calling endpoint: {}", url);

        ResponseEntity<UsageResponse> response = restTemplate.getForEntity(url, UsageResponse.class);
        return response.getBody();
    }
}
