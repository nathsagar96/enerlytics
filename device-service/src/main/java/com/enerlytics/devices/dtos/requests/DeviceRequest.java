package com.enerlytics.devices.dtos.requests;

import com.enerlytics.devices.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record DeviceRequest(
        @NotBlank(message = "Name is required") @Size(max = 50, message = "Name must be less than {max} characters")
                String name,
        @NotNull(message = "Type is required") DeviceType type,
        @NotBlank(message = "Location is required")
                @Size(max = 256, message = "Location must be less than {256} characters")
                String location,
        @NotNull(message = "User ID is required") UUID userId) {}
