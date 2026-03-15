package com.ticketing.eventservice.util;

import com.ticketing.eventservice.exception.ForbiddenException;

public final class GatewaySecurityGuards {

    private GatewaySecurityGuards() {
    }

    public static void requireAdmin(String roleHeader) {
        if (roleHeader == null || !"ADMIN".equalsIgnoreCase(roleHeader)) {
            throw new ForbiddenException("Admin privileges are required");
        }
    }
}
