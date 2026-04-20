package com.ticketing.eventservice.messaging;

import java.math.BigDecimal;

public record EventCreatedMessage(
        Long eventId,
        String title,
        String venue,
        Integer totalRows,
        Integer seatsPerRow,
        Integer vipRows,
        BigDecimal vipPrice,
        BigDecimal regularPrice
) {
}
