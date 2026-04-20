package com.ticketing.seatservice.messaging;

public record EventCancelledMessage(Long eventId, String title, String reason) {}