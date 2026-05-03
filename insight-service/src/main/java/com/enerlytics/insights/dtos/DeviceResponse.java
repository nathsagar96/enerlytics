package com.enerlytics.insights.dtos;

public record DeviceResponse(Long id, String name, String deviceType, String location, Double energyConsumed) {}
