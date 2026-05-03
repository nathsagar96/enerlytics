package com.enerlytics.devices.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.enerlytics.devices.dtos.requests.CreateDeviceRequest;
import com.enerlytics.devices.dtos.requests.UpdateDeviceRequest;
import com.enerlytics.devices.dtos.responses.DeviceResponse;
import com.enerlytics.devices.entities.DeviceType;
import com.enerlytics.devices.exceptions.ResourceNotFoundException;
import com.enerlytics.devices.services.DeviceService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@WebMvcTest(controllers = DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviceService deviceService;

    @Test
    @DisplayName("Should create a device successfully and return 201 Created")
    void createDevice_Success() throws Exception {
        // Arrange
        DeviceResponse response = new DeviceResponse(1L, "Living Room Camera", DeviceType.CAMERA, "Living Room", 1L);
        when(deviceService.createDevice(any(CreateDeviceRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "name": "Living Room Camera",
                                        "deviceType": "CAMERA",
                                        "location": "Living Room",
                                        "userId": 1
                                    }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Living Room Camera"))
                .andExpect(jsonPath("$.deviceType").value("CAMERA"))
                .andExpect(jsonPath("$.location").value("Living Room"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when required fields are missing")
    void createDevice_BadRequest_MissingFields() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "deviceType": "CAMERA",
                                        "location": "Living Room"
                                    }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors").isMap());
    }

    @Test
    @DisplayName("Should retrieve a device successfully and return 200 OK")
    void getDeviceById_Success() throws Exception {
        // Arrange
        DeviceResponse response = new DeviceResponse(1L, "Doorbell", DeviceType.DOORBELL, "Front Door", 2L);
        when(deviceService.getDeviceById(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/devices/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Doorbell"))
                .andExpect(jsonPath("$.deviceType").value("DOORBELL"))
                .andExpect(jsonPath("$.location").value("Front Door"))
                .andExpect(jsonPath("$.userId").value(2));
    }

    @Test
    @DisplayName("Should return 404 Not Found when the device does not exist")
    void getDeviceById_NotFound() throws Exception {
        // Arrange
        when(deviceService.getDeviceById(99L)).thenThrow(new ResourceNotFoundException("Device not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/devices/99").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Device not found"));
    }

    @Test
    @DisplayName("Should retrieve all devices successfully and return 200 OK")
    void getAllDevices_Success() throws Exception {
        // Arrange
        List<DeviceResponse> devices = List.of(
                new DeviceResponse(1L, "Speaker", DeviceType.SPEAKER, "Bedroom", 1L),
                new DeviceResponse(2L, "Thermostat", DeviceType.THERMOSTAT, "Hallway", 1L));
        when(deviceService.getAllDevices()).thenReturn(devices);

        // Act & Assert
        mockMvc.perform(get("/api/v1/devices").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Speaker"))
                .andExpect(jsonPath("$[0].deviceType").value("SPEAKER"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Thermostat"))
                .andExpect(jsonPath("$[1].deviceType").value("THERMOSTAT"));
    }

    @Test
    @DisplayName("Should return an empty list when no devices exist and return 200 OK")
    void getAllDevices_EmptyList() throws Exception {
        // Arrange
        when(deviceService.getAllDevices()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/devices").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Should update a device successfully and return 200 OK")
    void updateDevice_Success() throws Exception {
        // Arrange
        UpdateDeviceRequest request = new UpdateDeviceRequest("Outdoor Camera", null, "Garage", 3L);
        DeviceResponse response = new DeviceResponse(1L, "Outdoor Camera", DeviceType.CAMERA, "Garage", 3L);
        when(deviceService.updateDevice(1L, request)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/devices/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "name": "Outdoor Camera",
                                        "location": "Garage",
                                        "userId": 3
                                    }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Outdoor Camera"))
                .andExpect(jsonPath("$.deviceType").value("CAMERA"))
                .andExpect(jsonPath("$.location").value("Garage"))
                .andExpect(jsonPath("$.userId").value(3));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when deviceType is provided for update")
    void updateDevice_BadRequest_DeviceTypeProvided() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/devices/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "name": "Outdoor Camera",
                                        "deviceType": "CAMERA",
                                        "location": "Garage",
                                        "userId": 3
                                    }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors").isMap());
    }

    @Test
    @DisplayName("Should return 404 Not Found when updating a device that does not exist")
    void updateDevice_NotFound() throws Exception {
        // Arrange
        UpdateDeviceRequest request = new UpdateDeviceRequest("Outdoor Camera", null, "Garage", 3L);
        when(deviceService.updateDevice(99L, request)).thenThrow(new ResourceNotFoundException("Device not found"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/devices/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "name": "Outdoor Camera",
                                        "location": "Garage",
                                        "userId": 3
                                    }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Device not found"));
    }

    @Test
    @DisplayName("Should delete a device successfully and return 204 No Content")
    void deleteDevice_Success() throws Exception {
        // Arrange
        doNothing().when(deviceService).deleteDevice(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/devices/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 404 Not Found when deleting a device that does not exist")
    void deleteDevice_NotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Device not found"))
                .when(deviceService)
                .deleteDevice(99L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/devices/99").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Device not found"));
    }

    @Test
    @DisplayName("Should retrieve all devices by user id successfully and return 200 OK")
    void getAllDevicesByUserId_Success() throws Exception {
        // Arrange
        Long userId = 1L;
        List<DeviceResponse> devices = List.of(
                new DeviceResponse(1L, "Speaker", DeviceType.SPEAKER, "Bedroom", userId),
                new DeviceResponse(2L, "Thermostat", DeviceType.THERMOSTAT, "Hallway", userId));
        when(deviceService.getAllDevicesByUserId(userId)).thenReturn(devices);

        // Act & Assert
        mockMvc.perform(get("/api/v1/devices/user/" + userId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].userId").value(userId))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].userId").value(userId));
    }

    @Test
    @DisplayName("Should return an empty list when no devices exist for a user and return 200 OK")
    void getAllDevicesByUserId_EmptyList() throws Exception {
        // Arrange
        Long userId = 99L;
        when(deviceService.getAllDevicesByUserId(userId)).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/devices/user/" + userId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }
}
