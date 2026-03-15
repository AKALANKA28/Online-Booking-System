package com.ticketing.eventservice.dto;

import com.ticketing.eventservice.entity.EventStatus;
import jakarta.validation.constraints.NotNull;

public record EventStatusUpdateRequest(@NotNull EventStatus status) {
}
