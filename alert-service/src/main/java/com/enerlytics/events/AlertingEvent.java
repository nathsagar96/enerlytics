package com.enerlytics.events;

public record AlertingEvent(Long userId, String message, Double threshold, Double energyConsumed, String email) {}
