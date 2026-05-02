package com.enerlytics.devices.dtos.requests;

import com.enerlytics.devices.entities.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateDeviceRequest(
        @NotBlank @Size(max = 255) String name,
        @NotNull DeviceType deviceType,
        @NotBlank @Size(max = 255) String location,
        @NotNull @Positive Long userId) {}
