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
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DeviceRepository repository;

    @Override
    public void run(String @NonNull ... args) {
        if (repository.count() == 0) {
            initializeDevices();
        }
    }

    private void initializeDevices() {
        log.info("Initializing devices...");

        Random random = new Random();

        List<Device> devices = List.of(
                Device.builder()
                        .name("Smart Thermostat")
                        .type(DeviceType.THERMOSTAT)
                        .location("Living Room")
                        .userId(random.nextInt(10) + 1L)
                        .build(),
                Device.builder()
                        .name("Smart Light")
                        .type(DeviceType.LIGHT)
                        .location("Kitchen")
                        .userId(random.nextInt(10) + 1L)
                        .build(),
                Device.builder()
                        .name("Smart Camera")
                        .type(DeviceType.CAMERA)
                        .location("Front Door")
                        .userId(random.nextInt(10) + 1L)
                        .build(),
                Device.builder()
                        .name("Smart Speaker")
                        .type(DeviceType.SPEAKER)
                        .location("Living Room")
                        .userId(random.nextInt(10) + 1L)
                        .build(),
                Device.builder()
                        .name("Smart Lock")
                        .type(DeviceType.LOCK)
                        .location("Front Door")
                        .userId(random.nextInt(10) + 1L)
                        .build(),
                Device.builder()
                        .name("Smart Doorbell")
                        .type(DeviceType.DOORBELL)
                        .location("Front Door")
                        .userId(random.nextInt(10) + 1L)
                        .build(),
                Device.builder()
                        .name("Living Room Thermostat")
                        .type(DeviceType.THERMOSTAT)
                        .location("Living Room")
                        .userId(random.nextInt(10) + 1L)
                        .build(),
                Device.builder()
                        .name("Bedroom Light")
                        .type(DeviceType.LIGHT)
                        .location("Bedroom")
                        .userId(random.nextInt(10) + 1L)
                        .build(),
                Device.builder()
                        .name("Backyard Camera")
                        .type(DeviceType.CAMERA)
                        .location("Backyard")
                        .userId(random.nextInt(10) + 1L)
                        .build(),
                Device.builder()
                        .name("Kitchen Speaker")
                        .type(DeviceType.SPEAKER)
                        .location("Kitchen")
                        .userId(random.nextInt(10) + 1L)
                        .build(),
                Device.builder()
                        .name("Garage Lock")
                        .type(DeviceType.LOCK)
                        .location("Garage")
                        .userId(random.nextInt(10) + 1L)
                        .build(),
                Device.builder()
                        .name("Porch Doorbell")
                        .type(DeviceType.DOORBELL)
                        .location("Porch")
                        .userId(random.nextInt(10) + 1L)
                        .build(),
                Device.builder()
                        .name("Hallway Thermostat")
                        .type(DeviceType.THERMOSTAT)
                        .location("Hallway")
                        .userId(random.nextInt(10) + 1L)
                        .build(),
                Device.builder()
                        .name("Bathroom Light")
                        .type(DeviceType.LIGHT)
                        .location("Bathroom")
                        .userId(random.nextInt(10) + 1L)
                        .build(),
                Device.builder()
                        .name("Driveway Camera")
                        .type(DeviceType.CAMERA)
                        .location("Driveway")
                        .userId(random.nextInt(10) + 1L)
                        .build(),
                Device.builder()
                        .name("Bedroom Speaker")
                        .type(DeviceType.SPEAKER)
                        .location("Bedroom")
                        .userId(random.nextInt(10) + 1L)
                        .build(),
                Device.builder()
                        .name("Basement Lock")
                        .type(DeviceType.LOCK)
                        .location("Basement")
                        .userId(random.nextInt(10) + 1L)
                        .build(),
                Device.builder()
                        .name("Side Door Doorbell")
                        .type(DeviceType.DOORBELL)
                        .location("Side Door")
                        .userId(random.nextInt(10) + 1L)
                        .build(),
                Device.builder()
                        .name("Office Thermostat")
                        .type(DeviceType.THERMOSTAT)
                        .location("Office")
                        .userId(random.nextInt(10) + 1L)
                        .build(),
                Device.builder()
                        .name("Garden Light")
                        .type(DeviceType.LIGHT)
                        .location("Garden")
                        .userId(random.nextInt(10) + 1L)
                        .build());

        repository.saveAll(devices);
        log.info("Devices initialized successfully.");
    }
}
