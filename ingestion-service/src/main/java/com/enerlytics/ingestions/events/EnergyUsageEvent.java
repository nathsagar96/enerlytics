package com.enerlytics.ingestions.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.UUID;

public record EnergyUsageEvent(
        UUID deviceId, Double energyConsumed, @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp) {}
