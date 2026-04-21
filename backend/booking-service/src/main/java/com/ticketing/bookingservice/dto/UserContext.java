package com.ticketing.bookingservice.dto;

public record UserContext(String userId, String userEmail, String userPhone, String role) {
    public boolean isAdmin() {
        return role != null && "ADMIN".equalsIgnoreCase(role);
    }
}
