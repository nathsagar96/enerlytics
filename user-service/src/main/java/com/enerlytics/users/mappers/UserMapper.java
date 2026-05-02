package com.enerlytics.users.mappers;

import com.enerlytics.users.dtos.requests.CreateUserRequest;
import com.enerlytics.users.dtos.requests.UpdateUserRequest;
import com.enerlytics.users.dtos.responses.UserResponse;
import com.enerlytics.users.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(CreateUserRequest request) {
        return User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .address(request.address())
                .alerting(request.alerting())
                .energyAlertingThreshold(request.energyAlertingThreshold())
                .build();
    }

    public void updateEntity(User user, UpdateUserRequest request) {
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.email() != null) {
            user.setEmail(request.email());
        }
        if (request.address() != null) {
            user.setAddress(request.address());
        }
        if (request.alerting() != null) {
            user.setAlerting(request.alerting());
        }
        if (request.energyAlertingThreshold() != null) {
            user.setEnergyAlertingThreshold(request.energyAlertingThreshold());
        }
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
}
