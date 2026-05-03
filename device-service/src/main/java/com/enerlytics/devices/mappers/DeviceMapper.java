package com.enerlytics.devices.mappers;

import com.enerlytics.devices.dtos.requests.CreateDeviceRequest;
import com.enerlytics.devices.dtos.requests.UpdateDeviceRequest;
import com.enerlytics.devices.dtos.responses.DeviceResponse;
import com.enerlytics.devices.entities.Device;
import org.springframework.stereotype.Component;

@Component
public class DeviceMapper {

    public Device toEntity(CreateDeviceRequest request) {
        return Device.builder()
                .name(request.name())
                .deviceType(request.deviceType())
                .location(request.location())
                .userId(request.userId())
                .build();
    }

    public void updateEntity(Device device, UpdateDeviceRequest request) {
        if (request.name() != null) {
            device.setName(request.name());
        }
        if (request.location() != null) {
            device.setLocation(request.location());
        }
        if (request.userId() != null) {
            device.setUserId(request.userId());
        }
    }

    public DeviceResponse toResponse(Device device) {
        return new DeviceResponse(
                device.getId(), device.getName(), device.getDeviceType(), device.getLocation(), device.getUserId());
    }
}
