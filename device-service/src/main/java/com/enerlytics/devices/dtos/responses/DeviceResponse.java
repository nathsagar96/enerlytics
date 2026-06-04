package com.enerlytics.devices.dtos.responses;

import com.enerlytics.devices.entities.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record DeviceResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Living Room Thermostat") String name,
        @Schema(example = "THERMOSTAT") DeviceType deviceType,
        @Schema(example = "Living Room") String location,
        @Schema(example = "1") Long userId,
        @Schema(example = "2024-01-01T00:00:00Z") Instant createdAt,
        @Schema(example = "2024-01-01T00:00:00Z") Instant updatedAt) {}
