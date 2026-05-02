package com.enerlytics.devices.dtos.responses;

import com.enerlytics.devices.entities.DeviceType;

public record DeviceResponse(Long id, String name, DeviceType deviceType, String location, Long userId) {}
