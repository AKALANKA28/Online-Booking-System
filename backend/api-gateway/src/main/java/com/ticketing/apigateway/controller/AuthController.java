package com.ticketing.apigateway.controller;

import com.ticketing.apigateway.dto.LoginRequest;
import com.ticketing.apigateway.dto.LoginResponse;
import com.ticketing.apigateway.dto.UserResponse;
import com.ticketing.apigateway.service.DemoUserService;
import com.ticketing.apigateway.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final DemoUserService demoUserService;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "Issue a demo JWT for a known username and password")
    public Mono<ResponseEntity<LoginResponse>> login(@RequestBody LoginRequest request) {
        if (!StringUtils.hasText(request.username()) || !StringUtils.hasText(request.password())) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return Mono.justOrEmpty(demoUserService.authenticate(request.username(), request.password()))
                .map(user -> {
                    String token = jwtService.generateToken(user);
                    LoginResponse response = new LoginResponse(
                            token,
                            "Bearer",
                            jwtService.getExpiry(token),
                            user.userId(),
                            user.email(),
                            user.role()
                    );
                    return ResponseEntity.ok(response);
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @GetMapping("/users")
    @Operation(summary = "List the available demo users")
    public Mono<List<UserResponse>> users() {
        return Mono.just(demoUserService.getUsers());
    }
}
