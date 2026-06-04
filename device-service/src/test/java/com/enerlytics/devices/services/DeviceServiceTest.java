package com.enerlytics.devices.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceMapper deviceMapper;

    @InjectMocks
    private DeviceService deviceService;

    @Test
    @DisplayName("Should create a new device successfully")
    void createDevice_Successful() {
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
                new DeviceResponse(1L, "Living Room Camera", DeviceType.CAMERA, "Living Room", 1L, null, null);

        when(deviceMapper.toEntity(request)).thenReturn(deviceEntity);
        when(deviceRepository.save(any(Device.class))).thenReturn(deviceEntity);
        when(deviceMapper.toResponse(deviceEntity)).thenReturn(expectedResponse);

        DeviceResponse actualResponse = deviceService.createDevice(request);

        assertThat(actualResponse).isNotNull().isEqualTo(expectedResponse);

        verify(deviceMapper, times(1)).toEntity(request);
        verify(deviceRepository, times(1)).save(any(Device.class));
        verify(deviceMapper, times(1)).toResponse(deviceEntity);
    }

    @Test
    @DisplayName("Should return DeviceResponse when device with given ID exists")
    void getDeviceById_Successful() {
        Long deviceId = 1L;

        Device deviceEntity = Device.builder()
                .id(deviceId)
                .name("Thermostat")
                .deviceType(DeviceType.THERMOSTAT)
                .location("Hallway")
                .userId(2L)
                .build();

        DeviceResponse expectedResponse =
                new DeviceResponse(deviceId, "Thermostat", DeviceType.THERMOSTAT, "Hallway", 2L, null, null);

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(deviceEntity));
        when(deviceMapper.toResponse(deviceEntity)).thenReturn(expectedResponse);

        DeviceResponse actualResponse = deviceService.getDeviceById(deviceId);

        assertThat(actualResponse).isNotNull().isEqualTo(expectedResponse);

        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceMapper, times(1)).toResponse(deviceEntity);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when device with given ID does not exist")
    void getDeviceById_DeviceNotFound() {
        Long deviceId = 999L;
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.getDeviceById(deviceId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Device not found with id: 999");

        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should return all devices page successfully")
    void getAllDevices_Successful() {
        Pageable pageable = PageRequest.of(0, 20);
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

        Page<Device> devicePage = new PageImpl<>(devices, pageable, devices.size());

        List<DeviceResponse> expectedResponses = List.of(
                new DeviceResponse(1L, "Doorbell", DeviceType.DOORBELL, "Front Door", 1L, null, null),
                new DeviceResponse(2L, "Speaker", DeviceType.SPEAKER, "Bedroom", 1L, null, null));

        when(deviceRepository.findAll(pageable)).thenReturn(devicePage);
        when(deviceMapper.toResponse(devices.get(0))).thenReturn(expectedResponses.get(0));
        when(deviceMapper.toResponse(devices.get(1))).thenReturn(expectedResponses.get(1));

        Page<DeviceResponse> actualResponses = deviceService.getAllDevices(pageable);

        assertThat(actualResponses).isNotNull();
        assertThat(actualResponses.getContent()).containsExactlyElementsOf(expectedResponses);

        verify(deviceRepository, times(1)).findAll(pageable);
        verify(deviceMapper, times(2)).toResponse(any(Device.class));
    }

    @Test
    @DisplayName("Should return an empty page when no devices are available")
    void getAllDevices_EmptyList() {
        Pageable pageable = PageRequest.of(0, 20);
        when(deviceRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

        Page<DeviceResponse> actualResponses = deviceService.getAllDevices(pageable);

        assertThat(actualResponses).isNotNull().isEmpty();

        verify(deviceRepository, times(1)).findAll(pageable);
        verify(deviceMapper, never()).toResponse(any(Device.class));
    }

    @Test
    @DisplayName("Should update device successfully")
    void updateDevice_Successful() {
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
                new DeviceResponse(deviceId, "Outdoor Camera", DeviceType.CAMERA, "Garage", 3L, null, null);

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        doNothing().when(deviceMapper).updateEntity(existingDevice, request);
        when(deviceRepository.save(existingDevice)).thenReturn(updatedDevice);
        when(deviceMapper.toResponse(updatedDevice)).thenReturn(expectedResponse);

        DeviceResponse actualResponse = deviceService.updateDevice(deviceId, request);

        assertThat(actualResponse).isNotNull().isEqualTo(expectedResponse);

        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceMapper, times(1)).updateEntity(existingDevice, request);
        verify(deviceRepository, times(1)).save(existingDevice);
        verify(deviceMapper, times(1)).toResponse(updatedDevice);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating a non-existent device")
    void updateDevice_DeviceNotFound() {
        Long deviceId = 404L;
        UpdateDeviceRequest request = new UpdateDeviceRequest("Camera", null, "Hall", 1L);
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.updateDevice(deviceId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Device not found with id: 404");

        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceMapper, never()).updateEntity(any(Device.class), any(UpdateDeviceRequest.class));
        verify(deviceRepository, never()).save(any(Device.class));
    }

    @Test
    @DisplayName("Should delete device successfully")
    void deleteDevice_Successful() {
        Long deviceId = 1L;
        Device device = Device.builder()
                .id(deviceId)
                .name("Light")
                .deviceType(DeviceType.LIGHT)
                .location("Porch")
                .userId(2L)
                .build();

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        doNothing().when(deviceRepository).delete(device);

        deviceService.deleteDevice(deviceId);

        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceRepository, times(1)).delete(device);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting a non-existent device")
    void deleteDevice_DeviceNotFound() {
        Long deviceId = 777L;
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.deleteDevice(deviceId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Device not found with id: 777");

        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceRepository, never()).delete(any(Device.class));
    }

    @Test
    @DisplayName("Should return paginated devices for a given user ID")
    void getAllDevicesByUserId_Successful() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 20);
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

        Page<Device> devicePage = new PageImpl<>(devices, pageable, devices.size());

        List<DeviceResponse> expectedResponses = List.of(
                new DeviceResponse(1L, "Doorbell", DeviceType.DOORBELL, "Front Door", userId, null, null),
                new DeviceResponse(2L, "Speaker", DeviceType.SPEAKER, "Bedroom", userId, null, null));

        when(deviceRepository.findAllByUserId(userId, pageable)).thenReturn(devicePage);
        when(deviceMapper.toResponse(devices.get(0))).thenReturn(expectedResponses.get(0));
        when(deviceMapper.toResponse(devices.get(1))).thenReturn(expectedResponses.get(1));

        Page<DeviceResponse> actualResponses = deviceService.getAllDevicesByUserId(userId, pageable);

        assertThat(actualResponses).isNotNull();
        assertThat(actualResponses.getContent()).containsExactlyElementsOf(expectedResponses);

        verify(deviceRepository, times(1)).findAllByUserId(userId, pageable);
        verify(deviceMapper, times(2)).toResponse(any(Device.class));
    }
}
