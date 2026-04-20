package com.ticketing.userservice.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidateCredentialsRequest(
        @NotBlank String username,
        @NotBlank String password
) {}