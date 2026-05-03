package com.enerlytics.devices.dtos.responses;

import com.enerlytics.devices.entities.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;

public record DeviceResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Living Room Thermostat") String name,
        @Schema(example = "THERMOSTAT") DeviceType deviceType,
        @Schema(example = "Living Room") String location,
        @Schema(example = "1") Long userId) {}
