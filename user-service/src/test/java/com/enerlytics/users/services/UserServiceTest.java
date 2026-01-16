package com.enerlytics.users.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.enerlytics.users.dtos.requests.UserRequest;
import com.enerlytics.users.dtos.responses.PageResponse;
import com.enerlytics.users.dtos.responses.UserResponse;
import com.enerlytics.users.entities.User;
import com.enerlytics.users.exceptions.UserNotFoundException;
import com.enerlytics.users.mappers.UserMapper;
import com.enerlytics.users.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Nested
    @DisplayName("Get All Users")
    class GetAllUsers {

        @Test
        @DisplayName("Should return a page of user responses when users exist")
        void shouldReturnPageOfUserResponsesWhenUsersExist() {
            // Arrange
            int pageNumber = 0;
            int size = 10;
            Pageable expectedPageable = PageRequest.of(pageNumber, size);
            List<User> users = List.of(new User(), new User());
            Page<User> userPage = new PageImpl<>(users);
            PageResponse<UserResponse> expectedResponse = new PageResponse<>(List.of(), 0, 10, 0, 0);

            when(repository.findAll(expectedPageable)).thenReturn(userPage);
            when(mapper.toPageResponse(userPage)).thenReturn(expectedResponse);

            // Act
            PageResponse<UserResponse> result = userService.getAllUsers(pageNumber, size);

            // Assert
            verify(repository, times(1)).findAll(pageableCaptor.capture());
            assertEquals(expectedPageable, pageableCaptor.getValue());
            verify(mapper, times(1)).toPageResponse(userPage);
            assertEquals(expectedResponse, result);
        }

        @Test
        @DisplayName("Should return empty page when no users exist")
        void shouldReturnEmptyPageWhenNoUsersExist() {
            // Arrange
            int pageNumber = 100;
            int size = 5;
            Pageable expectedPageable = PageRequest.of(pageNumber, size);
            Page<User> emptyPage = new PageImpl<>(List.of());
            PageResponse<UserResponse> expectedResponse = new PageResponse<>(List.of(), 100, 5, 0, 0);

            when(repository.findAll(expectedPageable)).thenReturn(emptyPage);
            when(mapper.toPageResponse(emptyPage)).thenReturn(expectedResponse);

            // Act
            PageResponse<UserResponse> result = userService.getAllUsers(pageNumber, size);

            // Assert
            verify(repository, times(1)).findAll(pageableCaptor.capture());
            assertEquals(expectedPageable, pageableCaptor.getValue());
            verify(mapper, times(1)).toPageResponse(emptyPage);
            assertEquals(expectedResponse, result);
        }

        @Test
        @DisplayName("Should return single user page when pageSize is 1")
        void shouldReturnSingleUserPageWhenSizeIsOne() {
            // Arrange
            int pageNumber = 0;
            int size = 1;
            Pageable expectedPageable = PageRequest.of(pageNumber, size);
            List<User> users = List.of(new User());
            Page<User> userPage = new PageImpl<>(users);
            PageResponse<UserResponse> expectedResponse = new PageResponse<>(List.of(), 0, 1, 0, 1);

            when(repository.findAll(expectedPageable)).thenReturn(userPage);
            when(mapper.toPageResponse(userPage)).thenReturn(expectedResponse);

            // Act
            PageResponse<UserResponse> result = userService.getAllUsers(pageNumber, size);

            // Assert
            verify(repository, times(1)).findAll(pageableCaptor.capture());
            assertEquals(expectedPageable, pageableCaptor.getValue());
            verify(mapper, times(1)).toPageResponse(userPage);
            assertEquals(expectedResponse, result);
        }
    }

    @Nested
    @DisplayName("Get Users By IDs (Batch)")
    class GetUsersByIds {
        @Test
        @DisplayName("Should return a list of user responses when user IDs exist")
        void shouldReturnListOfUserResponsesWhenUserIdsExist() {
            // Arrange
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            java.util.Set<UUID> ids = java.util.Set.of(id1, id2);

            User user1 = new User();
            user1.setId(id1);
            User user2 = new User();
            user2.setId(id2);
            List<User> users = List.of(user1, user2);

            UserResponse response1 =
                    new UserResponse(id1, "John", "Doe", "john@example.com", "123 Main St", true, 100.0);
            UserResponse response2 =
                    new UserResponse(id2, "Jane", "Smith", "jane@example.com", "456 Oak Ave", false, 50.0);

            when(repository.findAllById(ids)).thenReturn(users);
            when(mapper.toResponse(user1)).thenReturn(response1);
            when(mapper.toResponse(user2)).thenReturn(response2);

            // Act
            List<UserResponse> result = userService.getUsersByIds(ids);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.contains(response1));
            assertTrue(result.contains(response2));
            verify(repository, times(1)).findAllById(ids);
            verify(mapper, times(2)).toResponse(any());
        }

        @Test
        @DisplayName("Should return empty list when IDs are null or empty")
        void shouldReturnEmptyListWhenIdsAreNullOrEmpty() {
            // Act & Assert
            assertTrue(userService.getUsersByIds(null).isEmpty());
            assertTrue(userService.getUsersByIds(java.util.Set.of()).isEmpty());
            verify(repository, never()).findAllById(any());
        }
    }

    @Nested
    @DisplayName("Create User")
    class CreateUser {

        @Test
        @DisplayName("Should create and return user response when valid request is provided")
        void shouldCreateAndReturnUserResponseWhenValidRequestIsProvided() {
            // Arrange
            UserRequest request = new UserRequest("John", "Doe", "john@example.com", "123 Main St", true, 100.0);
            User user = new User();
            User savedUser = new User();
            savedUser.setId(UUID.randomUUID());
            UserResponse expectedResponse =
                    new UserResponse(UUID.randomUUID(), "John", "Doe", "john@example.com", "123 Main St", true, 100.0);

            when(mapper.toEntity(request)).thenReturn(user);
            when(repository.save(user)).thenReturn(savedUser);
            when(mapper.toResponse(savedUser)).thenReturn(expectedResponse);

            // Act
            UserResponse result = userService.createUser(request);

            // Assert
            verify(mapper, times(1)).toEntity(request);
            verify(repository, times(1)).save(user);
            verify(mapper, times(1)).toResponse(savedUser);
            assertEquals(expectedResponse, result);
        }

        @Test
        @DisplayName("Should create user with all fields populated")
        void shouldCreateUserWithAllFieldsPopulated() {
            // Arrange
            UserRequest request = new UserRequest("Jane", "Smith", "jane@example.com", "456 Oak Ave", false, 50.0);
            User user = new User();
            User savedUser = new User();
            savedUser.setId(UUID.randomUUID());
            UserResponse expectedResponse = new UserResponse(
                    UUID.randomUUID(), "Jane", "Smith", "jane@example.com", "456 Oak Ave", false, 50.0);

            when(mapper.toEntity(request)).thenReturn(user);
            when(repository.save(user)).thenReturn(savedUser);
            when(mapper.toResponse(savedUser)).thenReturn(expectedResponse);

            // Act
            UserResponse result = userService.createUser(request);

            // Assert
            verify(mapper, times(1)).toEntity(request);
            verify(repository, times(1)).save(user);
            verify(mapper, times(1)).toResponse(savedUser);
            assertEquals(expectedResponse, result);
        }
    }

    @Nested
    @DisplayName("Get User By ID")
    class GetUserById {

        @Test
        @DisplayName("Should return user response when user is found")
        void shouldReturnUserResponseWhenUserIsFound() {
            // Arrange
            UUID id = UUID.randomUUID();
            User user = new User();
            user.setId(id);
            UserResponse expectedResponse =
                    new UserResponse(id, "John", "Doe", "john@example.com", "123 Main St", true, 100.0);

            when(repository.findById(id)).thenReturn(Optional.of(user));
            when(mapper.toResponse(user)).thenReturn(expectedResponse);

            // Act
            UserResponse result = userService.getUserById(id);

            // Assert
            verify(repository, times(1)).findById(id);
            verify(mapper, times(1)).toResponse(user);
            assertEquals(expectedResponse, result);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user is not found")
        void shouldThrowUserNotFoundExceptionWhenUserIsNotFound() {
            // Arrange
            UUID id = UUID.randomUUID();

            when(repository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            UserNotFoundException exception =
                    assertThrows(UserNotFoundException.class, () -> userService.getUserById(id));

            assertEquals("User not found with ID: " + id, exception.getMessage());
            verify(repository, times(1)).findById(id);
            verify(mapper, never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("Update User")
    class UpdateUser {

        @Test
        @DisplayName("Should update and return user response when user exists")
        void shouldUpdateAndReturnUserResponseWhenUserExists() {
            // Arrange
            UUID id = UUID.randomUUID();
            UserRequest request = new UserRequest("Updated", "User", "updated@example.com", "789 Pine Rd", true, 200.0);
            User existingUser = new User();
            existingUser.setId(id);
            User updatedUser = new User();
            updatedUser.setId(id);
            UserResponse expectedResponse =
                    new UserResponse(id, "Updated", "User", "updated@example.com", "789 Pine Rd", true, 200.0);

            when(repository.findById(id)).thenReturn(Optional.of(existingUser));
            when(repository.save(existingUser)).thenReturn(updatedUser);
            when(mapper.toResponse(updatedUser)).thenReturn(expectedResponse);

            // Act
            UserResponse result = userService.updateUser(id, request);

            // Assert
            verify(repository, times(1)).findById(id);
            verify(repository, times(1)).save(existingUser);
            verify(mapper, times(1)).toResponse(updatedUser);
            assertEquals(expectedResponse, result);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user to update is not found")
        void shouldThrowUserNotFoundExceptionWhenUserToUpdateIsNotFound() {
            // Arrange
            UUID id = UUID.randomUUID();
            UserRequest request = new UserRequest("Updated", "User", "updated@example.com", "789 Pine Rd", true, 200.0);

            when(repository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            UserNotFoundException exception =
                    assertThrows(UserNotFoundException.class, () -> userService.updateUser(id, request));

            assertEquals("User not found with ID: " + id, exception.getMessage());
            verify(repository, times(1)).findById(id);
            verify(repository, never()).save(any());
            verify(mapper, never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("Delete User")
    class DeleteUser {

        @Test
        @DisplayName("Should delete user when user exists")
        void shouldDeleteUserWhenUserExists() {
            // Arrange
            UUID id = UUID.randomUUID();

            when(repository.existsById(id)).thenReturn(true);

            // Act
            userService.deleteUser(id);

            // Assert
            verify(repository, times(1)).existsById(id);
            verify(repository, times(1)).deleteById(id);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user to delete is not found")
        void shouldThrowUserNotFoundExceptionWhenUserToDeleteIsNotFound() {
            // Arrange
            UUID id = UUID.randomUUID();

            when(repository.existsById(id)).thenReturn(false);

            // Act & Assert
            UserNotFoundException exception =
                    assertThrows(UserNotFoundException.class, () -> userService.deleteUser(id));

            assertEquals("User not found with ID: " + id, exception.getMessage());
            verify(repository, times(1)).existsById(id);
            verify(repository, never()).deleteById(id);
        }
    }
}
