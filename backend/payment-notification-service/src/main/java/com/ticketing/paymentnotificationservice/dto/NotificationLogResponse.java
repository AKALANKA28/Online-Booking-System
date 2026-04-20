package com.ticketing.paymentnotificationservice.dto;

import com.ticketing.paymentnotificationservice.entity.NotificationLog;
import com.ticketing.paymentnotificationservice.entity.NotificationStatus;

import java.time.OffsetDateTime;

public record NotificationLogResponse(
        Long id,
        String bookingReference,
        String recipient,
        String channel,
        String subject,
        String message,
        NotificationStatus status,
        OffsetDateTime createdAt
) {
    public static NotificationLogResponse from(NotificationLog notificationLog) {
        return new NotificationLogResponse(
                notificationLog.getId(),
                notificationLog.getBookingReference(),
                notificationLog.getRecipient(),
                notificationLog.getChannel(),
                notificationLog.getSubject(),
                notificationLog.getMessage(),
                notificationLog.getStatus(),
                notificationLog.getCreatedAt()
        );
    }
}
