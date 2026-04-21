package com.ticketing.userservice.service;

import com.ticketing.userservice.dto.InternaluserValidationResponse;
import com.ticketing.userservice.dto.RegisterRequest;
import com.ticketing.userservice.dto.UpdateProfileRequest;
import com.ticketing.userservice.entity.User;
import com.ticketing.userservice.entity.UserRole;
import com.ticketing.userservice.exception.ConflictException;
import com.ticketing.userservice.exception.Notfoundexception;
import com.ticketing.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserApplicationService userApplicationService;

    @Test
    void registerThrowsConflictWhenUsernameExists() {
        RegisterRequest request = new RegisterRequest("alice", "alice@example.com", "pass", null);
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> userApplicationService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Username already taken");
    }

    @Test
    void validateCredentialsReturnsValidForMatchingPassword() {
        User user = User.builder()
                .userId("u-1")
                .username("bob")
                .passwordHash("hashed")
                .email("bob@example.com")
                .role(UserRole.CUSTOMER)
                .active(true)
                .build();

        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);

        InternaluserValidationResponse response = userApplicationService.validateCredentials(
                new com.ticketing.userservice.dto.ValidateCredentialsRequest("bob", "pass")
        );

        assertThat(response.valid()).isTrue();
        assertThat(response.userId()).isEqualTo("u-1");
    }

    @Test
    void getByUserIdThrowsNotFoundWhenMissing() {
        when(userRepository.findByUserId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userApplicationService.getByUserId("missing"))
                .isInstanceOf(Notfoundexception.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateProfileUpdatesEmailAndPassword() {
        User existing = User.builder()
                .userId("u-2")
                .username("charlie")
                .passwordHash("oldhash")
                .email("charlie@example.com")
                .role(UserRole.CUSTOMER)
                .active(true)
                .build();

        when(userRepository.findByUserId("u-2")).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("newpass")).thenReturn("newhash");
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        com.ticketing.userservice.dto.UserResponse updated = userApplicationService.updateProfile(
                "u-2",
                new UpdateProfileRequest("new@example.com", "newpass", "+111")
        );

        assertThat(updated.email()).isEqualTo("new@example.com");
        assertThat(updated.phone()).isEqualTo("+111");
    }
}
