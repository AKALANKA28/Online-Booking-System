package com.ticketing.eventservice.dto;

public record EventCancelledMessage(
    Long eventId,
    String title,
    String reason
) {}