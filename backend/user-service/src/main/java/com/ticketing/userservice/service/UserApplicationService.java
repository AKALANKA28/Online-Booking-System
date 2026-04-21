package com.ticketing.userservice.service;

import com.ticketing.userservice.dto.*;
import com.ticketing.userservice.entity.User;
import com.ticketing.userservice.entity.UserRole;
import com.ticketing.userservice.exception.ConflictException;
import com.ticketing.userservice.exception.Notfoundexception;
import com.ticketing.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered: " + request.email());
        }

        User user = User.builder()
                .userId(UUID.randomUUID().toString())
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .email(request.email())
                .phone(blankToNull(request.phone()))
                .role(UserRole.CUSTOMER)
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getByUserId(String userId) {
        return UserResponse.from(findUserById(userId));
    }

    @Transactional
    public UserResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = findUserById(userId);

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new ConflictException("Email already registered: " + request.email());
            }
            user.setEmail(request.email());
        }

        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        if (request.phone() != null) {
            user.setPhone(blankToNull(request.phone()));
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public InternaluserValidationResponse validateCredentials(ValidateCredentialsRequest request) {
        return userRepository.findByUsername(request.username())
                .filter(user -> user.isActive()
                        && passwordEncoder.matches(request.password(), user.getPasswordHash()))
                .map(InternaluserValidationResponse::valid)
                .orElse(InternaluserValidationResponse.invalid());
    }

    @Transactional
    public UserResponse ensureAdminUser(String username, String email, String rawPassword) {
        Optional<User> byUsername = userRepository.findByUsername(username);
        if (byUsername.isPresent()) {
            User existing = byUsername.get();
            if (existing.getRole() != UserRole.ADMIN || !existing.isActive()) {
                existing.setRole(UserRole.ADMIN);
                existing.setActive(true);
                return UserResponse.from(userRepository.save(existing));
            }
            return UserResponse.from(existing);
        }

        Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            User existing = byEmail.get();
            if (existing.getRole() != UserRole.ADMIN || !existing.isActive()) {
                existing.setRole(UserRole.ADMIN);
                existing.setActive(true);
                return UserResponse.from(userRepository.save(existing));
            }
            return UserResponse.from(existing);
        }

        User admin = User.builder()
                .userId(UUID.randomUUID().toString())
                .username(username)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .email(email)
                .role(UserRole.ADMIN)
                .active(true)
                .build();

        return UserResponse.from(userRepository.save(admin));
    }

    private User findUserById(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new Notfoundexception("User not found: " + userId));
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}