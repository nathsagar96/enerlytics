package com.enerlytics.usages.dtos.responses;

import java.util.List;

public record UsageResponse(Long userId, List<DeviceUsageResponse> devices) {}
