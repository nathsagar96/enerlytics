package com.enerlytics.devices.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.enerlytics.devices.dtos.requests.CreateDeviceRequest;
import com.enerlytics.devices.dtos.requests.UpdateDeviceRequest;
import com.enerlytics.devices.dtos.responses.DeviceResponse;
import com.enerlytics.devices.entities.DeviceType;
import com.enerlytics.devices.exceptions.ResourceNotFoundException;
import com.enerlytics.devices.repositories.DeviceRepository;
import com.enerlytics.devices.services.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
class DeviceServiceIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine")
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

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("Living Room Thermostat");
        assertThat(fetched).isEqualTo(created);
        assertThat(created.createdAt()).isNotNull();
        assertThat(created.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should fetch device by ID")
    void getDeviceById() {
        CreateDeviceRequest createRequest =
                new CreateDeviceRequest("Living Room Thermostat", DeviceType.THERMOSTAT, "Living Room", 1L);
        DeviceResponse created = deviceService.createDevice(createRequest);

        DeviceResponse fetched = deviceService.getDeviceById(created.id());

        assertThat(fetched).isNotNull();
        assertThat(fetched.id()).isEqualTo(created.id());
        assertThat(fetched.name()).isEqualTo("Living Room Thermostat");
    }

    @Test
    @DisplayName("Should list devices page")
    void getAllDevices() {
        deviceService.createDevice(
                new CreateDeviceRequest("Living Room Thermostat", DeviceType.THERMOSTAT, "Living Room", 1L));
        deviceService.createDevice(new CreateDeviceRequest("Kitchen Sensor", DeviceType.OTHER, "Kitchen", 1L));

        Pageable pageable = PageRequest.of(0, 20);
        Page<DeviceResponse> devices = deviceService.getAllDevices(pageable);

        assertThat(devices).isNotNull();
        assertThat(devices.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Should update device")
    void updateDevice() {
        CreateDeviceRequest createRequest =
                new CreateDeviceRequest("Living Room Thermostat", DeviceType.THERMOSTAT, "Living Room", 1L);
        DeviceResponse created = deviceService.createDevice(createRequest);

        UpdateDeviceRequest updateRequest = new UpdateDeviceRequest("Kitchen Thermostat", null, "Kitchen", 1L);
        DeviceResponse updated = deviceService.updateDevice(created.id(), updateRequest);

        assertThat(updated).isNotNull();
        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.name()).isEqualTo("Kitchen Thermostat");
        assertThat(updated.location()).isEqualTo("Kitchen");
    }

    @Test
    @DisplayName("Should delete device")
    void deleteDevice() {
        CreateDeviceRequest createRequest =
                new CreateDeviceRequest("Living Room Thermostat", DeviceType.THERMOSTAT, "Living Room", 1L);
        DeviceResponse created = deviceService.createDevice(createRequest);

        deviceService.deleteDevice(created.id());

        assertThatThrownBy(() -> deviceService.getDeviceById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get all devices by user ID")
    void getAllDevicesByUserId() {
        DeviceResponse device1 = deviceService.createDevice(
                new CreateDeviceRequest("Living Room Thermostat", DeviceType.THERMOSTAT, "Living Room", 1L));
        DeviceResponse device2 =
                deviceService.createDevice(new CreateDeviceRequest("Kitchen Sensor", DeviceType.OTHER, "Kitchen", 1L));
        DeviceResponse device3 =
                deviceService.createDevice(new CreateDeviceRequest("Bedroom Light", DeviceType.LIGHT, "Bedroom", 2L));

        Pageable pageable = PageRequest.of(0, 20);
        Page<DeviceResponse> devicesForUser1 = deviceService.getAllDevicesByUserId(1L, pageable);
        Page<DeviceResponse> devicesForUser2 = deviceService.getAllDevicesByUserId(2L, pageable);

        assertThat(devicesForUser1.getContent())
                .hasSize(2)
                .extracting(DeviceResponse::id)
                .containsExactlyInAnyOrder(device1.id(), device2.id());

        assertThat(devicesForUser2.getContent())
                .hasSize(1)
                .extracting(DeviceResponse::id)
                .containsExactly(device3.id());
    }
}
