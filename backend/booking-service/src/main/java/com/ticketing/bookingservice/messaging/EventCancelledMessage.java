package com.ticketing.bookingservice.messaging;

public record EventCancelledMessage(Long eventId, String title, String reason) {}