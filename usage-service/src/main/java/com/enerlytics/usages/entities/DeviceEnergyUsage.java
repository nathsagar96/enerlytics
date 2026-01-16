package com.enerlytics.usages.entities;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class DeviceEnergyUsage {
    private UUID deviceId;
    private Double energyConsumed;
    private UUID userId;
}
