package com.ticketing.userservice.controller;

import com.ticketing.userservice.dto.*;
import com.ticketing.userservice.service.UserApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserApplicationService userApplicationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new customer account")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userApplicationService.register(request);
    }



    
    @GetMapping("/{userId}")
    @Operation(summary = "Get user profile by userId")
    public UserResponse getProfile(
            @RequestHeader(value = "X-User-Id", required = false) String requestingUserId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable String userId) {
        // Users can only view their own profile unless they are ADMIN
        if (!"ADMIN".equalsIgnoreCase(role) && !userId.equals(requestingUserId)) {
            throw new com.ticketing.userservice.exception.Forbiddenexception("Access denied");
        }
        return userApplicationService.getByUserId(userId);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user profile")
    public UserResponse updateProfile(
            @RequestHeader(value = "X-User-Id", required = false) String requestingUserId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable String userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        if (!"ADMIN".equalsIgnoreCase(role) && !userId.equals(requestingUserId)) {
            throw new com.ticketing.userservice.exception.Forbiddenexception("Access denied");
        }
        return userApplicationService.updateProfile(userId, request);
    }
}