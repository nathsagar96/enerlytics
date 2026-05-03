package com.enerlytics.users.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateUserRequest(
        @Schema(example = "Ava") @NotBlank String firstName,
        @Schema(example = "Shah") String lastName,
        @Schema(example = "ava.shah@example.com") @NotBlank @Email String email,
        @Schema(example = "221B Baker Street") String address,
        @Schema(example = "true") Boolean alerting,
        @Schema(example = "18.5") @PositiveOrZero Double energyAlertingThreshold) {}
