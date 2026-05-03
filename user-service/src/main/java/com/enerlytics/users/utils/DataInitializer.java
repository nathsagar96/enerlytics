package com.enerlytics.users.utils;

import com.enerlytics.users.entities.User;
import com.enerlytics.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository repository;

    @Override
    public void run(String @NonNull ... args) {
        if (repository.count() == 0) {
            initializeUsers();
        }
    }

    private void initializeUsers() {
        log.info("Initializing users...");

        String[] firstNames = {
            "John", "Jane", "Michael", "Emily", "David", "Sarah", "Robert", "Jennifer", "William", "Lisa"
        };

        String[] lastNames = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Garcia", "Rodriguez", "Wilson"
        };

        String[] addresses = {
            "123 Main St, New York, NY",
            "456 Oak Ave, Los Angeles, CA",
            "789 Pine Rd, Chicago, IL",
            "321 Elm Blvd, Houston, TX",
            "654 Cedar Ln, Phoenix, AZ",
            "987 Birch Dr, Philadelphia, PA",
            "135 Maple St, San Antonio, TX",
            "246 Spruce Ave, San Diego, CA",
            "369 Willow Rd, Dallas, TX",
            "482 Aspen Ct, San Jose, CA"
        };

        for (int i = 0; i < 10; i++) {
            User user = User.builder()
                    .firstName(firstNames[i])
                    .lastName(lastNames[i])
                    .email(firstNames[i].toLowerCase() + "." + lastNames[i].toLowerCase() + "@example.com")
                    .address(addresses[i])
                    .alerting(i % 2 == 0)
                    .energyAlertingThreshold(50.0 + (i * 10.0))
                    .build();
            repository.save(user);
        }

        log.info("Users initialized successfully");
    }
}
