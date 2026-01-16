package com.enerlytics.usages.clients;

import com.enerlytics.usages.dtos.responses.UserResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class UserClient {

    private final String baseUrl;
    private final RestTemplate restTemplate;

    public UserClient(@Value("${user-service.base-url}") String baseUrl, RestTemplate restTemplate) {
        this.baseUrl = baseUrl;
        this.restTemplate = restTemplate;
    }

    public Map<UUID, UserResponse> getUsersByIds(Set<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            String url = UriComponentsBuilder.fromUriString(baseUrl)
                    .path("/batch")
                    .queryParam("ids", userIds.stream().map(UUID::toString).collect(Collectors.joining(",")))
                    .toUriString();

            ResponseEntity<List<UserResponse>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<List<UserResponse>>() {});

            return Optional.ofNullable(response.getBody()).orElse(Collections.emptyList()).stream()
                    .collect(Collectors.toMap(UserResponse::id, Function.identity()));
        } catch (RestClientException e) {
            log.error("Failed to fetch users in batch: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
