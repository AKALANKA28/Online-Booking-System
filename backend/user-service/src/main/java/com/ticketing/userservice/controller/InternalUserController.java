package com.ticketing.userservice.controller;

import com.ticketing.userservice.dto.InternaluserValidationResponse;
import com.ticketing.userservice.dto.ValidateCredentialsRequest;
import com.ticketing.userservice.service.UserApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserApplicationService userApplicationService;

    @PostMapping("/validate")
    @Operation(summary = "Validate credentials for internal authentication flow")
    public InternaluserValidationResponse validateCredentials(@Valid @RequestBody ValidateCredentialsRequest request) {
        return userApplicationService.validateCredentials(request);
    }
}
