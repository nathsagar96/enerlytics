package com.enerlytics.devices.dtos.requests;

import com.enerlytics.devices.entities.DeviceType;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateDeviceRequest(
        @Size(max = 255) String name,
        @Null(message = "deviceType cannot be updated") DeviceType deviceType,
        @Size(max = 255) String location,
        @Positive Long userId) {}
