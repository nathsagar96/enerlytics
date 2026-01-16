package com.enerlytics.users.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.enerlytics.users.dtos.requests.UserRequest;
import com.enerlytics.users.dtos.responses.PageResponse;
import com.enerlytics.users.dtos.responses.UserResponse;
import com.enerlytics.users.exceptions.UserAlreadyExistsException;
import com.enerlytics.users.exceptions.UserNotFoundException;
import com.enerlytics.users.services.UserService;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    // Helper method to create a sample UserResponse
    private UserResponse createSampleUserResponse(UUID id) {
        return new UserResponse(id, "Test", "User", "test@example.com", "Test Address", true, 100.0);
    }

    // Helper method to create a sample PageResponse
    private PageResponse<UserResponse> createSamplePageResponse() {
        UserResponse user1 = createSampleUserResponse(UUID.randomUUID());
        UserResponse user2 = createSampleUserResponse(UUID.randomUUID());
        List<UserResponse> content = Arrays.asList(user1, user2);
        return new PageResponse<>(content, 0, 10, 1, 2);
    }

    @Nested
    @DisplayName("Get All Users")
    class GetAllUsers {

        @Test
        @DisplayName("Should return a page of user responses when users exist")
        void shouldReturnPageOfUserResponsesWhenUsersExist() throws Exception {
            // Arrange
            PageResponse<UserResponse> mockPageResponse = createSamplePageResponse();
            when(userService.getAllUsers(0, 10)).thenReturn(mockPageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/users")
                            .param("pageNumber", "0")
                            .param("pageSize", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.pageNumber").value(0))
                    .andExpect(jsonPath("$.pageSize").value(10));

            verify(userService, times(1)).getAllUsers(0, 10);
        }

        @Test
        @DisplayName("Should return a page of user responses with custom pagination")
        void shouldReturnPageOfUserResponsesWithCustomPagination() throws Exception {
            // Arrange
            PageResponse<UserResponse> mockPageResponse = new PageResponse<>(
                    List.of(createSampleUserResponse(UUID.randomUUID()), createSampleUserResponse(UUID.randomUUID())),
                    1,
                    1,
                    1,
                    2);
            when(userService.getAllUsers(1, 1)).thenReturn(mockPageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/users")
                            .param("pageNumber", "1")
                            .param("pageSize", "1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.pageNumber").value(1))
                    .andExpect(jsonPath("$.pageSize").value(1));

            verify(userService, times(1)).getAllUsers(1, 1);
        }

        @Test
        @DisplayName("Should return empty page when no users exist")
        void shouldReturnEmptyPageWhenNoUsersExist() throws Exception {
            // Arrange
            PageResponse<UserResponse> emptyPageResponse = new PageResponse<>(List.of(), 0, 10, 0, 0);
            when(userService.getAllUsers(0, 10)).thenReturn(emptyPageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/users")
                            .param("pageNumber", "0")
                            .param("pageSize", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(0))
                    .andExpect(jsonPath("$.numberOfElements").value(0));

            verify(userService, times(1)).getAllUsers(0, 10);
        }
    }

    @Nested
    @DisplayName("Create User")
    class CreateUser {

        @Test
        @DisplayName("Should create and return user response when valid request is provided")
        void shouldCreateAndReturnUserResponseWhenValidRequestIsProvided() throws Exception {
            // Arrange
            UserResponse mockResponse = createSampleUserResponse(UUID.randomUUID());
            when(userService.createUser(any(UserRequest.class))).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(
                            post("/api/v1/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            "{\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"test@example.com\",\"address\":\"Test Address\",\"alerting\":true,\"energyAlertingThreshold\":100.0}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.firstName").value("Test"))
                    .andExpect(jsonPath("$.email").value("test@example.com"));

            verify(userService, times(1)).createUser(any(UserRequest.class));
        }

        @Test
        @DisplayName("Should return bad request when invalid request is provided")
        void shouldReturnBadRequestWhenInvalidRequestIsProvided() throws Exception {
            // Act & Assert
            mockMvc.perform(
                            post("/api/v1/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            "{\"firstName\":\"\",\"lastName\":\"User\",\"email\":\"invalid-email\",\"address\":\"Test Address\",\"alerting\":true,\"energyAlertingThreshold\":100.0}"))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(any(UserRequest.class));
        }

        @Test
        @DisplayName("Should return conflict when user already exists")
        void shouldReturnConflictWhenUserAlreadyExists() throws Exception {
            // Arrange
            when(userService.createUser(any(UserRequest.class)))
                    .thenThrow(new UserAlreadyExistsException("User already exists"));

            // Act & Assert
            mockMvc.perform(
                            post("/api/v1/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            "{\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"test@example.com\",\"address\":\"Test Address\",\"alerting\":true,\"energyAlertingThreshold\":100.0}"))
                    .andExpect(status().isConflict());

            verify(userService, times(1)).createUser(any(UserRequest.class));
        }
    }

    @Nested
    @DisplayName("Batch Fetch Users")
    class GetUsersByIds {
        @Test
        @DisplayName("Should return a list of user responses when users exist for provided IDs")
        void shouldReturnListOfUserResponsesWhenUsersExistForProvidedIds() throws Exception {
            // Arrange
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            UserResponse user1 = createSampleUserResponse(id1);
            UserResponse user2 = createSampleUserResponse(id2);
            List<UserResponse> expectedResponse = List.of(user1, user2);

            when(userService.getUsersByIds(anySet())).thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/users/batch")
                            .param("ids", id1 + "," + id2)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(id1.toString()))
                    .andExpect(jsonPath("$[1].id").value(id2.toString()));

            verify(userService, times(1)).getUsersByIds(anySet());
        }

        @Test
        @DisplayName("Should return empty list when no IDs are provided")
        void shouldReturnEmptyListWhenNoIdsAreProvided() throws Exception {
            // Arrange
            when(userService.getUsersByIds(anySet())).thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(get("/api/v1/users/batch").param("ids", "").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("Get User By ID")
    class GetUserById {

        @Test
        @DisplayName("Should return user response when user is found")
        void shouldReturnUserResponseWhenUserIsFound() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            UserResponse mockResponse = createSampleUserResponse(userId);
            when(userService.getUserById(userId)).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/users/{id}", userId).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId.toString()))
                    .andExpect(jsonPath("$.firstName").value("Test"))
                    .andExpect(jsonPath("$.email").value("test@example.com"));

            verify(userService, times(1)).getUserById(userId);
        }

        @Test
        @DisplayName("Should return not found when user is not found")
        void shouldReturnNotFoundWhenUserIsNotFound() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            when(userService.getUserById(userId)).thenThrow(new UserNotFoundException("User not found"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/users/{id}", userId).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(userService, times(1)).getUserById(userId);
        }
    }

    @Nested
    @DisplayName("Update User")
    class UpdateUser {

        @Test
        @DisplayName("Should update and return user response when user exists")
        void shouldUpdateAndReturnUserResponseWhenUserExists() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            UserResponse mockResponse = createSampleUserResponse(userId);
            when(userService.updateUser(eq(userId), any(UserRequest.class))).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(
                            put("/api/v1/users/{id}", userId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            "{\"firstName\":\"Updated\",\"lastName\":\"User\",\"email\":\"updated@example.com\",\"address\":\"Updated Address\",\"alerting\":false,\"energyAlertingThreshold\":200.0}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId.toString()))
                    .andExpect(jsonPath("$.firstName").value("Test"))
                    .andExpect(jsonPath("$.email").value("test@example.com"));

            verify(userService, times(1)).updateUser(eq(userId), any(UserRequest.class));
        }

        @Test
        @DisplayName("Should return not found when user to update is not found")
        void shouldReturnNotFoundWhenUserToUpdateIsNotFound() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            when(userService.updateUser(eq(userId), any(UserRequest.class)))
                    .thenThrow(new UserNotFoundException("User not found"));

            // Act & Assert
            mockMvc.perform(
                            put("/api/v1/users/{id}", userId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            "{\"firstName\":\"Updated\",\"lastName\":\"User\",\"email\":\"updated@example.com\",\"address\":\"Updated Address\",\"alerting\":false,\"energyAlertingThreshold\":200.0}"))
                    .andExpect(status().isNotFound());

            verify(userService, times(1)).updateUser(eq(userId), any(UserRequest.class));
        }

        @Test
        @DisplayName("Should return bad request when invalid request is provided")
        void shouldReturnBadRequestWhenInvalidRequestIsProvided() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();

            // Act & Assert
            mockMvc.perform(
                            put("/api/v1/users/{id}", userId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            "{\"firstName\":\"\",\"lastName\":\"User\",\"email\":\"invalid-email\",\"address\":\"Updated Address\",\"alerting\":false,\"energyAlertingThreshold\":200.0}"))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).updateUser(any(UUID.class), any(UserRequest.class));
        }
    }

    @Nested
    @DisplayName("Delete User")
    class DeleteUser {

        @Test
        @DisplayName("Should delete user when user exists")
        void shouldDeleteUserWhenUserExists() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            doNothing().when(userService).deleteUser(userId);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/users/{id}", userId).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(userService, times(1)).deleteUser(userId);
        }

        @Test
        @DisplayName("Should return not found when user to delete is not found")
        void shouldReturnNotFoundWhenUserToDeleteIsNotFound() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            doThrow(new UserNotFoundException("User not found with ID: " + userId))
                    .when(userService)
                    .deleteUser(userId);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/users/{id}", userId).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(userService, times(1)).deleteUser(userId);
        }
    }
}
