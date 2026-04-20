package com.ticketing.bookingservice.dto;

import com.ticketing.bookingservice.entity.BookingItem;

import java.math.BigDecimal;

public record BookingItemResponse(String seatNumber, String category, BigDecimal price) {
    public static BookingItemResponse from(BookingItem item) {
        return new BookingItemResponse(item.getSeatNumber(), item.getCategory(), item.getPrice());
    }
}
