package com.enerlytics.users.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enerlytics.users.dtos.requests.CreateUserRequest;
import com.enerlytics.users.dtos.requests.UpdateUserRequest;
import com.enerlytics.users.dtos.responses.UserResponse;
import com.enerlytics.users.entities.User;
import com.enerlytics.users.exceptions.DuplicateResourceException;
import com.enerlytics.users.exceptions.ResourceNotFoundException;
import com.enerlytics.users.mappers.UserMapper;
import com.enerlytics.users.repositories.UserRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    public UserServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should create a new user successfully")
    void createUser_Successful() {
        // Arrange
        CreateUserRequest request =
                new CreateUserRequest("John", "Doe", "john.doe@example.com", "123 Main St", true, 100.0);

        User userEntity = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .address("123 Main St")
                .alertingEnabled(true)
                .energyAlertingThreshold(100.0)
                .build();

        UserResponse expectedResponse =
                new UserResponse(1L, "John", "Doe", "john.doe@example.com", "123 Main St", true, 100.0);

        when(userMapper.toEntity(request)).thenReturn(userEntity);
        when(userRepository.save(any(User.class))).thenReturn(userEntity);
        when(userMapper.toResponse(userEntity)).thenReturn(expectedResponse);

        // Act
        UserResponse actualResponse = userService.createUser(request);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);

        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toEntity(request);
        verify(userMapper, times(1)).toResponse(userEntity);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already exists")
    void createUser_EmailAlreadyExists() {
        // Arrange
        CreateUserRequest request =
                new CreateUserRequest("John", "Doe", "duplicate@example.com", "123 Main St", true, 100.0);

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> userService.createUser(request),
                "Expected createUser() to throw DuplicateResourceException");

        assertEquals("User with email already exists: duplicate@example.com", exception.getMessage());
        verify(userRepository, times(1)).existsByEmail(request.email());
        verify(userMapper, never()).toEntity(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should return UserResponse when user with given ID exists")
    void getUserById_Successful() {
        // Arrange
        Long userId = 1L;

        User userEntity = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .address("123 Main St")
                .alertingEnabled(true)
                .energyAlertingThreshold(100.0)
                .build();

        UserResponse expectedResponse =
                new UserResponse(userId, "John", "Doe", "john.doe@example.com", "123 Main St", true, 100.0);

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(userEntity));
        when(userMapper.toResponse(userEntity)).thenReturn(expectedResponse);

        // Act
        UserResponse actualResponse = userService.getUserById(userId);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);

        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).toResponse(userEntity);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user with given ID does not exist")
    void getUserById_UserNotFound() {
        // Arrange
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(userId),
                "Expected getUserById() to throw ResourceNotFoundException");

        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should return all users successfully")
    void getAllUsers_Successful() {
        // Arrange
        List<User> users = List.of(
                User.builder()
                        .id(1L)
                        .firstName("John")
                        .lastName("Doe")
                        .email("john.doe@example.com")
                        .address("123 Main St")
                        .alertingEnabled(true)
                        .energyAlertingThreshold(100.0)
                        .build(),
                User.builder()
                        .id(2L)
                        .firstName("Jane")
                        .lastName("Smith")
                        .email("jane.smith@example.com")
                        .address("456 Elm St")
                        .alertingEnabled(false)
                        .energyAlertingThreshold(150.0)
                        .build());

        List<UserResponse> expectedResponses = List.of(
                new UserResponse(1L, "John", "Doe", "john.doe@example.com", "123 Main St", true, 100.0),
                new UserResponse(2L, "Jane", "Smith", "jane.smith@example.com", "456 Elm St", false, 150.0));

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toResponse(users.get(0))).thenReturn(expectedResponses.get(0));
        when(userMapper.toResponse(users.get(1))).thenReturn(expectedResponses.get(1));

        // Act
        List<UserResponse> actualResponses = userService.getAllUsers();

        // Assert
        assertNotNull(actualResponses);
        assertEquals(expectedResponses.size(), actualResponses.size());
        assertEquals(expectedResponses, actualResponses);

        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(2)).toResponse(any(User.class));
    }

    @Test
    @DisplayName("Should return an empty list when no users are available")
    void getAllUsers_EmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of());

        // Act
        List<UserResponse> actualResponses = userService.getAllUsers();

        // Assert
        assertNotNull(actualResponses);
        assertEquals(0, actualResponses.size());

        verify(userRepository, times(1)).findAll();
        verify(userMapper, never()).toResponse(any(User.class));
    }

    @Test
    @DisplayName("Should update user successfully")
    void updateUser_Successful() {
        // Arrange
        Long userId = 1L;
        UpdateUserRequest request =
                new UpdateUserRequest("Johnathan", "Doe", "john.doe@example.com", "456 Elm St", false, 120.0);

        User existingUser = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .address("123 Main St")
                .alertingEnabled(true)
                .energyAlertingThreshold(100.0)
                .build();

        User updatedUser = User.builder()
                .id(userId)
                .firstName("Johnathan")
                .lastName("Doe")
                .email("john.doe@example.com")
                .address("456 Elm St")
                .alertingEnabled(false)
                .energyAlertingThreshold(120.0)
                .build();

        UserResponse expectedResponse =
                new UserResponse(userId, "Johnathan", "Doe", "john.doe@example.com", "456 Elm St", false, 120.0);

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(updatedUser);
        doNothing().when(userMapper).updateEntity(existingUser, request);
        when(userMapper.toResponse(updatedUser)).thenReturn(expectedResponse);

        // Act
        UserResponse actualResponse = userService.updateUser(userId, request);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).updateEntity(existingUser, request);
        verify(userRepository, times(1)).save(existingUser);
        verify(userMapper, times(1)).toResponse(updatedUser);
    }

    @Test
    @DisplayName("Should update user successfully without changing email when email is not provided")
    void updateUser_Successful_NoEmailChange() {
        // Arrange
        Long userId = 1L;
        UpdateUserRequest request = new UpdateUserRequest("Johnathan", "Doe", null, "456 Elm St", false, 120.0);

        User existingUser = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .address("123 Main St")
                .alertingEnabled(true)
                .energyAlertingThreshold(100.0)
                .build();

        User updatedUser = User.builder()
                .id(userId)
                .firstName("Johnathan")
                .lastName("Doe")
                .email("john.doe@example.com")
                .address("456 Elm St")
                .alertingEnabled(false)
                .energyAlertingThreshold(120.0)
                .build();

        UserResponse expectedResponse =
                new UserResponse(userId, "Johnathan", "Doe", "john.doe@example.com", "456 Elm St", false, 120.0);

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(updatedUser);
        doNothing().when(userMapper).updateEntity(existingUser, request);
        when(userMapper.toResponse(updatedUser)).thenReturn(expectedResponse);

        // Act
        UserResponse actualResponse = userService.updateUser(userId, request);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).existsByEmailAndIdNot(any(), any());
        verify(userMapper, times(1)).updateEntity(existingUser, request);
        verify(userRepository, times(1)).save(existingUser);
        verify(userMapper, times(1)).toResponse(updatedUser);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent user")
    void updateUser_UserNotFound() {
        // Arrange
        Long userId = 999L;
        UpdateUserRequest request =
                new UpdateUserRequest("Johnathan", "Doe", "john.doe@example.com", "456 Elm St", false, 120.0);

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateUser(userId, request),
                "Expected updateUser() to throw ResourceNotFoundException");

        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, never()).updateEntity(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already exists during update")
    void updateUser_EmailAlreadyExists() {
        // Arrange
        Long userId = 1L;
        UpdateUserRequest request =
                new UpdateUserRequest("Johnathan", "Doe", "duplicate@example.com", "456 Elm St", false, 120.0);

        User existingUser = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .address("123 Main St")
                .alertingEnabled(true)
                .energyAlertingThreshold(100.0)
                .build();

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(existingUser));
        when(userRepository.existsByEmailAndIdNot(request.email(), userId)).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> userService.updateUser(userId, request),
                "Expected updateUser() to throw DuplicateResourceException");

        assertEquals("User with email already exists: duplicate@example.com", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByEmailAndIdNot(request.email(), userId);
        verify(userMapper, never()).updateEntity(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete user successfully")
    void deleteUser_Successful() {
        // Arrange
        Long userId = 1L;

        User existingUser = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .address("123 Main St")
                .alertingEnabled(true)
                .energyAlertingThreshold(100.0)
                .build();

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(existingUser));
        doNothing().when(userRepository).delete(existingUser);

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).delete(existingUser);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent user")
    void deleteUser_UserNotFound() {
        // Arrange
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.deleteUser(userId),
                "Expected deleteUser() to throw ResourceNotFoundException");

        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).delete(any());
    }
}
