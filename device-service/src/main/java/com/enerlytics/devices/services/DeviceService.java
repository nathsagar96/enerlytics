package com.enerlytics.devices.services;

import com.enerlytics.devices.dtos.requests.DeviceRequest;
import com.enerlytics.devices.dtos.responses.DeviceResponse;
import com.enerlytics.devices.dtos.responses.PageResponse;
import com.enerlytics.devices.entities.Device;
import com.enerlytics.devices.exceptions.DeviceNotFoundException;
import com.enerlytics.devices.mappers.DeviceMapper;
import com.enerlytics.devices.repositories.DeviceRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository repository;
    private final DeviceMapper mapper;

    public DeviceResponse createDevice(DeviceRequest request) {
        log.debug("Creating device with name: {}", request.name());

        Device device = mapper.toEntity(request);
        Device savedDevice = repository.save(device);

        log.info("Device created successfully with id: {}", savedDevice.getId());
        return mapper.toResponse(savedDevice);
    }

    public DeviceResponse getDeviceById(UUID id) {
        log.debug("Fetching device with id: {}", id);
        Device device = repository
                .findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + id));

        log.info("Device fetched successfully with id: {}", id);
        return mapper.toResponse(device);
    }

    public PageResponse<DeviceResponse> getAllDevices(int pageNumber, int pageSize) {
        log.debug("Fetching all devices with pageNumber: {} and pageSize: {}", pageNumber, pageSize);

        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        Page<Device> devicePage = repository.findAll(pageRequest);

        log.info("Fetched {} devices successfully", devicePage.getTotalElements());
        return mapper.toPageResponse(devicePage);
    }

    public PageResponse<DeviceResponse> getAllDevicesByUserId(UUID userId, int pageNumber, int pageSize) {
        log.debug(
                "Fetching all devices for User Id: {} with pageNumber: {} and pageSize: {}",
                userId,
                pageNumber,
                pageSize);

        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        Page<Device> devicePage = repository.findAllByUserId(userId, pageRequest);

        log.info("Fetched {} devices for User Id: {} successfully", devicePage.getTotalElements(), userId);
        return mapper.toPageResponse(devicePage);
    }

    public DeviceResponse updateDevice(UUID id, DeviceRequest request) {
        log.debug("Updating device with id: {}", id);
        Device existingDevice = repository
                .findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + id));

        existingDevice.setName(request.name());
        existingDevice.setType(request.type());
        existingDevice.setLocation(request.location());
        existingDevice.setUserId(request.userId());

        Device updatedDevice = repository.save(existingDevice);

        log.info("Device updated successfully with id: {}", updatedDevice.getId());
        return mapper.toResponse(updatedDevice);
    }

    public void deleteDevice(UUID id) {
        log.debug("Deleting device with id: {}", id);

        if (!repository.existsById(id)) {
            throw new DeviceNotFoundException("Device not found with id: " + id);
        }
        repository.deleteById(id);

        log.info("Device deleted successfully with id: {}", id);
    }
}
