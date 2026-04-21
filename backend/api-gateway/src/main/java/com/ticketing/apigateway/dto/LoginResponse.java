package com.ticketing.apigateway.dto;

import java.time.OffsetDateTime;

public record LoginResponse(
        String accessToken,
        String tokenType,
        OffsetDateTime expiresAt,
        String userId,
        String email,
        String phone,
        String role
) {
}
