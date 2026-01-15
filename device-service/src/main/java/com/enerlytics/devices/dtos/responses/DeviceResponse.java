package com.enerlytics.devices.dtos.responses;

import com.enerlytics.devices.enums.DeviceType;
import java.util.UUID;

public record DeviceResponse(UUID id, String name, DeviceType type, String location, UUID userId) {}
