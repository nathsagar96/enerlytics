package com.enerlytics.devices.mappers;

import com.enerlytics.devices.dtos.requests.DeviceRequest;
import com.enerlytics.devices.dtos.responses.DeviceResponse;
import com.enerlytics.devices.dtos.responses.PageResponse;
import com.enerlytics.devices.entities.Device;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class DeviceMapper {

    public Device toEntity(DeviceRequest request) {
        return Device.builder()
                .name(request.name())
                .type(request.type())
                .location(request.location())
                .userId(request.userId())
                .build();
    }

    public DeviceResponse toResponse(Device device) {
        return new DeviceResponse(
                device.getId(), device.getName(), device.getType(), device.getLocation(), device.getUserId());
    }

    public PageResponse<DeviceResponse> toPageResponse(Page<Device> devicePage) {
        return new PageResponse<>(
                devicePage.getContent().stream().map(this::toResponse).toList(),
                devicePage.getNumber(),
                devicePage.getSize(),
                devicePage.getTotalPages(),
                devicePage.getTotalElements());
    }
}
