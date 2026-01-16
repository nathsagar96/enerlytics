package com.enerlytics.devices.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.enerlytics.devices.dtos.requests.DeviceRequest;
import com.enerlytics.devices.dtos.responses.DeviceResponse;
import com.enerlytics.devices.dtos.responses.PageResponse;
import com.enerlytics.devices.entities.Device;
import com.enerlytics.devices.enums.DeviceType;
import com.enerlytics.devices.exceptions.DeviceNotFoundException;
import com.enerlytics.devices.mappers.DeviceMapper;
import com.enerlytics.devices.repositories.DeviceRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository repository;

    @Mock
    private DeviceMapper mapper;

    @InjectMocks
    private DeviceService deviceService;

    @Nested
    @DisplayName("Create Device")
    class CreateDevice {
        @Test
        @DisplayName("should return created device response when valid request is provided")
        void shouldReturnCreatedDeviceResponseWhenValidRequestIsProvided() {
            // Arrange
            DeviceRequest request = new DeviceRequest("Test Device", DeviceType.SPEAKER, "Location", UUID.randomUUID());
            Device device = new Device();
            device.setId(UUID.randomUUID());
            DeviceResponse expectedResponse =
                    new DeviceResponse(device.getId(), "Test Device", DeviceType.SPEAKER, "Location", request.userId());

            when(mapper.toEntity(request)).thenReturn(device);
            when(repository.save(device)).thenReturn(device);
            when(mapper.toResponse(device)).thenReturn(expectedResponse);

            // Act
            DeviceResponse actualResponse = deviceService.createDevice(request);

            // Assert
            assertNotNull(actualResponse);
            assertEquals(expectedResponse, actualResponse);
            verify(mapper, times(1)).toEntity(request);
            verify(repository, times(1)).save(device);
            verify(mapper, times(1)).toResponse(device);
        }
    }

    @Nested
    @DisplayName("Get Devices By IDs (Batch)")
    class GetDevicesByIds {
        @Test
        @DisplayName("should return list of device responses when device IDs exist")
        void shouldReturnListOfDeviceResponsesWhenDeviceIdsExist() {
            // Arrange
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            Set<UUID> ids = Set.of(id1, id2);

            Device device1 = new Device();
            device1.setId(id1);
            Device device2 = new Device();
            device2.setId(id2);
            List<Device> devices = List.of(device1, device2);

            DeviceResponse response1 =
                    new DeviceResponse(id1, "Device 1", DeviceType.SPEAKER, "Loc 1", UUID.randomUUID());
            DeviceResponse response2 =
                    new DeviceResponse(id2, "Device 2", DeviceType.CAMERA, "Loc 2", UUID.randomUUID());

            when(repository.findAllById(ids)).thenReturn(devices);
            when(mapper.toResponse(device1)).thenReturn(response1);
            when(mapper.toResponse(device2)).thenReturn(response2);

            // Act
            List<DeviceResponse> actualResponse = deviceService.getDevicesByIds(ids);

            // Assert
            assertNotNull(actualResponse);
            assertEquals(2, actualResponse.size());
            assertTrue(actualResponse.contains(response1));
            assertTrue(actualResponse.contains(response2));
            verify(repository, times(1)).findAllById(ids);
            verify(mapper, times(2)).toResponse(any());
        }

        @Test
        @DisplayName("should return empty list when IDs are null or empty")
        void shouldReturnEmptyListWhenIdsAreNullOrEmpty() {
            // Act & Assert
            assertTrue(deviceService.getDevicesByIds(null).isEmpty());
            assertTrue(deviceService.getDevicesByIds(Set.of()).isEmpty());
            verify(repository, never()).findAllById(any());
        }
    }

    @Nested
    @DisplayName("Get Device By ID")
    class GetDeviceById {
        @Test
        @DisplayName("should return device response when device exists")
        void shouldReturnDeviceResponseWhenDeviceExists() {
            // Arrange
            UUID deviceId = UUID.randomUUID();
            Device device = new Device();
            device.setId(deviceId);
            DeviceResponse expectedResponse =
                    new DeviceResponse(deviceId, "Test Device", DeviceType.SPEAKER, "Location", UUID.randomUUID());

            when(repository.findById(deviceId)).thenReturn(Optional.of(device));
            when(mapper.toResponse(device)).thenReturn(expectedResponse);

            // Act
            DeviceResponse actualResponse = deviceService.getDeviceById(deviceId);

            // Assert
            assertNotNull(actualResponse);
            assertEquals(expectedResponse, actualResponse);
            verify(repository, times(1)).findById(deviceId);
            verify(mapper, times(1)).toResponse(device);
        }

        @Test
        @DisplayName("should throw DeviceNotFoundException when device does not exist")
        void shouldThrowDeviceNotFoundExceptionWhenDeviceDoesNotExist() {
            // Arrange
            UUID deviceId = UUID.randomUUID();
            when(repository.findById(deviceId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(DeviceNotFoundException.class, () -> deviceService.getDeviceById(deviceId));
            verify(repository, times(1)).findById(deviceId);
        }
    }

    @Nested
    @DisplayName("Get All Devices")
    class GetAllDevices {
        @Test
        @DisplayName("should return page response with devices when devices exist")
        void shouldReturnPageResponseWithDevicesWhenDevicesExist() {
            // Arrange
            int pageNumber = 0;
            int pageSize = 10;
            PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

            Device device1 = new Device();
            device1.setId(UUID.randomUUID());
            Device device2 = new Device();
            device2.setId(UUID.randomUUID());
            List<Device> devices = List.of(device1, device2);
            Page<Device> devicePage = new PageImpl<>(devices, pageRequest, devices.size());

            DeviceResponse response1 =
                    new DeviceResponse(device1.getId(), "Device 1", DeviceType.SPEAKER, "Location", UUID.randomUUID());
            DeviceResponse response2 =
                    new DeviceResponse(device2.getId(), "Device 2", DeviceType.CAMERA, "Location", UUID.randomUUID());
            List<DeviceResponse> responseList = List.of(response1, response2);
            PageResponse<DeviceResponse> expectedResponse =
                    new PageResponse<>(responseList, pageNumber, pageSize, 1, devices.size());

            when(repository.findAll(pageRequest)).thenReturn(devicePage);
            when(mapper.toPageResponse(devicePage)).thenReturn(expectedResponse);

            // Act
            PageResponse<DeviceResponse> actualResponse = deviceService.getAllDevices(pageNumber, pageSize);

            // Assert
            assertNotNull(actualResponse);
            assertEquals(expectedResponse, actualResponse);
            assertEquals(2, actualResponse.content().size());
            verify(repository, times(1)).findAll(pageRequest);
            verify(mapper, times(1)).toPageResponse(devicePage);
        }

        @Test
        @DisplayName("should return empty page response when no devices exist")
        void shouldReturnEmptyPageResponseWhenNoDevicesExist() {
            // Arrange
            int pageNumber = 0;
            int pageSize = 10;
            PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
            Page<Device> emptyPage = new PageImpl<>(List.of(), pageRequest, 0);
            PageResponse<DeviceResponse> expectedResponse = new PageResponse<>(List.of(), pageNumber, pageSize, 0, 0L);

            when(repository.findAll(pageRequest)).thenReturn(emptyPage);
            when(mapper.toPageResponse(emptyPage)).thenReturn(expectedResponse);

            // Act
            PageResponse<DeviceResponse> actualResponse = deviceService.getAllDevices(pageNumber, pageSize);

            // Assert
            assertNotNull(actualResponse);
            assertEquals(expectedResponse, actualResponse);
            assertTrue(actualResponse.content().isEmpty());
            assertEquals(0, actualResponse.numberOfElements());
            verify(repository, times(1)).findAll(pageRequest);
            verify(mapper, times(1)).toPageResponse(emptyPage);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when page number is negative")
        void shouldThrowIllegalArgumentExceptionWhenPageNumberIsNegative() {
            // Arrange
            int pageNumber = -1;
            int pageSize = 10;

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> deviceService.getAllDevices(pageNumber, pageSize));
        }
    }

    @Nested
    @DisplayName("Get All Devices By User ID")
    class GetAllDevicesByUserId {
        @Test
        @DisplayName("should return page response with user devices when devices exist")
        void shouldReturnPageResponseWithUserDevicesWhenDevicesExist() {
            // Arrange
            UUID userId = UUID.randomUUID();
            int pageNumber = 0;
            int pageSize = 10;
            PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

            Device device1 = new Device();
            device1.setId(UUID.randomUUID());
            device1.setUserId(userId);
            Device device2 = new Device();
            device2.setId(UUID.randomUUID());
            device2.setUserId(userId);
            List<Device> devices = List.of(device1, device2);
            Page<Device> devicePage = new PageImpl<>(devices, pageRequest, devices.size());

            DeviceResponse response1 =
                    new DeviceResponse(device1.getId(), "Device 1", DeviceType.SPEAKER, "Location", userId);
            DeviceResponse response2 =
                    new DeviceResponse(device2.getId(), "Device 2", DeviceType.CAMERA, "Location", userId);
            List<DeviceResponse> responseList = List.of(response1, response2);
            PageResponse<DeviceResponse> expectedResponse =
                    new PageResponse<>(responseList, pageNumber, pageSize, 1, devices.size());

            when(repository.findAllByUserId(userId, pageRequest)).thenReturn(devicePage);
            when(mapper.toPageResponse(devicePage)).thenReturn(expectedResponse);

            // Act
            PageResponse<DeviceResponse> actualResponse =
                    deviceService.getAllDevicesByUserId(userId, pageNumber, pageSize);

            // Assert
            assertNotNull(actualResponse);
            assertEquals(expectedResponse, actualResponse);
            assertEquals(2, actualResponse.content().size());
            verify(repository, times(1)).findAllByUserId(userId, pageRequest);
            verify(mapper, times(1)).toPageResponse(devicePage);
        }

        @Test
        @DisplayName("should return empty page response when user has no devices")
        void shouldReturnEmptyPageResponseWhenUserHasNoDevices() {
            // Arrange
            UUID userId = UUID.randomUUID();
            int pageNumber = 0;
            int pageSize = 10;
            PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
            Page<Device> emptyPage = new PageImpl<>(List.of(), pageRequest, 0);
            PageResponse<DeviceResponse> expectedResponse = new PageResponse<>(List.of(), pageNumber, pageSize, 0, 0L);

            when(repository.findAllByUserId(userId, pageRequest)).thenReturn(emptyPage);
            when(mapper.toPageResponse(emptyPage)).thenReturn(expectedResponse);

            // Act
            PageResponse<DeviceResponse> actualResponse =
                    deviceService.getAllDevicesByUserId(userId, pageNumber, pageSize);

            // Assert
            assertNotNull(actualResponse);
            assertEquals(expectedResponse, actualResponse);
            assertTrue(actualResponse.content().isEmpty());
            assertEquals(0, actualResponse.numberOfElements());
            verify(repository, times(1)).findAllByUserId(userId, pageRequest);
            verify(mapper, times(1)).toPageResponse(emptyPage);
        }
    }

    @Nested
    @DisplayName("Update Device")
    class UpdateDevice {
        @Test
        @DisplayName("should return updated device response when device exists")
        void shouldReturnUpdatedDeviceResponseWhenDeviceExists() {
            // Arrange
            UUID deviceId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            DeviceRequest request =
                    new DeviceRequest("Updated Device", DeviceType.THERMOSTAT, "Updated Location", userId);

            Device existingDevice = new Device();
            existingDevice.setId(deviceId);
            existingDevice.setName("Old Name");
            existingDevice.setType(DeviceType.LIGHT);
            existingDevice.setLocation("Old Location");
            existingDevice.setUserId(UUID.randomUUID());

            Device updatedDevice = new Device();
            updatedDevice.setId(deviceId);
            updatedDevice.setName(request.name());
            updatedDevice.setType(request.type());
            updatedDevice.setLocation(request.location());
            updatedDevice.setUserId(request.userId());

            DeviceResponse expectedResponse =
                    new DeviceResponse(deviceId, request.name(), request.type(), request.location(), userId);

            when(repository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
            when(repository.save(existingDevice)).thenReturn(updatedDevice);
            when(mapper.toResponse(updatedDevice)).thenReturn(expectedResponse);

            // Act
            DeviceResponse actualResponse = deviceService.updateDevice(deviceId, request);

            // Assert
            assertNotNull(actualResponse);
            assertEquals(expectedResponse, actualResponse);
            assertEquals(request.name(), existingDevice.getName());
            assertEquals(request.type(), existingDevice.getType());
            assertEquals(request.location(), existingDevice.getLocation());
            assertEquals(request.userId(), existingDevice.getUserId());
            verify(repository, times(1)).findById(deviceId);
            verify(repository, times(1)).save(existingDevice);
            verify(mapper, times(1)).toResponse(updatedDevice);
        }

        @Test
        @DisplayName("should throw DeviceNotFoundException when device does not exist")
        void shouldThrowDeviceNotFoundExceptionWhenDeviceDoesNotExist() {
            // Arrange
            UUID deviceId = UUID.randomUUID();
            DeviceRequest request = new DeviceRequest("Updated Device", DeviceType.LOCK, "Location", UUID.randomUUID());

            when(repository.findById(deviceId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(DeviceNotFoundException.class, () -> deviceService.updateDevice(deviceId, request));
            verify(repository, times(1)).findById(deviceId);
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete Device")
    class DeleteDevice {
        @Test
        @DisplayName("should delete device successfully when device exists")
        void shouldDeleteDeviceSuccessfullyWhenDeviceExists() {
            // Arrange
            UUID deviceId = UUID.randomUUID();
            when(repository.existsById(deviceId)).thenReturn(true);

            // Act
            assertDoesNotThrow(() -> deviceService.deleteDevice(deviceId));

            // Assert
            verify(repository, times(1)).existsById(deviceId);
            verify(repository, times(1)).deleteById(deviceId);
        }

        @Test
        @DisplayName("should throw DeviceNotFoundException when device does not exist")
        void shouldThrowDeviceNotFoundExceptionWhenDeviceDoesNotExist() {
            // Arrange
            UUID deviceId = UUID.randomUUID();
            when(repository.existsById(deviceId)).thenReturn(false);

            // Act & Assert
            assertThrows(DeviceNotFoundException.class, () -> deviceService.deleteDevice(deviceId));
            verify(repository, times(1)).existsById(deviceId);
            verify(repository, never()).deleteById(any());
        }
    }
}
