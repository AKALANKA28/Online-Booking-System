package com.ticketing.apigateway.controller;

import com.ticketing.apigateway.dto.LoginRequest;
import com.ticketing.apigateway.dto.LoginResponse;
import com.ticketing.apigateway.service.JwtService;
import com.ticketing.apigateway.service.UserAuthClient;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserAuthClient userAuthClient;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "Issue a JWT after validating credentials with user-service")
    public Mono<ResponseEntity<LoginResponse>> login(@RequestBody LoginRequest request) {
        if (!StringUtils.hasText(request.username()) || !StringUtils.hasText(request.password())) {
            return Mono.just(ResponseEntity.<LoginResponse>badRequest().build());
        }

        return userAuthClient.validateCredentials(request.username(), request.password())
                .map(validation -> {
                    if (!validation.valid()) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).<LoginResponse>build();
                    }

                    String token = jwtService.generateToken(
                            validation.userId(),
                            validation.username(),
                            validation.email(),
                            validation.role()
                    );
                    LoginResponse response = new LoginResponse(
                            token,
                            "Bearer",
                            jwtService.getExpiry(token),
                            validation.userId(),
                            validation.email(),
                            validation.role()
                    );
                    return ResponseEntity.ok(response);
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).<LoginResponse>build())
                .onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).<LoginResponse>build());
    }
}
