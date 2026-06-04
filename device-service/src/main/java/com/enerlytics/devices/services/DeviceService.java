package com.enerlytics.devices.services;

import com.enerlytics.devices.dtos.requests.CreateDeviceRequest;
import com.enerlytics.devices.dtos.requests.UpdateDeviceRequest;
import com.enerlytics.devices.dtos.responses.DeviceResponse;
import com.enerlytics.devices.entities.Device;
import com.enerlytics.devices.exceptions.ResourceNotFoundException;
import com.enerlytics.devices.mappers.DeviceMapper;
import com.enerlytics.devices.repositories.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    @Transactional
    public DeviceResponse createDevice(CreateDeviceRequest request) {
        log.info("Creating new device: {} for user: {}", request.name(), request.userId());
        Device deviceEntity = deviceMapper.toEntity(request);
        Device savedDeviceEntity = deviceRepository.save(deviceEntity);
        log.debug("Device saved with id: {}", savedDeviceEntity.getId());
        return deviceMapper.toResponse(savedDeviceEntity);
    }

    @Transactional(readOnly = true)
    public DeviceResponse getDeviceById(Long id) {
        log.info("Fetching device with id: {}", id);
        return deviceMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public Page<DeviceResponse> getAllDevices(Pageable pageable) {
        log.info("Fetching devices page: {}", pageable);
        return deviceRepository.findAll(pageable).map(deviceMapper::toResponse);
    }

    @Transactional
    public DeviceResponse updateDevice(Long id, UpdateDeviceRequest request) {
        log.info("Updating device with id: {}", id);
        Device deviceEntity = findById(id);
        deviceMapper.updateEntity(deviceEntity, request);
        Device updatedDeviceEntity = deviceRepository.save(deviceEntity);
        log.debug("Device with id: {} updated successfully", id);
        return deviceMapper.toResponse(updatedDeviceEntity);
    }

    @Transactional
    public void deleteDevice(Long id) {
        log.info("Deleting device with id: {}", id);
        Device deviceEntity = findById(id);
        deviceRepository.delete(deviceEntity);
        log.debug("Device with id: {} deleted successfully", id);
    }

    @Transactional(readOnly = true)
    public Page<DeviceResponse> getAllDevicesByUserId(Long userId, Pageable pageable) {
        log.info("Fetching devices for user id: {} page: {}", userId, pageable);
        Page<Device> devicePage = deviceRepository.findAllByUserId(userId, pageable);
        log.debug("Found {} devices for user id: {}", devicePage.getNumberOfElements(), userId);
        return devicePage.map(deviceMapper::toResponse);
    }

    private Device findById(Long id) {
        return deviceRepository.findById(id).orElseThrow(() -> {
            log.error("Device not found with id: {}", id);
            return new ResourceNotFoundException("Device", id);
        });
    }
}
