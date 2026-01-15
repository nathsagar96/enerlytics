package com.enerlytics.ingestions.dtos.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.UUID;

public record EnergyUsageRequest(
        @NotNull(message = "deviceId is required") UUID deviceId,
        @Positive(message = "energy consumed should be positive") Double energyConsumed,
        @NotNull(message = "timestamp is required") @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp) {}
