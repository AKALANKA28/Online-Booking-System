package com.ticketing.paymentnotificationservice.service;

import com.ticketing.paymentnotificationservice.config.IntegrationProperties;
import com.ticketing.paymentnotificationservice.entity.NotificationLog;
import com.ticketing.paymentnotificationservice.entity.NotificationStatus;
import com.ticketing.paymentnotificationservice.integration.NotificationDispatchService;
import com.ticketing.paymentnotificationservice.messaging.BookingResultMessage;
import com.ticketing.paymentnotificationservice.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationApplicationService {

    private final NotificationLogRepository notificationLogRepository;
    private final IntegrationProperties integrationProperties;
    private final NotificationDispatchService notificationDispatchService;

    @Transactional
    public void handleBookingResult(BookingResultMessage message) {
        String subject;
        String body;
        if ("CONFIRMED".equalsIgnoreCase(message.status())) {
            subject = "Booking Confirmed - " + message.bookingReference();
            body = "Your booking is confirmed for event " + message.eventId()
                    + ". Seats: " + String.join(", ", message.seatNumbers())
                    + ". Total paid: " + message.totalAmount();
        } else if ("CANCELLED".equalsIgnoreCase(message.status())) {
            subject = "Event Cancelled - " + message.bookingReference();
            body = "Your event was cancelled by the organizer for event " + message.eventId()
                    + ". Affected seats: " + String.join(", ", message.seatNumbers())
                    + ". Please contact support for refund and next steps.";
        } else {
            subject = "Booking Failed - " + message.bookingReference();
            body = "Your booking could not be completed for event " + message.eventId()
                    + ". Any reserved seats have been released.";
        }

        boolean needEmail = integrationProperties.sendgridEnabled();
        boolean needSms = integrationProperties.twilioSmsEnabled()
                && message.userPhone() != null
                && !message.userPhone().isBlank();
        boolean anyDispatch = needEmail || needSms;

        String channel;
        if (needEmail && needSms) {
            channel = "EMAIL+SMS";
        } else if (needSms) {
            channel = "SMS";
        } else if (needEmail) {
            channel = "EMAIL";
        } else {
            channel = "NONE";
        }

        NotificationStatus status = NotificationStatus.SENT;
        String storedMessage = body;

        if (!anyDispatch) {
            status = NotificationStatus.FAILED;
            storedMessage = body + "\nDispatch error: No notification channel configured. "
                    + "Set SENDGRID_API_KEY and SENDGRID_FROM_EMAIL (or Twilio SMS settings).";
            log.warn("Notification skipped for booking {} because no channels are configured", message.bookingReference());
        }

        if (anyDispatch) {
            try {
                if (needEmail) {
                    if (message.userEmail() == null || message.userEmail().isBlank()) {
                        throw new IllegalArgumentException("Recipient email is missing for booking " + message.bookingReference());
                    }
                    notificationDispatchService.sendEmail(message.userEmail(), subject, body);
                }
                if (needSms) {
                    notificationDispatchService.sendSms(message.userPhone(), subject + "\n" + body);
                }
            } catch (Exception e) {
                log.warn("Notification dispatch failed for booking {}", message.bookingReference(), e);
                status = NotificationStatus.FAILED;
                storedMessage = body + "\nDispatch error: " + e.getMessage();
            }
        }

        NotificationLog logEntry = NotificationLog.builder()
                .bookingReference(message.bookingReference())
                .recipient(message.userEmail())
                .channel(channel)
                .subject(subject)
                .message(storedMessage)
                .status(status)
                .build();

        notificationLogRepository.save(logEntry);
    }

    @Transactional(readOnly = true)
    public List<NotificationLog> findByBookingReference(String bookingReference) {
        return notificationLogRepository.findByBookingReferenceOrderByCreatedAtDesc(bookingReference);
    }
}
