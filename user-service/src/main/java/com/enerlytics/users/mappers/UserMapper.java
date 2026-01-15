package com.enerlytics.users.mappers;

import com.enerlytics.users.dtos.requests.UserRequest;
import com.enerlytics.users.dtos.responses.PageResponse;
import com.enerlytics.users.dtos.responses.UserResponse;
import com.enerlytics.users.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserRequest request) {
        return User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .address(request.address())
                .alerting(request.alerting())
                .energyAlertingThreshold(request.energyAlertingThreshold())
                .build();
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getAddress(),
                user.isAlerting(),
                user.getEnergyAlertingThreshold());
    }

    public PageResponse<UserResponse> toPageResponse(Page<User> page) {
        return new PageResponse<>(
                page.stream().map(this::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getNumberOfElements());
    }
}
