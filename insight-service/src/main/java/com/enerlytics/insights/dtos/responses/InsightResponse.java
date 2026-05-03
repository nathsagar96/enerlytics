package com.enerlytics.insights.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;

public record InsightResponse(
        @Schema(example = "1") Long userId,
        @Schema(example = "Shift high-load appliances to off-peak hours.") String tips,
        @Schema(example = "27.4") Double energyUsage) {}
