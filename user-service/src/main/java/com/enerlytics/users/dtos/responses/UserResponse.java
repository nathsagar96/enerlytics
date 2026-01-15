package com.enerlytics.users.dtos.responses;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String address,
        Boolean alerting,
        Double energyAlertingThreshold) {}
