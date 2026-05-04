package com.enerlytics.devices.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enerlytics.devices.dtos.requests.CreateDeviceRequest;
import com.enerlytics.devices.dtos.requests.UpdateDeviceRequest;
import com.enerlytics.devices.dtos.responses.DeviceResponse;
import com.enerlytics.devices.entities.DeviceType;
import com.enerlytics.devices.exceptions.ResourceNotFoundException;
import com.enerlytics.devices.repositories.DeviceRepository;
import com.enerlytics.devices.services.DeviceService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = "app.init-data=false")
class DeviceServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("devices")
            .withUsername("postgres")
            .withPassword("password");

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DeviceRepository deviceRepository;

    @BeforeEach
    void cleanup() {
        deviceRepository.deleteAll();
    }

    @Test
    @DisplayName("Should persist and fetch device")
    void createAndFetchDevice() {
        CreateDeviceRequest createRequest =
                new CreateDeviceRequest("Living Room Thermostat", DeviceType.THERMOSTAT, "Living Room", 1L);

        DeviceResponse created = deviceService.createDevice(createRequest);
        DeviceResponse fetched = deviceService.getDeviceById(created.id());

        assertNotNull(created.id());
        assertEquals("Living Room Thermostat", created.name());
        assertEquals(created, fetched);
    }

    @Test
    @DisplayName("Should fetch device by ID")
    void getDeviceById() {
        CreateDeviceRequest createRequest =
                new CreateDeviceRequest("Living Room Thermostat", DeviceType.THERMOSTAT, "Living Room", 1L);
        DeviceResponse created = deviceService.createDevice(createRequest);

        DeviceResponse fetched = deviceService.getDeviceById(created.id());

        assertNotNull(fetched);
        assertEquals(created.id(), fetched.id());
        assertEquals("Living Room Thermostat", fetched.name());
    }

    @Test
    @DisplayName("Should list all devices")
    void getAllDevices() {
        CreateDeviceRequest createRequest1 =
                new CreateDeviceRequest("Living Room Thermostat", DeviceType.THERMOSTAT, "Living Room", 1L);
        CreateDeviceRequest createRequest2 = new CreateDeviceRequest("Kitchen Sensor", DeviceType.OTHER, "Kitchen", 1L);

        deviceService.createDevice(createRequest1);
        deviceService.createDevice(createRequest2);

        List<DeviceResponse> devices = deviceService.getAllDevices();

        assertNotNull(devices);
        assertEquals(2, devices.size());
    }

    @Test
    @DisplayName("Should update device")
    void updateDevice() {
        CreateDeviceRequest createRequest =
                new CreateDeviceRequest("Living Room Thermostat", DeviceType.THERMOSTAT, "Living Room", 1L);
        DeviceResponse created = deviceService.createDevice(createRequest);

        UpdateDeviceRequest updateRequest = new UpdateDeviceRequest("Kitchen Thermostat", null, "Kitchen", 1L);
        DeviceResponse updated = deviceService.updateDevice(created.id(), updateRequest);

        assertNotNull(updated);
        assertEquals(created.id(), updated.id());
        assertEquals("Kitchen Thermostat", updated.name());
        assertEquals("Kitchen", updated.location());
    }

    @Test
    @DisplayName("Should delete device")
    void deleteDevice() {
        CreateDeviceRequest createRequest =
                new CreateDeviceRequest("Living Room Thermostat", DeviceType.THERMOSTAT, "Living Room", 1L);
        DeviceResponse created = deviceService.createDevice(createRequest);

        deviceService.deleteDevice(created.id());

        assertThrows(ResourceNotFoundException.class, () -> deviceService.getDeviceById(created.id()));
    }

    @Test
    @DisplayName("Should get all devices by user ID")
    void getAllDevicesByUserId() {
        CreateDeviceRequest createRequest1 =
                new CreateDeviceRequest("Living Room Thermostat", DeviceType.THERMOSTAT, "Living Room", 1L);
        CreateDeviceRequest createRequest2 = new CreateDeviceRequest("Kitchen Sensor", DeviceType.OTHER, "Kitchen", 1L);
        CreateDeviceRequest createRequest3 =
                new CreateDeviceRequest("Bedroom Light", DeviceType.LIGHT, "Bedroom", 2L); // Different user

        DeviceResponse device1 = deviceService.createDevice(createRequest1);
        DeviceResponse device2 = deviceService.createDevice(createRequest2);
        DeviceResponse device3 = deviceService.createDevice(createRequest3);

        List<DeviceResponse> devicesForUser1 = deviceService.getAllDevicesByUserId(1L);

        assertNotNull(devicesForUser1);
        assertEquals(2, devicesForUser1.size());
        // Check that we got the right devices (order may vary)
        boolean foundDevice1 = devicesForUser1.stream().anyMatch(d -> d.id().equals(device1.id()));
        boolean foundDevice2 = devicesForUser1.stream().anyMatch(d -> d.id().equals(device2.id()));
        assertTrue(foundDevice1 && foundDevice2);

        // Verify that user 2 has only one device
        List<DeviceResponse> devicesForUser2 = deviceService.getAllDevicesByUserId(2L);
        assertNotNull(devicesForUser2);
        assertEquals(1, devicesForUser2.size());
        assertTrue(devicesForUser2.stream().anyMatch(d -> d.id().equals(device3.id())));
    }
}
