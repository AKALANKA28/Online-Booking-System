package com.ticketing.apigateway.dto;

public record UserValidationResponse(
        String userId,
        String username,
        String email,
        String role,
        boolean valid
) {
}
