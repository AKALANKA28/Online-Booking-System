package com.ticketing.apigateway.controller;

import com.ticketing.apigateway.dto.LoginRequest;
import com.ticketing.apigateway.dto.LoginResponse;
import com.ticketing.apigateway.dto.UserValidationResponse;
import com.ticketing.apigateway.service.JwtService;
import com.ticketing.apigateway.service.UserAuthClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserAuthClient userAuthClient;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    @Test
    void loginReturnsBadRequestWhenUsernameMissing() {
        ResponseEntity<LoginResponse> response = authController.login(new LoginRequest("", "pw")).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void loginReturnsUnauthorizedWhenCredentialsInvalid() {
        UserValidationResponse validation = new UserValidationResponse(
                "u-1", "alice", "alice@example.com", "+123456789", "CUSTOMER", false);
        when(userAuthClient.validateCredentials("alice", "wrong")).thenReturn(Mono.just(validation));

        ResponseEntity<LoginResponse> response = authController.login(new LoginRequest("alice", "wrong")).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void loginReturnsUnauthorizedWhenUserServiceReturnsEmpty() {
        when(userAuthClient.validateCredentials("alice", "pw")).thenReturn(Mono.empty());

        ResponseEntity<LoginResponse> response = authController.login(new LoginRequest("alice", "pw")).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void loginReturnsServiceUnavailableWhenUserServiceErrors() {
        when(userAuthClient.validateCredentials(anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("user-service down")));

        ResponseEntity<LoginResponse> response = authController.login(new LoginRequest("alice", "pw")).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void loginReturnsTokenWhenCredentialsValid() {
        OffsetDateTime expiry = OffsetDateTime.now().plusHours(8);
        UserValidationResponse validation = new UserValidationResponse(
                "u-1", "alice", "alice@example.com", "+123456789", "CUSTOMER", true);

        when(userAuthClient.validateCredentials("alice", "pw")).thenReturn(Mono.just(validation));
        when(jwtService.generateToken("u-1", "alice", "alice@example.com", "CUSTOMER", "+123456789"))
                .thenReturn("jwt-token");
        when(jwtService.getExpiry("jwt-token")).thenReturn(expiry);

        ResponseEntity<LoginResponse> response = authController.login(new LoginRequest("alice", "pw")).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isEqualTo("jwt-token");
        assertThat(response.getBody().expiresAt()).isEqualTo(expiry);
        assertThat(response.getBody().email()).isEqualTo("alice@example.com");
    }
}
