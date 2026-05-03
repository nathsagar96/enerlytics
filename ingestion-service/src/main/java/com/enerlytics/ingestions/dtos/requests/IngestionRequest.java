package com.enerlytics.ingestions.dtos.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;

public record IngestionRequest(
        @NotNull @Positive Long deviceId,
        @NotNull @PositiveOrZero Double energyConsumed,

        @JsonFormat(shape = JsonFormat.Shape.STRING) @NotNull
        Instant timestamp) {}
