package com.enerlytics.users.services;

import com.enerlytics.users.dtos.requests.UserRequest;
import com.enerlytics.users.dtos.responses.PageResponse;
import com.enerlytics.users.dtos.responses.UserResponse;
import com.enerlytics.users.entities.User;
import com.enerlytics.users.exceptions.UserAlreadyExistsException;
import com.enerlytics.users.exceptions.UserNotFoundException;
import com.enerlytics.users.mappers.UserMapper;
import com.enerlytics.users.repositories.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllUsers(int pageNumber, int pageSize) {
        log.debug("Getting all users, pageNumber: {}, pageSize: {}", pageNumber, pageSize);

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        var users = repository.findAll(pageable);

        return mapper.toPageResponse(users);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByIds(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            log.debug("No user IDs provided for batch fetch");
            return Collections.emptyList();
        }

        log.debug("Fetching users in batch for {} ids", ids.size());
        var users = repository.findAllById(ids);

        log.info("Successfully fetched {} users in batch", users.size());
        return users.stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public UserResponse createUser(UserRequest request) {
        log.debug("Creating user");

        if (repository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("User with email " + request.email() + " already exists");
        }

        User user = mapper.toEntity(request);
        User savedUser = repository.save(user);

        log.info("User created with ID: {}", savedUser.getId());
        return mapper.toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        log.debug("Getting user by ID: {}", id);

        User user =
                repository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        return mapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(UUID id, UserRequest request) {
        log.debug("Updating user with ID: {}", id);

        User user =
                repository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setAddress(request.address());
        user.setAlerting(request.alerting());
        user.setEnergyAlertingThreshold(request.energyAlertingThreshold());
        User updatedUser = repository.save(user);

        log.info("User updated with ID: {}", updatedUser.getId());
        return mapper.toResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(UUID id) {
        log.debug("Deleting user with ID: {}", id);

        if (!repository.existsById(id)) {
            throw new UserNotFoundException("User not found with ID: " + id);
        }

        repository.deleteById(id);
        log.info("User deleted with ID: {}", id);
    }
}
