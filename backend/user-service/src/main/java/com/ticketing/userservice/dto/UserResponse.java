package com.ticketing.userservice.dto;

import com.ticketing.userservice.entity.User;
import com.ticketing.userservice.entity.UserRole;

import java.time.OffsetDateTime;

public record UserResponse(
        String userId,
        String username,
        String email,
        UserRole role,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}