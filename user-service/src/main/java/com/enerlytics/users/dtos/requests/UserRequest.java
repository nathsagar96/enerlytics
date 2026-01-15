package com.enerlytics.users.dtos.requests;

import jakarta.validation.constraints.*;

public record UserRequest(
        @NotBlank(message = "First name is required")
                @Size(max = 50, message = "First name can be of maximum {max} characters")
                String firstName,
        @Size(max = 50, message = "First name can be of maximum {max} characters") String lastName,
        @Email(message = "Email should be valid") @NotBlank(message = "Email is required") String email,
        @Size(max = 256, message = "Address can be of maximum {max} characters") String address,
        @NotNull(message = "Alerting setting is required") Boolean alerting,
        @NotNull(message = "Energy alerting threshold is required")
                @Positive(message = "Energy alerting threshold must be positive")
                Double energyAlertingThreshold) {}
