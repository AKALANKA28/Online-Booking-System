package com.ticketing.apigateway.dto;

public record UserValidationResponse(
        String userId,
        String username,
        String email,
        String phone,
        String role,
        boolean valid
) {
}
