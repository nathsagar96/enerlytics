package com.enerlytics.ingestions.dtos.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;

public record IngestionRequest(
        @Schema(example = "101") @NotNull @Positive Long deviceId,
        @Schema(example = "2.45") @NotNull @PositiveOrZero Double energyConsumed,

        @Schema(example = "2026-05-03T10:15:30Z") @JsonFormat(shape = JsonFormat.Shape.STRING) @NotNull
        Instant timestamp) {}
