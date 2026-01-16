package com.enerlytics.usages.configs;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "influxdb")
public record InfluxDBProperties(
        @NotBlank String url, @NotBlank String token, @NotBlank String org, @NotBlank String bucket) {}
