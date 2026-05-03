package com.enerlytics.devices.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enerlytics.devices.dtos.requests.CreateDeviceRequest;
import com.enerlytics.devices.dtos.requests.UpdateDeviceRequest;
import com.enerlytics.devices.dtos.responses.DeviceResponse;
import com.enerlytics.devices.entities.Device;
import com.enerlytics.devices.entities.DeviceType;
import com.enerlytics.devices.exceptions.ResourceNotFoundException;
import com.enerlytics.devices.mappers.DeviceMapper;
import com.enerlytics.devices.repositories.DeviceRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceMapper deviceMapper;

    @InjectMocks
    private DeviceService deviceService;

    public DeviceServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should create a new device successfully")
    void createDevice_Successful() {
        // Arrange
        CreateDeviceRequest request =
                new CreateDeviceRequest("Living Room Camera", DeviceType.CAMERA, "Living Room", 1L);

        Device deviceEntity = Device.builder()
                .id(1L)
                .name("Living Room Camera")
                .deviceType(DeviceType.CAMERA)
                .location("Living Room")
                .userId(1L)
                .build();

        DeviceResponse expectedResponse =
                new DeviceResponse(1L, "Living Room Camera", DeviceType.CAMERA, "Living Room", 1L);

        when(deviceMapper.toEntity(request)).thenReturn(deviceEntity);
        when(deviceRepository.save(any(Device.class))).thenReturn(deviceEntity);
        when(deviceMapper.toResponse(deviceEntity)).thenReturn(expectedResponse);

        // Act
        DeviceResponse actualResponse = deviceService.createDevice(request);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);

        verify(deviceMapper, times(1)).toEntity(request);
        verify(deviceRepository, times(1)).save(any(Device.class));
        verify(deviceMapper, times(1)).toResponse(deviceEntity);
    }

    @Test
    @DisplayName("Should return DeviceResponse when device with given ID exists")
    void getDeviceById_Successful() {
        // Arrange
        Long deviceId = 1L;

        Device deviceEntity = Device.builder()
                .id(deviceId)
                .name("Thermostat")
                .deviceType(DeviceType.THERMOSTAT)
                .location("Hallway")
                .userId(2L)
                .build();

        DeviceResponse expectedResponse =
                new DeviceResponse(deviceId, "Thermostat", DeviceType.THERMOSTAT, "Hallway", 2L);

        when(deviceRepository.findById(deviceId)).thenReturn(java.util.Optional.of(deviceEntity));
        when(deviceMapper.toResponse(deviceEntity)).thenReturn(expectedResponse);

        // Act
        DeviceResponse actualResponse = deviceService.getDeviceById(deviceId);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);

        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceMapper, times(1)).toResponse(deviceEntity);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when device with given ID does not exist")
    void getDeviceById_DeviceNotFound() {
        // Arrange
        Long deviceId = 999L;
        when(deviceRepository.findById(deviceId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> deviceService.getDeviceById(deviceId),
                "Expected getDeviceById() to throw ResourceNotFoundException");

        assertEquals("Device not found with id: 999", exception.getMessage());
        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should return all devices successfully")
    void getAllDevices_Successful() {
        // Arrange
        List<Device> devices = List.of(
                Device.builder()
                        .id(1L)
                        .name("Doorbell")
                        .deviceType(DeviceType.DOORBELL)
                        .location("Front Door")
                        .userId(1L)
                        .build(),
                Device.builder()
                        .id(2L)
                        .name("Speaker")
                        .deviceType(DeviceType.SPEAKER)
                        .location("Bedroom")
                        .userId(1L)
                        .build());

        List<DeviceResponse> expectedResponses = List.of(
                new DeviceResponse(1L, "Doorbell", DeviceType.DOORBELL, "Front Door", 1L),
                new DeviceResponse(2L, "Speaker", DeviceType.SPEAKER, "Bedroom", 1L));

        when(deviceRepository.findAll()).thenReturn(devices);
        when(deviceMapper.toResponse(devices.get(0))).thenReturn(expectedResponses.get(0));
        when(deviceMapper.toResponse(devices.get(1))).thenReturn(expectedResponses.get(1));

        // Act
        List<DeviceResponse> actualResponses = deviceService.getAllDevices();

        // Assert
        assertNotNull(actualResponses);
        assertEquals(expectedResponses.size(), actualResponses.size());
        assertEquals(expectedResponses, actualResponses);

        verify(deviceRepository, times(1)).findAll();
        verify(deviceMapper, times(2)).toResponse(any(Device.class));
    }

    @Test
    @DisplayName("Should return an empty list when no devices are available")
    void getAllDevices_EmptyList() {
        // Arrange
        when(deviceRepository.findAll()).thenReturn(List.of());

        // Act
        List<DeviceResponse> actualResponses = deviceService.getAllDevices();

        // Assert
        assertNotNull(actualResponses);
        assertEquals(0, actualResponses.size());

        verify(deviceRepository, times(1)).findAll();
        verify(deviceMapper, never()).toResponse(any(Device.class));
    }

    @Test
    @DisplayName("Should update device successfully")
    void updateDevice_Successful() {
        // Arrange
        Long deviceId = 1L;
        UpdateDeviceRequest request = new UpdateDeviceRequest("Outdoor Camera", null, "Garage", 3L);

        Device existingDevice = Device.builder()
                .id(deviceId)
                .name("Indoor Camera")
                .deviceType(DeviceType.CAMERA)
                .location("Kitchen")
                .userId(1L)
                .build();

        Device updatedDevice = Device.builder()
                .id(deviceId)
                .name("Outdoor Camera")
                .deviceType(DeviceType.CAMERA)
                .location("Garage")
                .userId(3L)
                .build();

        DeviceResponse expectedResponse =
                new DeviceResponse(deviceId, "Outdoor Camera", DeviceType.CAMERA, "Garage", 3L);

        when(deviceRepository.findById(deviceId)).thenReturn(java.util.Optional.of(existingDevice));
        doNothing().when(deviceMapper).updateEntity(existingDevice, request);
        when(deviceRepository.save(existingDevice)).thenReturn(updatedDevice);
        when(deviceMapper.toResponse(updatedDevice)).thenReturn(expectedResponse);

        // Act
        DeviceResponse actualResponse = deviceService.updateDevice(deviceId, request);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);

        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceMapper, times(1)).updateEntity(existingDevice, request);
        verify(deviceRepository, times(1)).save(existingDevice);
        verify(deviceMapper, times(1)).toResponse(updatedDevice);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating a non-existent device")
    void updateDevice_DeviceNotFound() {
        // Arrange
        Long deviceId = 404L;
        UpdateDeviceRequest request = new UpdateDeviceRequest("Camera", null, "Hall", 1L);
        when(deviceRepository.findById(deviceId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> deviceService.updateDevice(deviceId, request),
                "Expected updateDevice() to throw ResourceNotFoundException");

        assertEquals("Device not found with id: 404", exception.getMessage());
        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceMapper, never()).updateEntity(any(Device.class), any(UpdateDeviceRequest.class));
        verify(deviceRepository, never()).save(any(Device.class));
    }

    @Test
    @DisplayName("Should delete device successfully")
    void deleteDevice_Successful() {
        // Arrange
        Long deviceId = 1L;
        Device device = Device.builder()
                .id(deviceId)
                .name("Light")
                .deviceType(DeviceType.LIGHT)
                .location("Porch")
                .userId(2L)
                .build();

        when(deviceRepository.findById(deviceId)).thenReturn(java.util.Optional.of(device));
        doNothing().when(deviceRepository).delete(device);

        // Act
        deviceService.deleteDevice(deviceId);

        // Assert
        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceRepository, times(1)).delete(device);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting a non-existent device")
    void deleteDevice_DeviceNotFound() {
        // Arrange
        Long deviceId = 777L;
        when(deviceRepository.findById(deviceId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> deviceService.deleteDevice(deviceId),
                "Expected deleteDevice() to throw ResourceNotFoundException");

        assertEquals("Device not found with id: 777", exception.getMessage());
        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceRepository, never()).delete(any(Device.class));
    }

    @Test
    @DisplayName("Should return all devices for a given user ID")
    void getAllDevicesByUserId_Successful() {
        // Arrange
        Long userId = 1L;
        List<Device> devices = List.of(
                Device.builder()
                        .id(1L)
                        .name("Doorbell")
                        .deviceType(DeviceType.DOORBELL)
                        .location("Front Door")
                        .userId(userId)
                        .build(),
                Device.builder()
                        .id(2L)
                        .name("Speaker")
                        .deviceType(DeviceType.SPEAKER)
                        .location("Bedroom")
                        .userId(userId)
                        .build());

        List<DeviceResponse> expectedResponses = List.of(
                new DeviceResponse(1L, "Doorbell", DeviceType.DOORBELL, "Front Door", userId),
                new DeviceResponse(2L, "Speaker", DeviceType.SPEAKER, "Bedroom", userId));

        when(deviceRepository.findAllByUserId(userId)).thenReturn(devices);
        when(deviceMapper.toResponse(devices.get(0))).thenReturn(expectedResponses.get(0));
        when(deviceMapper.toResponse(devices.get(1))).thenReturn(expectedResponses.get(1));

        // Act
        List<DeviceResponse> actualResponses = deviceService.getAllDevicesByUserId(userId);

        // Assert
        assertNotNull(actualResponses);
        assertEquals(expectedResponses.size(), actualResponses.size());
        assertEquals(expectedResponses, actualResponses);

        verify(deviceRepository, times(1)).findAllByUserId(userId);
        verify(deviceMapper, times(2)).toResponse(any(Device.class));
    }
}
