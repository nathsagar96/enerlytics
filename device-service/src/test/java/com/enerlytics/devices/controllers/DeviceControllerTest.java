package com.enerlytics.devices.controllers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.enerlytics.devices.dtos.requests.DeviceRequest;
import com.enerlytics.devices.dtos.responses.DeviceResponse;
import com.enerlytics.devices.dtos.responses.PageResponse;
import com.enerlytics.devices.enums.DeviceType;
import com.enerlytics.devices.exceptions.DeviceNotFoundException;
import com.enerlytics.devices.services.DeviceService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(DeviceController.class)
@AutoConfigureMockMvc
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviceService deviceService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("POST /api/v1/devices (createDevice)")
    class CreateDevice {
        @Test
        @DisplayName("should return 201 CREATED and device response when valid request is provided")
        void shouldReturn201CreatedAndDeviceResponseWhenValidRequestIsProvided() throws Exception {
            // Arrange
            DeviceRequest request = new DeviceRequest("Test Device", DeviceType.SPEAKER, "Location", UUID.randomUUID());
            DeviceResponse expectedResponse = new DeviceResponse(
                    UUID.randomUUID(), "Test Device", DeviceType.SPEAKER, "Location", request.userId());

            when(deviceService.createDevice(request)).thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(post("/api/v1/devices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));

            verify(deviceService, times(1)).createDevice(request);
        }

        @Test
        @DisplayName("should return 400 BAD REQUEST when invalid request is provided")
        void shouldReturn400BadRequestWhenInvalidRequestIsProvided() throws Exception {
            // Arrange
            DeviceRequest invalidRequest = new DeviceRequest("", null, "", null);

            // Act & Assert
            mockMvc.perform(post("/api/v1/devices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(deviceService, never()).createDevice(any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/devices/{id} (getDeviceById)")
    class GetDeviceById {
        @Test
        @DisplayName("should return 200 OK and device response when device exists")
        void shouldReturn200OkAndDeviceResponseWhenDeviceExists() throws Exception {
            // Arrange
            UUID deviceId = UUID.randomUUID();
            DeviceResponse expectedResponse =
                    new DeviceResponse(deviceId, "Test Device", DeviceType.SPEAKER, "Location", UUID.randomUUID());

            when(deviceService.getDeviceById(deviceId)).thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/devices/{id}", deviceId))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));

            verify(deviceService, times(1)).getDeviceById(deviceId);
        }

        @Test
        @DisplayName("should return 404 NOT FOUND when device does not exist")
        void shouldReturn404NotFoundWhenDeviceDoesNotExist() throws Exception {
            // Arrange
            UUID deviceId = UUID.randomUUID();

            when(deviceService.getDeviceById(deviceId)).thenThrow(new DeviceNotFoundException("deviceId"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/devices/{id}", deviceId)).andExpect(status().isNotFound());

            verify(deviceService, times(1)).getDeviceById(deviceId);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/devices (getAllDevices)")
    class GetAllDevices {
        @Test
        @DisplayName("should return 200 OK and page response with default pagination")
        void shouldReturn200OkAndPageResponseWithDefaultPagination() throws Exception {
            // Arrange
            int pageNumber = 0;
            int pageSize = 10;
            DeviceResponse device1 = new DeviceResponse(
                    UUID.randomUUID(), "Device 1", DeviceType.SPEAKER, "Location", UUID.randomUUID());
            DeviceResponse device2 =
                    new DeviceResponse(UUID.randomUUID(), "Device 2", DeviceType.CAMERA, "Location", UUID.randomUUID());
            PageResponse<DeviceResponse> expectedResponse =
                    new PageResponse<>(List.of(device1, device2), pageNumber, pageSize, 1, 2L);

            when(deviceService.getAllDevices(pageNumber, pageSize)).thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/devices")
                            .param("pageNumber", String.valueOf(pageNumber))
                            .param("pageSize", String.valueOf(pageSize)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));

            verify(deviceService, times(1)).getAllDevices(pageNumber, pageSize);
        }

        @Test
        @DisplayName("should return 200 OK and page response with custom pagination")
        void shouldReturn200OkAndPageResponseWithCustomPagination() throws Exception {
            // Arrange
            int pageNumber = 1;
            int pageSize = 5;
            DeviceResponse device =
                    new DeviceResponse(UUID.randomUUID(), "Device", DeviceType.LIGHT, "Location", UUID.randomUUID());
            PageResponse<DeviceResponse> expectedResponse =
                    new PageResponse<>(List.of(device), pageNumber, pageSize, 1, 1L);

            when(deviceService.getAllDevices(pageNumber, pageSize)).thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/devices")
                            .param("pageNumber", String.valueOf(pageNumber))
                            .param("pageSize", String.valueOf(pageSize)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));

            verify(deviceService, times(1)).getAllDevices(pageNumber, pageSize);
        }

        @Test
        @DisplayName("should return 200 OK and empty page response when no devices exist")
        void shouldReturn200OkAndEmptyPageResponseWhenNoDevicesExist() throws Exception {
            // Arrange
            int pageNumber = 0;
            int pageSize = 10;
            PageResponse<DeviceResponse> expectedResponse = new PageResponse<>(List.of(), pageNumber, pageSize, 0, 0L);

            when(deviceService.getAllDevices(pageNumber, pageSize)).thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/devices")
                            .param("pageNumber", String.valueOf(pageNumber))
                            .param("pageSize", String.valueOf(pageSize)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));

            verify(deviceService, times(1)).getAllDevices(pageNumber, pageSize);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/devices/user/{userId} (getAllDevicesByUserId)")
    class GetAllDevicesByUserId {
        @Test
        @DisplayName("should return 200 OK and page response with user devices")
        void shouldReturn200OkAndPageResponseWithUserDevices() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            int pageNumber = 0;
            int pageSize = 10;
            DeviceResponse device1 =
                    new DeviceResponse(UUID.randomUUID(), "Device 1", DeviceType.SPEAKER, "Location", userId);
            DeviceResponse device2 =
                    new DeviceResponse(UUID.randomUUID(), "Device 2", DeviceType.CAMERA, "Location", userId);
            PageResponse<DeviceResponse> expectedResponse =
                    new PageResponse<>(List.of(device1, device2), pageNumber, pageSize, 1, 2L);

            when(deviceService.getAllDevicesByUserId(userId, pageNumber, pageSize))
                    .thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/devices/user/{userId}", userId)
                            .param("pageNumber", String.valueOf(pageNumber))
                            .param("pageSize", String.valueOf(pageSize)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));

            verify(deviceService, times(1)).getAllDevicesByUserId(userId, pageNumber, pageSize);
        }

        @Test
        @DisplayName("should return 200 OK and empty page response when user has no devices")
        void shouldReturn200OkAndEmptyPageResponseWhenUserHasNoDevices() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            int pageNumber = 0;
            int pageSize = 10;
            PageResponse<DeviceResponse> expectedResponse = new PageResponse<>(List.of(), pageNumber, pageSize, 0, 0L);

            when(deviceService.getAllDevicesByUserId(userId, pageNumber, pageSize))
                    .thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/devices/user/{userId}", userId)
                            .param("pageNumber", String.valueOf(pageNumber))
                            .param("pageSize", String.valueOf(pageSize)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));

            verify(deviceService, times(1)).getAllDevicesByUserId(userId, pageNumber, pageSize);
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/devices/{id} (updateDevice)")
    class UpdateDevice {
        @Test
        @DisplayName("should return 200 OK and updated device response when device exists")
        void shouldReturn200OkAndUpdatedDeviceResponseWhenDeviceExists() throws Exception {
            // Arrange
            UUID deviceId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            DeviceRequest request =
                    new DeviceRequest("Updated Device", DeviceType.THERMOSTAT, "Updated Location", userId);
            DeviceResponse expectedResponse =
                    new DeviceResponse(deviceId, "Updated Device", DeviceType.THERMOSTAT, "Updated Location", userId);

            when(deviceService.updateDevice(deviceId, request)).thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(put("/api/v1/devices/{id}", deviceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));

            verify(deviceService, times(1)).updateDevice(deviceId, request);
        }

        @Test
        @DisplayName("should return 404 NOT FOUND when device does not exist")
        void shouldReturn404NotFoundWhenDeviceDoesNotExist() throws Exception {
            // Arrange
            UUID deviceId = UUID.randomUUID();
            DeviceRequest request = new DeviceRequest("Updated Device", DeviceType.LOCK, "Location", UUID.randomUUID());

            when(deviceService.updateDevice(deviceId, request)).thenThrow(new DeviceNotFoundException("deviceId"));

            // Act & Assert
            mockMvc.perform(put("/api/v1/devices/{id}", deviceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            verify(deviceService, times(1)).updateDevice(deviceId, request);
        }

        @Test
        @DisplayName("should return 400 BAD REQUEST when invalid request is provided")
        void shouldReturn400BadRequestWhenInvalidRequestIsProvided() throws Exception {
            // Arrange
            UUID deviceId = UUID.randomUUID();
            DeviceRequest invalidRequest = new DeviceRequest("", null, "", null);

            // Act & Assert
            mockMvc.perform(put("/api/v1/devices/{id}", deviceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(deviceService, never()).updateDevice(any(), any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/devices/{id} (deleteDevice)")
    class DeleteDevice {
        @Test
        @DisplayName("should return 204 NO CONTENT when device exists")
        void shouldReturn204NoContentWhenDeviceExists() throws Exception {
            // Arrange
            UUID deviceId = UUID.randomUUID();

            doNothing().when(deviceService).deleteDevice(deviceId);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/devices/{id}", deviceId)).andExpect(status().isNoContent());

            verify(deviceService, times(1)).deleteDevice(deviceId);
        }

        @Test
        @DisplayName("should return 404 NOT FOUND when device does not exist")
        void shouldReturn404NotFoundWhenDeviceDoesNotExist() throws Exception {
            // Arrange
            UUID deviceId = UUID.randomUUID();

            doThrow(new DeviceNotFoundException("deviceId")).when(deviceService).deleteDevice(deviceId);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/devices/{id}", deviceId)).andExpect(status().isNotFound());

            verify(deviceService, times(1)).deleteDevice(deviceId);
        }
    }
}
