package com.enerlytics.users.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enerlytics.users.dtos.requests.CreateUserRequest;
import com.enerlytics.users.dtos.requests.UpdateUserRequest;
import com.enerlytics.users.dtos.responses.UserResponse;
import com.enerlytics.users.exceptions.DuplicateResourceException;
import com.enerlytics.users.exceptions.ResourceNotFoundException;
import com.enerlytics.users.repositories.UserRepository;
import com.enerlytics.users.services.UserService;
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
class UserServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("users")
            .withUsername("postgres")
            .withPassword("password");

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should persist and fetch user")
    void createAndFetchUser() {
        CreateUserRequest createRequest =
                new CreateUserRequest("John", "Doe", "john.tc@example.com", "123 Main St", true, 100.0);

        UserResponse created = userService.createUser(createRequest);
        UserResponse fetched = userService.getUserById(created.id());

        assertNotNull(created.id());
        assertEquals("john.tc@example.com", created.email());
        assertEquals(created, fetched);
    }

    @Test
    @DisplayName("Should enforce unique email constraint through service validation")
    void createUser_duplicateEmail() {
        CreateUserRequest createRequest =
                new CreateUserRequest("Jane", "Doe", "jane.tc@example.com", "456 Elm St", false, 80.0);
        userService.createUser(createRequest);

        DuplicateResourceException exception =
                assertThrows(DuplicateResourceException.class, () -> userService.createUser(createRequest));

        assertEquals("User with email already exists: jane.tc@example.com", exception.getMessage());
    }

    @Test
    @DisplayName("Should fetch user by ID")
    void getUserById() {
        CreateUserRequest createRequest =
                new CreateUserRequest("John", "Doe", "john.tc@example.com", "123 Main St", true, 100.0);
        UserResponse created = userService.createUser(createRequest);

        UserResponse fetched = userService.getUserById(created.id());

        assertNotNull(fetched);
        assertEquals(created.id(), fetched.id());
        assertEquals("john.tc@example.com", fetched.email());
    }

    @Test
    @DisplayName("Should list all users")
    void getAllUsers() {
        CreateUserRequest createRequest1 =
                new CreateUserRequest("John", "Doe", "john.tc@example.com", "123 Main St", true, 100.0);
        CreateUserRequest createRequest2 =
                new CreateUserRequest("Jane", "Doe", "jane.tc@example.com", "456 Elm St", false, 80.0);

        userService.createUser(createRequest1);
        userService.createUser(createRequest2);

        List<UserResponse> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(2, users.size());
    }

    @Test
    @DisplayName("Should update user")
    void updateUser() {
        CreateUserRequest createRequest =
                new CreateUserRequest("John", "Doe", "john.tc@example.com", "123 Main St", true, 100.0);
        UserResponse created = userService.createUser(createRequest);

        UpdateUserRequest updateRequest =
                new UpdateUserRequest("John", "Smith", "john.smith@example.com", "789 Oak St", false, 150.0);
        UserResponse updated = userService.updateUser(created.id(), updateRequest);

        assertNotNull(updated);
        assertEquals(created.id(), updated.id());
        assertEquals("john.smith@example.com", updated.email());
        assertEquals("Smith", updated.lastName());
    }

    @Test
    @DisplayName("Should delete user")
    void deleteUser() {
        CreateUserRequest createRequest =
                new CreateUserRequest("John", "Doe", "john.tc@example.com", "123 Main St", true, 100.0);
        UserResponse created = userService.createUser(createRequest);

        userService.deleteUser(created.id());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(created.id()));
    }
}
