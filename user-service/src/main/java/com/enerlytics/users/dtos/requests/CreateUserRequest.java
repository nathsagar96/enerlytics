package com.enerlytics.users.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateUserRequest(
        @NotBlank String firstName,
        String lastName,
        @NotBlank @Email String email,
        String address,
        Boolean alerting,
        @PositiveOrZero Double energyAlertingThreshold) {}
