package com.enerlytics.users.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateUserRequest(
        @Schema(example = "Ava") String firstName,
        @Schema(example = "Shah") String lastName,
        @Schema(example = "ava.shah@example.com") @Email String email,
        @Schema(example = "42 Wallaby Way") String address,
        @Schema(example = "false") Boolean alertingEnabled,
        @Schema(example = "22.0") @PositiveOrZero Double energyAlertingThreshold) {}
