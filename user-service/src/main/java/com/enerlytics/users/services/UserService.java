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

    private final UserRepository repository;
    private final UserMapper mapper;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.email());
        validateEmailForCreate(request.email());
        User user = mapper.toEntity(request);
        User savedUser = repository.save(user);
        log.debug("User saved with id: {}", savedUser.getId());
        return mapper.toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        return mapper.toResponse(findUserById(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user with id: {}", id);
        User user = findUserById(id);
        if (request.email() != null) {
            validateEmailForUpdate(request.email(), id);
        }
        mapper.updateEntity(user, request);
        User updatedUser = repository.save(user);
        log.debug("User updated with id: {}", updatedUser.getId());
        return mapper.toResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        User user = findUserById(id);
        repository.delete(user);
    }

    private User findUserById(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private void validateEmailForCreate(String email) {
        if (repository.existsByEmail(email)) {
            throw new DuplicateResourceException("User with email already exists: " + email);
        }
    }

    private void validateEmailForUpdate(String email, Long id) {
        if (repository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateResourceException("User with email already exists: " + email);
        }
    }
}
