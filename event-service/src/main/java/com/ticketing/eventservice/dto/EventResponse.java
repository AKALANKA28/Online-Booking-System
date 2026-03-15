package com.ticketing.eventservice.dto;

import com.ticketing.eventservice.entity.Event;
import com.ticketing.eventservice.entity.EventStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record EventResponse(
        Long id,
        String title,
        String description,
        String venue,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        Integer totalRows,
        Integer seatsPerRow,
        Integer vipRows,
        BigDecimal vipPrice,
        BigDecimal regularPrice,
        EventStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getVenue(),
                event.getStartsAt(),
                event.getEndsAt(),
                event.getTotalRows(),
                event.getSeatsPerRow(),
                event.getVipRows(),
                event.getVipPrice(),
                event.getRegularPrice(),
                event.getStatus(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
