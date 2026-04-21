package com.ticketing.bookingservice.messaging;

import com.ticketing.bookingservice.entity.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingResultPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    public void publishConfirmed(Booking booking) {
        rabbitTemplate.convertAndSend(exchange, "booking.confirmed", toMessage(booking, "CONFIRMED"));
    }

    public void publishFailed(Booking booking) {
        rabbitTemplate.convertAndSend(exchange, "booking.failed", toMessage(booking, "FAILED"));
    }

    public void publishCancelled(Booking booking) {
        rabbitTemplate.convertAndSend(exchange, "booking.cancelled", toMessage(booking, "CANCELLED"));
    }

    private BookingResultMessage toMessage(Booking booking, String status) {
        return new BookingResultMessage(
                booking.getBookingReference(),
                booking.getEventId(),
                booking.getUserId(),
                booking.getUserEmail(),
                booking.getUserPhone(),
                booking.getTotalAmount(),
                status,
                booking.getItems().stream().map(item -> item.getSeatNumber()).toList()
        );
    }
}
