package com.ticketing.paymentnotificationservice.service;

import com.ticketing.paymentnotificationservice.entity.NotificationLog;
import com.ticketing.paymentnotificationservice.entity.NotificationStatus;
import com.ticketing.paymentnotificationservice.messaging.BookingResultMessage;
import com.ticketing.paymentnotificationservice.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationApplicationService {

    private final NotificationLogRepository notificationLogRepository;

    @Transactional
    public void handleBookingResult(BookingResultMessage message) {
        String subject;
        String body;
        if ("CONFIRMED".equalsIgnoreCase(message.status())) {
            subject = "Booking Confirmed - " + message.bookingReference();
            body = "Your booking is confirmed for event " + message.eventId()
                    + ". Seats: " + String.join(", ", message.seatNumbers())
                    + ". Total paid: " + message.totalAmount();
        } else {
            subject = "Booking Failed - " + message.bookingReference();
            body = "Your booking could not be completed for event " + message.eventId()
                    + ". Any reserved seats have been released.";
        }

        NotificationLog log = NotificationLog.builder()
                .bookingReference(message.bookingReference())
                .recipient(message.userEmail())
                .channel("EMAIL")
                .subject(subject)
                .message(body)
                .status(NotificationStatus.SENT)
                .build();

        notificationLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<NotificationLog> findByBookingReference(String bookingReference) {
        return notificationLogRepository.findByBookingReferenceOrderByCreatedAtDesc(bookingReference);
    }
}
