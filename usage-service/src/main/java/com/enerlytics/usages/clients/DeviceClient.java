package com.enerlytics.usages.clients;

import com.enerlytics.usages.dtos.DeviceResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class DeviceClient {

    private final RestTemplate template;

    @Value("${device-service.base-url}")
    private String baseUrl;

    public DeviceResponse getDeviceById(Long deviceId) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/{deviceId}")
                .buildAndExpand(deviceId)
                .toUriString();

        ResponseEntity<DeviceResponse> response = template.getForEntity(url, DeviceResponse.class);
        return response.getBody();
    }

    public List<DeviceResponse> getAllDevicesForUser(Long userId) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/user/{userId}")
                .buildAndExpand(userId)
                .toUriString();

        ResponseEntity<DeviceResponse[]> response = template.getForEntity(url, DeviceResponse[].class);
        DeviceResponse[] devices = response.getBody();
        return devices == null ? List.of() : List.of(devices);
    }
}
