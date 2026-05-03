package com.enerlytics.devices.utils;

import com.enerlytics.devices.entities.Device;
import com.enerlytics.devices.entities.DeviceType;
import com.enerlytics.devices.repositories.DeviceRepository;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.init-data", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private final DeviceRepository repository;
    private final Random random = new Random();

    @Override
    public void run(String @NonNull ... args) {
        if (repository.count() == 0) {
            initializeDevices();
        } else {
            log.info("Devices already present, skipping initialization.");
        }
    }

    private void initializeDevices() {
        log.info("Initializing sample devices...");

        List<Device> devices = List.of(
                createDevice("Living Room Thermostat", DeviceType.THERMOSTAT, "Living Room"),
                createDevice("Kitchen Smart Light", DeviceType.LIGHT, "Kitchen"),
                createDevice("Front Door Camera", DeviceType.CAMERA, "Front Door"),
                createDevice("Living Room Speaker", DeviceType.SPEAKER, "Living Room"),
                createDevice("Front Door Lock", DeviceType.LOCK, "Front Door"),
                createDevice("Front Door Doorbell", DeviceType.DOORBELL, "Front Door"),
                createDevice("Bedroom Light", DeviceType.LIGHT, "Bedroom"),
                createDevice("Backyard Camera", DeviceType.CAMERA, "Backyard"),
                createDevice("Kitchen Speaker", DeviceType.SPEAKER, "Kitchen"),
                createDevice("Garage Lock", DeviceType.LOCK, "Garage"),
                createDevice("Porch Doorbell", DeviceType.DOORBELL, "Porch"),
                createDevice("Hallway Thermostat", DeviceType.THERMOSTAT, "Hallway"),
                createDevice("Bathroom Light", DeviceType.LIGHT, "Bathroom"),
                createDevice("Driveway Camera", DeviceType.CAMERA, "Driveway"),
                createDevice("Bedroom Speaker", DeviceType.SPEAKER, "Bedroom"),
                createDevice("Basement Lock", DeviceType.LOCK, "Basement"),
                createDevice("Side Door Doorbell", DeviceType.DOORBELL, "Side Door"),
                createDevice("Office Thermostat", DeviceType.THERMOSTAT, "Office"),
                createDevice("Garden Light", DeviceType.LIGHT, "Garden"),
                createDevice("Master Bedroom AC", DeviceType.THERMOSTAT, "Bedroom"));

        repository.saveAll(devices);
        log.info("Sample devices initialized successfully (Count: {}).", devices.size());
    }

    private Device createDevice(String name, DeviceType type, String location) {
        return Device.builder()
                .name(name)
                .type(type)
                .location(location)
                .userId(random.nextInt(10) + 1L)
                .build();
    }
}
