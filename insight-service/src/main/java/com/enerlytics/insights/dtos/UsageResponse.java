package com.enerlytics.insights.dtos;

import java.util.List;

public record UsageResponse(Long userId, List<DeviceResponse> devices) {}
