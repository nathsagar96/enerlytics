package com.enerlytics.users.utils;

import com.enerlytics.users.entities.User;
import com.enerlytics.users.repositories.UserRepository;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.init-data", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository repository;

    private static final List<String> FIRST_NAMES =
            List.of("John", "Jane", "Michael", "Emily", "David", "Sarah", "Robert", "Jennifer", "William", "Lisa");
    private static final List<String> LAST_NAMES = List.of(
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Garcia", "Rodriguez", "Wilson");
    private static final List<String> ADDRESSES = List.of(
            "123 Main St, New York, NY",
            "456 Oak Ave, Los Angeles, CA",
            "789 Pine Rd, Chicago, IL",
            "321 Elm Blvd, Houston, TX",
            "654 Cedar Ln, Phoenix, AZ",
            "987 Birch Dr, Philadelphia, PA",
            "135 Maple St, San Antonio, TX",
            "246 Spruce Ave, San Diego, CA",
            "369 Willow Rd, Dallas, TX",
            "482 Aspen Ct, San Jose, CA");

    @Override
    @Transactional
    public void run(String @NonNull ... args) {
        if (repository.count() == 0) {
            initializeUsers();
        } else {
            log.info("Data initialization skipped: users already exist in the database.");
        }
    }

    private void initializeUsers() {
        log.info("Starting user data initialization...");

        List<User> users = IntStream.range(0, FIRST_NAMES.size())
                .mapToObj(i -> User.builder()
                        .firstName(FIRST_NAMES.get(i))
                        .lastName(LAST_NAMES.get(i))
                        .email(FIRST_NAMES.get(i).toLowerCase() + "."
                                + LAST_NAMES.get(i).toLowerCase() + "@example.com")
                        .address(ADDRESSES.get(i))
                        .alerting(i % 2 == 0)
                        .energyAlertingThreshold(50.0 + (i * 10.0))
                        .build())
                .toList();

        repository.saveAll(users);
        log.info("Successfully initialized {} users.", users.size());
    }
}
