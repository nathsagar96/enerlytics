package com.enerlytics.usages.clients;

import com.enerlytics.usages.dtos.external.UserServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestTemplate template;

    @Value("${user-service.base-url}")
    private String baseUrl;

    public UserServiceResponse getUserById(Long userId) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/{userId}")
                .buildAndExpand(userId)
                .toUriString();

        ResponseEntity<UserServiceResponse> response = template.getForEntity(url, UserServiceResponse.class);
        return response.getBody();
    }
}
