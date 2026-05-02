package com.enerlytics.users.controllers;

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

import com.enerlytics.users.dtos.requests.CreateUserRequest;
import com.enerlytics.users.dtos.requests.UpdateUserRequest;
import com.enerlytics.users.dtos.responses.UserResponse;
import com.enerlytics.users.exceptions.ResourceNotFoundException;
import com.enerlytics.users.services.UserService;
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
@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("Should create a user successfully and return 201 Created")
    void createUser_Success() throws Exception {
        // Arrange
        CreateUserRequest request =
                new CreateUserRequest("John", "Doe", "john.doe@example.com", "123 Main St", true, 100.0);

        UserResponse response = new UserResponse(1L, "John", "Doe", "john.doe@example.com", "123 Main St", true, 100.0);

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "firstName": "John",
                                        "lastName": "Doe",
                                        "email": "john.doe@example.com",
                                        "address": "123 Main St",
                                        "alerting": true,
                                        "energyAlertingThreshold": 100.0
                                    }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.address").value("123 Main St"))
                .andExpect(jsonPath("$.alerting").value(true))
                .andExpect(jsonPath("$.energyAlertingThreshold").value(100.0));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when required fields are missing")
    void createUser_BadRequest_MissingFields() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "lastName": "Doe",
                                        "email": "john.doe@example.com",
                                        "address": "123 Main St",
                                        "alerting": true,
                                        "energyAlertingThreshold": 100.0
                                    }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors").isMap());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when email format is invalid")
    void createUser_BadRequest_InvalidEmail() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "firstName": "John",
                                        "lastName": "Doe",
                                        "email": "invalid_email",
                                        "address": "123 Main St",
                                        "alerting": true,
                                        "energyAlertingThreshold": 100.0
                                    }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors").isMap());
    }

    @Test
    @DisplayName("Should retrieve a user successfully and return 200 OK")
    void getUserById_Success() throws Exception {
        // Arrange
        UserResponse response = new UserResponse(1L, "John", "Doe", "john.doe@example.com", "123 Main St", true, 100.0);

        when(userService.getUserById(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.address").value("123 Main St"))
                .andExpect(jsonPath("$.alerting").value(true))
                .andExpect(jsonPath("$.energyAlertingThreshold").value(100.0));
    }

    @Test
    @DisplayName("Should return 404 Not Found when the user does not exist")
    void getUserById_NotFound() throws Exception {
        // Arrange
        when(userService.getUserById(99L)).thenThrow(new ResourceNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/99").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("User not found"));
    }

    @Test
    @DisplayName("Should retrieve all users successfully and return 200 OK")
    void getAllUsers_Success() throws Exception {
        // Arrange
        List<UserResponse> userList = List.of(
                new UserResponse(1L, "John", "Doe", "john.doe@example.com", "123 Main St", true, 100.0),
                new UserResponse(2L, "Jane", "Smith", "jane.smith@example.com", "456 Elm St", false, 200.0));

        when(userService.getAllUsers()).thenReturn(userList);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].firstName").value("Jane"))
                .andExpect(jsonPath("$[1].lastName").value("Smith"))
                .andExpect(jsonPath("$[1].email").value("jane.smith@example.com"));
    }

    @Test
    @DisplayName("Should return an empty list when no users exist and return 200 OK")
    void getAllUsers_EmptyList() throws Exception {
        // Arrange
        when(userService.getAllUsers()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/users").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Should update a user successfully and return 200 OK")
    void updateUser_Success() throws Exception {
        // Arrange
        UpdateUserRequest request =
                new UpdateUserRequest("Jane", "Smith", "jane.smith@example.com", "456 Elm St", false, 200.0);
        UserResponse response =
                new UserResponse(1L, "Jane", "Smith", "jane.smith@example.com", "456 Elm St", false, 200.0);

        when(userService.updateUser(1L, request)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "firstName": "Jane",
                                        "lastName": "Smith",
                                        "email": "jane.smith@example.com",
                                        "address": "456 Elm St",
                                        "alerting": false,
                                        "energyAlertingThreshold": 200.0
                                    }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"))
                .andExpect(jsonPath("$.address").value("456 Elm St"))
                .andExpect(jsonPath("$.alerting").value(false))
                .andExpect(jsonPath("$.energyAlertingThreshold").value(200.0));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when email format is invalid for update")
    void updateUser_BadRequest_InvalidEmail() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "firstName": "Jane",
                                        "lastName": "Smith",
                                        "email": "invalid_email",
                                        "address": "456 Elm St",
                                        "alerting": false,
                                        "energyAlertingThreshold": 200.0
                                    }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors").isMap());
    }

    @Test
    @DisplayName("Should return 404 Not Found when updating a user that does not exist")
    void updateUser_NotFound() throws Exception {
        // Arrange
        UpdateUserRequest request =
                new UpdateUserRequest("Jane", "Smith", "jane.smith@example.com", "456 Elm St", false, 200.0);

        when(userService.updateUser(99L, request)).thenThrow(new ResourceNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "firstName": "Jane",
                                        "lastName": "Smith",
                                        "email": "jane.smith@example.com",
                                        "address": "456 Elm St",
                                        "alerting": false,
                                        "energyAlertingThreshold": 200.0
                                    }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("User not found"));
    }

    @Test
    @DisplayName("Should delete a user successfully and return 204 No Content")
    void deleteUser_Success() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 404 Not Found when deleting a user that does not exist")
    void deleteUser_NotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("User not found"))
                .when(userService)
                .deleteUser(99L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/99").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("User not found"));
    }
}
