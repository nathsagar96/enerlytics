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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository repository;
    private final DeviceMapper mapper;

    @Transactional
    public DeviceResponse createDevice(CreateDeviceRequest request) {
        Device device = mapper.toEntity(request);
        return mapper.toResponse(repository.save(device));
    }

    @Transactional(readOnly = true)
    public DeviceResponse getDeviceById(Long id) {
        return mapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> getAllDevices() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public DeviceResponse updateDevice(Long id, UpdateDeviceRequest request) {
        Device device = findById(id);
        mapper.updateEntity(device, request);
        return mapper.toResponse(repository.save(device));
    }

    @Transactional
    public void deleteDevice(Long id) {
        repository.delete(findById(id));
    }

    private Device findById(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));
    }
}
