package com.enerlytics.usages.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceEnergy {
    private Long deviceId;
    private Double energyConsumed;
    private Long userId;
}
