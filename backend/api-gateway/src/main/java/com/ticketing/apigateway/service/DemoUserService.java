package com.ticketing.apigateway.service;

import com.ticketing.apigateway.dto.UserResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DemoUserService {

    private final Map<String, DemoUser> users = Map.of(
            "admin", new DemoUser("admin", "admin123", "admin-1", "admin@ticketing.local", "ADMIN"),
            "customer", new DemoUser("customer", "customer123", "user-1", "customer@ticketing.local", "CUSTOMER"),
            "user2", new DemoUser("user2", "user2123", "user-2", "user2@ticketing.local", "CUSTOMER")
    );

    public Optional<DemoUser> authenticate(String username, String password) {
        DemoUser user = users.get(username);
        if (user == null || !user.password().equals(password)) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    public List<UserResponse> getUsers() {
        return users.values().stream()
                .map(user -> new UserResponse(user.username(), user.userId(), user.email(), user.role()))
                .toList();
    }

    public record DemoUser(String username, String password, String userId, String email, String role) {
    }
}
