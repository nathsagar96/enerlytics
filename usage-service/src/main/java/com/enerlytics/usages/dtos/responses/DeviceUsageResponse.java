package com.enerlytics.usages.dtos.responses;

public record DeviceUsageResponse(
        Long id, String name, String deviceType, String location, Long userId, Double energyConsumed) {}
