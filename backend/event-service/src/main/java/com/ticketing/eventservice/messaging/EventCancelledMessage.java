package com.ticketing.eventservice.messaging;

public record EventCancelledMessage(
    Long eventId,
    String title,
    String reason
) {}