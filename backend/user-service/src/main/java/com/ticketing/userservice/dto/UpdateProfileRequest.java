package com.ticketing.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 6, max = 128) String password,
        @Email @Size(max = 160) String email
) {}