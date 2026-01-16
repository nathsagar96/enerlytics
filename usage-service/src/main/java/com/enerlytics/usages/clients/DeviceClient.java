package com.enerlytics.usages.clients;

import com.enerlytics.usages.dtos.responses.DeviceResponse;
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
public class DeviceClient {

    private final String baseUrl;
    private final RestTemplate restTemplate;

    public DeviceClient(@Value("${device-service.base-url}") String baseUrl, RestTemplate restTemplate) {
        this.baseUrl = baseUrl;
        this.restTemplate = restTemplate;
    }

    public Map<UUID, DeviceResponse> getDevicesByIds(Set<UUID> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            String url = UriComponentsBuilder.fromUriString(baseUrl)
                    .path("/batch")
                    .queryParam("ids", deviceIds.stream().map(UUID::toString).collect(Collectors.joining(",")))
                    .toUriString();

            ResponseEntity<List<DeviceResponse>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<List<DeviceResponse>>() {});

            return Optional.ofNullable(response.getBody()).orElse(Collections.emptyList()).stream()
                    .collect(Collectors.toMap(DeviceResponse::id, Function.identity()));
        } catch (RestClientException e) {
            log.error("Failed to fetch devices in batch: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
