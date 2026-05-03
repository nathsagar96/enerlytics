package com.enerlytics.usages.dtos.responses;

import com.enerlytics.usages.dtos.DeviceResponse;
import java.util.List;

public record UsageResponse(Long userId, List<DeviceResponse> devices) {}
