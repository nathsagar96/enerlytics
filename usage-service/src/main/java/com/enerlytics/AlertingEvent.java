package com.enerlytics;

import java.util.UUID;
import lombok.Builder;

@Builder
public record AlertingEvent(UUID userId, String message, Double threshold, Double energyConsumed, String email) {}
