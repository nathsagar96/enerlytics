package com.enerlytics.usages.dtos.external;

public record UserServiceResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String address,
        Boolean alerting,
        Double energyAlertingThreshold) {}
