package com.enerlytics.devices.dtos.requests;

import com.enerlytics.devices.entities.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateDeviceRequest(
        @Schema(example = "Living Room Thermostat") @NotBlank @Size(max = 255)
        String name,

        @Schema(example = "THERMOSTAT") @NotNull DeviceType deviceType,

        @Schema(example = "Living Room") @NotBlank @Size(max = 255)
        String location,

        @Schema(example = "1") @NotNull @Positive Long userId) {}
