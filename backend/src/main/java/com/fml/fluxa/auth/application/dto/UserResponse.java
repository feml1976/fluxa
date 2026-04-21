package com.fml.fluxa.auth.application.dto;

import com.fml.fluxa.auth.domain.model.User;
import com.fml.fluxa.auth.domain.model.UserRole;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        Long groupId,
        boolean isActive
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getGroup() != null ? user.getGroup().getId() : null,
                user.isActive()
        );
    }
}
