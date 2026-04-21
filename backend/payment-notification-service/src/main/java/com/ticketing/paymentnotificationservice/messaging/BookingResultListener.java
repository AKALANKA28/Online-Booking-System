package com.ticketing.paymentnotificationservice.messaging;

import com.ticketing.paymentnotificationservice.service.NotificationApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingResultListener {

    private final NotificationApplicationService notificationApplicationService;

    @RabbitListener(queues = "${app.rabbitmq.confirmed-queue}")
    public void onBookingConfirmed(BookingResultMessage message) {
        log.info("Received booking.confirmed for bookingReference={}", message.bookingReference());
        notificationApplicationService.handleBookingResult(message);
    }

    @RabbitListener(queues = "${app.rabbitmq.failed-queue}")
    public void onBookingFailed(BookingResultMessage message) {
        log.info("Received booking.failed for bookingReference={}", message.bookingReference());
        notificationApplicationService.handleBookingResult(message);
    }

    @RabbitListener(queues = "${app.rabbitmq.cancelled-queue}")
    public void onBookingCancelled(BookingResultMessage message) {
        log.info("Received booking.cancelled for bookingReference={}", message.bookingReference());
        notificationApplicationService.handleBookingResult(message);
    }
}
