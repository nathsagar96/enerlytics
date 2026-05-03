package com.enerlytics.devices.dtos.requests;

import com.enerlytics.devices.entities.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateDeviceRequest(
        @Schema(example = "Kitchen Thermostat") @Size(max = 255)
        String name,

        @Schema(example = "null", description = "Device type is immutable and must remain null on updates")
        @Null(message = "deviceType cannot be updated")
        DeviceType deviceType,

        @Schema(example = "Kitchen") @Size(max = 255) String location,
        @Schema(example = "1") @Positive Long userId) {}
