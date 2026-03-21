package com.ticketing.eventservice.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record EventRequest(
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 1800) String description,
        @NotBlank @Size(max = 180) String venue,
        @NotNull OffsetDateTime startsAt,
        @NotNull OffsetDateTime endsAt,
        @NotNull @Min(1) @Max(26) Integer totalRows,
        @NotNull @Min(1) @Max(50) Integer seatsPerRow,
        @NotNull @Min(0) Integer vipRows,
        @NotNull @DecimalMin("0.0") BigDecimal vipPrice,
        @NotNull @DecimalMin("0.0") BigDecimal regularPrice
) {
}
