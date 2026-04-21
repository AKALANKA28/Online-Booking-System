package com.ticketing.userservice.dto;

import com.ticketing.userservice.entity.User;
import com.ticketing.userservice.entity.UserRole;

public record InternaluserValidationResponse(
        String userId,
        String username,
        String email,
        String phone,
        UserRole role,
        boolean valid
) {
    public static InternaluserValidationResponse valid(User user) {
        return new InternaluserValidationResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                true
        );
    }

    public static InternaluserValidationResponse invalid() {
        return new InternaluserValidationResponse(null, null, null, null, null, false);
    }
}