package com.enerlytics.usages.dtos;

public record DeviceResponse(
        Long id, String name, String deviceType, String location, Long userId, Double energyConsumed) {

    public void withEnergyConsumed(Double energyConsumed) {
        new DeviceResponse(this.id, this.name, this.deviceType, this.location, this.userId, energyConsumed);
    }
}
