package com.enerlytics.users.controllers;

import com.enerlytics.users.dtos.requests.UserRequest;
import com.enerlytics.users.dtos.responses.PageResponse;
import com.enerlytics.users.dtos.responses.UserResponse;
import com.enerlytics.users.services.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    PageResponse<UserResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize) {
        return service.getAllUsers(pageNumber, pageSize);
    }

    @GetMapping("/batch")
    @ResponseStatus(HttpStatus.OK)
    List<UserResponse> getUsersByIds(@RequestParam Set<UUID> ids) {
        return service.getUsersByIds(ids);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserResponse createUser(@Valid @RequestBody UserRequest request) {
        return service.createUser(request);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    UserResponse getUser(@PathVariable UUID id) {
        return service.getUserById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    UserResponse updateUser(@PathVariable UUID id, @Valid @RequestBody UserRequest request) {
        return service.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteUser(@PathVariable UUID id) {
        service.deleteUser(id);
    }
}
