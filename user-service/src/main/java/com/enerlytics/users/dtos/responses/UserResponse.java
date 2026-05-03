package com.enerlytics.users.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Ava") String firstName,
        @Schema(example = "Shah") String lastName,
        @Schema(example = "ava.shah@example.com") String email,
        @Schema(example = "221B Baker Street") String address,
        @Schema(example = "true") Boolean alertingEnabled,
        @Schema(example = "18.5") Double energyAlertingThreshold) {}
