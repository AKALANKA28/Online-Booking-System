package com.ticketing.bookingservice.messaging;

import com.ticketing.bookingservice.client.SeatServiceClient;
import com.ticketing.bookingservice.dto.ReleaseSeatsRequest;
import com.ticketing.bookingservice.entity.Booking;
import com.ticketing.bookingservice.entity.BookingStatus;
import com.ticketing.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventCancelledListener {
    
    private final BookingRepository bookingRepository;
    private final SeatServiceClient seatServiceClient;
    
    @RabbitListener(queues = "${app.rabbitmq.event-cancelled-queue}")
    public void onEventCancelled(EventCancelledMessage message) {
        log.info("Event {} cancelled - cancelling all bookings", message.eventId());
        List<Booking> bookings = bookingRepository.findByEventId(message.eventId());
        
        for (Booking booking : bookings) {
            if (booking.getStatus() == BookingStatus.CONFIRMED || 
                booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
                // Release seats
                try {
                    seatServiceClient.releaseSeats(
                        new ReleaseSeatsRequest(booking.getBookingReference())
                    );
                } catch (Exception e) {
                    log.warn("Could not release seats for {}", booking.getBookingReference());
                }
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);
            }
        }
    }
}