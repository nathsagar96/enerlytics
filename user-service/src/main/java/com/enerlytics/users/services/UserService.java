package com.enerlytics.users.services;

import com.enerlytics.users.dtos.requests.CreateUserRequest;
import com.enerlytics.users.dtos.requests.UpdateUserRequest;
import com.enerlytics.users.dtos.responses.UserResponse;
import com.enerlytics.users.entities.User;
import com.enerlytics.users.exceptions.DuplicateResourceException;
import com.enerlytics.users.exceptions.ResourceNotFoundException;
import com.enerlytics.users.mappers.UserMapper;
import com.enerlytics.users.repositories.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.email());
        validateEmailForCreate(request.email());
        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);
        log.debug("User saved with id: {}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        return userMapper.toResponse(findUserById(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user with id: {}", id);
        User user = findUserById(id);
        if (request.email() != null) {
            validateEmailForUpdate(request.email(), id);
        }
        userMapper.updateEntity(user, request);
        User updatedUser = userRepository.save(user);
        log.debug("User updated with id: {}", updatedUser.getId());
        return userMapper.toResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        User user = findUserById(id);
        userRepository.delete(user);
    }

    private User findUserById(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private void validateEmailForCreate(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User with email already exists: " + email);
        }
    }

    private void validateEmailForUpdate(String email, Long id) {
        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateResourceException("User with email already exists: " + email);
        }
    }
}
