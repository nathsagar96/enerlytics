package com.enerlytics.users.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateUserRequest(
        String firstName,
        String lastName,
        @Email String email,
        String address,
        Boolean alerting,
        @PositiveOrZero Double energyAlertingThreshold) {}
