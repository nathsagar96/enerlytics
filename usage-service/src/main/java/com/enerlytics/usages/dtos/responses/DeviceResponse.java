package com.enerlytics.usages.dtos.responses;

import java.util.UUID;

public record DeviceResponse(UUID id, String name, String type, String location, UUID userId) {}
