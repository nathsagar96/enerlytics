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
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviceService deviceService;

    @Test
    @DisplayName("Should create a device successfully and return 201 Created")
    void createDevice_Success() throws Exception {
        DeviceResponse response =
                new DeviceResponse(1L, "Living Room Camera", DeviceType.CAMERA, "Living Room", 1L, null, null);
        when(deviceService.createDevice(any(CreateDeviceRequest.class))).thenReturn(response);

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
        DeviceResponse response = new DeviceResponse(1L, "Doorbell", DeviceType.DOORBELL, "Front Door", 2L, null, null);
        when(deviceService.getDeviceById(1L)).thenReturn(response);

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
        when(deviceService.getDeviceById(99L)).thenThrow(new ResourceNotFoundException("Device", 99L));

        mockMvc.perform(get("/api/v1/devices/99").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Device not found with id: 99"));
    }

    @Test
    @DisplayName("Should retrieve a paginated list of devices and return 200 OK")
    void getAllDevices_Success() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        List<DeviceResponse> devices = List.of(
                new DeviceResponse(1L, "Speaker", DeviceType.SPEAKER, "Bedroom", 1L, null, null),
                new DeviceResponse(2L, "Thermostat", DeviceType.THERMOSTAT, "Hallway", 1L, null, null));
        Page<DeviceResponse> page = new PageImpl<>(devices, pageable, devices.size());
        when(deviceService.getAllDevices(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/devices").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Speaker"))
                .andExpect(jsonPath("$.content[0].deviceType").value("SPEAKER"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].name").value("Thermostat"))
                .andExpect(jsonPath("$.content[1].deviceType").value("THERMOSTAT"));
    }

    @Test
    @DisplayName("Should return an empty page when no devices exist and return 200 OK")
    void getAllDevices_EmptyList() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        when(deviceService.getAllDevices(any(Pageable.class))).thenReturn(Page.empty(pageable));

        mockMvc.perform(get("/api/v1/devices").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("Should update a device successfully and return 200 OK")
    void updateDevice_Success() throws Exception {
        UpdateDeviceRequest request = new UpdateDeviceRequest("Outdoor Camera", null, "Garage", 3L);
        DeviceResponse response = new DeviceResponse(1L, "Outdoor Camera", DeviceType.CAMERA, "Garage", 3L, null, null);
        when(deviceService.updateDevice(1L, request)).thenReturn(response);

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
        UpdateDeviceRequest request = new UpdateDeviceRequest("Outdoor Camera", null, "Garage", 3L);
        when(deviceService.updateDevice(99L, request)).thenThrow(new ResourceNotFoundException("Device", 99L));

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
                .andExpect(jsonPath("$.detail").value("Device not found with id: 99"));
    }

    @Test
    @DisplayName("Should delete a device successfully and return 204 No Content")
    void deleteDevice_Success() throws Exception {
        doNothing().when(deviceService).deleteDevice(1L);

        mockMvc.perform(delete("/api/v1/devices/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 404 Not Found when deleting a device that does not exist")
    void deleteDevice_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Device", 99L))
                .when(deviceService)
                .deleteDevice(99L);

        mockMvc.perform(delete("/api/v1/devices/99").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Device not found with id: 99"));
    }

    @Test
    @DisplayName("Should retrieve a paginated list of devices for a user and return 200 OK")
    void getAllDevicesByUserId_Success() throws Exception {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 20);
        List<DeviceResponse> devices = List.of(
                new DeviceResponse(1L, "Speaker", DeviceType.SPEAKER, "Bedroom", userId, null, null),
                new DeviceResponse(2L, "Thermostat", DeviceType.THERMOSTAT, "Hallway", userId, null, null));
        Page<DeviceResponse> page = new PageImpl<>(devices, pageable, devices.size());
        when(deviceService.getAllDevicesByUserId(any(Long.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/devices/user/" + userId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].userId").value(userId))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].userId").value(userId));
    }

    @Test
    @DisplayName("Should return an empty page when no devices exist for a user and return 200 OK")
    void getAllDevicesByUserId_EmptyList() throws Exception {
        Long userId = 99L;
        Pageable pageable = PageRequest.of(0, 20);
        when(deviceService.getAllDevicesByUserId(any(Long.class), any(Pageable.class)))
                .thenReturn(Page.empty(pageable));

        mockMvc.perform(get("/api/v1/devices/user/" + userId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(0));
    }
}
