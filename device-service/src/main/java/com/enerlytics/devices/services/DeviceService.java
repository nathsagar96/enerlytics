package com.enerlytics.devices.services;

import com.enerlytics.devices.dtos.requests.CreateDeviceRequest;
import com.enerlytics.devices.dtos.requests.UpdateDeviceRequest;
import com.enerlytics.devices.dtos.responses.DeviceResponse;
import com.enerlytics.devices.entities.Device;
import com.enerlytics.devices.exceptions.ResourceNotFoundException;
import com.enerlytics.devices.mappers.DeviceMapper;
import com.enerlytics.devices.repositories.DeviceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository repository;
    private final DeviceMapper mapper;

    @Transactional
    public DeviceResponse createDevice(CreateDeviceRequest request) {
        log.info("Creating new device: {} for user: {}", request.name(), request.userId());
        Device device = mapper.toEntity(request);
        Device savedDevice = repository.save(device);
        log.debug("Device saved with id: {}", savedDevice.getId());
        return mapper.toResponse(savedDevice);
    }

    @Transactional(readOnly = true)
    public DeviceResponse getDeviceById(Long id) {
        log.info("Fetching device with id: {}", id);
        return mapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> getAllDevices() {
        log.info("Fetching all devices");
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public DeviceResponse updateDevice(Long id, UpdateDeviceRequest request) {
        log.info("Updating device with id: {}", id);
        Device device = findById(id);
        mapper.updateEntity(device, request);
        Device updatedDevice = repository.save(device);
        log.debug("Device with id: {} updated successfully", id);
        return mapper.toResponse(updatedDevice);
    }

    @Transactional
    public void deleteDevice(Long id) {
        log.info("Deleting device with id: {}", id);
        Device device = findById(id);
        repository.delete(device);
        log.debug("Device with id: {} deleted successfully", id);
    }

    private Device findById(Long id) {
        return repository.findById(id).orElseThrow(() -> {
            log.error("Device not found with id: {}", id);
            return new ResourceNotFoundException("Device not found with id: " + id);
        });
    }

    public List<DeviceResponse> getAllDevicesByUserId(Long userId) {
        log.info("Fetching all devices for user id: {}", userId);
        List<Device> devices = repository.findAllByUserId(userId);
        log.debug("Found {} devices for user id: {}", devices.size(), userId);
        return devices.stream().map(mapper::toResponse).toList();
    }
}
