package com.enerlytics.devices.controllers;

import com.enerlytics.devices.dtos.requests.DeviceRequest;
import com.enerlytics.devices.dtos.responses.DeviceResponse;
import com.enerlytics.devices.dtos.responses.PageResponse;
import com.enerlytics.devices.services.DeviceService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/devices")
public class DeviceController {
    private final DeviceService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    DeviceResponse createDevice(@Valid @RequestBody DeviceRequest request) {
        return service.createDevice(request);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    DeviceResponse getDeviceById(@PathVariable UUID id) {
        return service.getDeviceById(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    PageResponse<DeviceResponse> getAllDevices(
            @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize) {
        return service.getAllDevices(pageNumber, pageSize);
    }

    @GetMapping("/batch")
    @ResponseStatus(HttpStatus.OK)
    List<DeviceResponse> getDevicesByIds(@RequestParam Set<UUID> ids) {
        return service.getDevicesByIds(ids);
    }

    @GetMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.OK)
    PageResponse<DeviceResponse> getAllDevicesByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        return service.getAllDevicesByUserId(userId, pageNumber, pageSize);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    DeviceResponse updateDevice(@PathVariable UUID id, @Valid @RequestBody DeviceRequest request) {
        return service.updateDevice(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteDevice(@PathVariable UUID id) {
        service.deleteDevice(id);
    }
}
