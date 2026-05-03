package com.enerlytics.usages.dtos;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String address,
        Boolean alerting,
        Double energyAlertingThreshold) {}
