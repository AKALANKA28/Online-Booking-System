package com.ticketing.seatservice.messaging;

import com.ticketing.seatservice.service.SeatInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventCreatedListener {

    private final SeatInventoryService seatInventoryService;

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void handleEventCreated(EventCreatedMessage message) {
        log.info("Received event.created for eventId={}", message.eventId());
        seatInventoryService.createSeatsForEvent(message);
    }
}
