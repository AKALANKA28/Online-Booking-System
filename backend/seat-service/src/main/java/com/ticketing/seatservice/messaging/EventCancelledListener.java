package com.ticketing.seatservice.messaging;

import com.ticketing.seatservice.entity.Seat;
import com.ticketing.seatservice.entity.SeatStatus;
import com.ticketing.seatservice.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component  
@RequiredArgsConstructor
public class EventCancelledListener {
    
    private final SeatRepository seatRepository;
    
    @RabbitListener(queues = "${app.rabbitmq.cancelled-queue}")
    public void onEventCancelled(EventCancelledMessage message) {
        log.info("Event {} cancelled - marking all seats unavailable", message.eventId());
        List<Seat> seats = seatRepository.findByEventIdOrderBySeatNumberAsc(message.eventId());
        seats.forEach(seat -> {
            seat.setStatus(SeatStatus.CANCELLED); // add CANCELLED to enum
            seat.setLockedByBookingReference(null);
            seat.setLockedUntil(null);
        });
        if (!seats.isEmpty()) seatRepository.saveAll(seats);
    }
}