package com.ticketing.eventservice.messaging;

import com.ticketing.eventservice.entity.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    public void publishEventCreated(Event event) {
        EventCreatedMessage payload = new EventCreatedMessage(
                event.getId(),
                event.getTitle(),
                event.getVenue(),
                event.getTotalRows(),
                event.getSeatsPerRow(),
                event.getVipRows(),
                event.getVipPrice(),
                event.getRegularPrice()
        );
        rabbitTemplate.convertAndSend(exchange, "event.created", payload);
    }
}
