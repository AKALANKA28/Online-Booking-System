package com.ticketing.bookingservice.util;

import com.ticketing.bookingservice.dto.UserContext;
import com.ticketing.bookingservice.exception.ForbiddenException;
import com.ticketing.bookingservice.exception.UnauthorizedException;

public final class UserContextGuards {

    private UserContextGuards() {
    }

    public static UserContext requireAuthenticated(String userId, String userEmail, String userPhone, String role) {
        if (userId == null || userId.isBlank() || userEmail == null || userEmail.isBlank()) {
            throw new UnauthorizedException("Missing authenticated user context from gateway");
        }
        String normalizedPhone = (userPhone == null || userPhone.isBlank() || "null".equalsIgnoreCase(userPhone))
                ? null
                : userPhone.trim();
        return new UserContext(userId, userEmail, normalizedPhone, role);
    }

    public static void requireOwnerOrAdmin(UserContext context, String ownerUserId) {
        if (!context.isAdmin() && !context.userId().equals(ownerUserId)) {
            throw new ForbiddenException("Access denied for booking resource");
        }
    }
}
