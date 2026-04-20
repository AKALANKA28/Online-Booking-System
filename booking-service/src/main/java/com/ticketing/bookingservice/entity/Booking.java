package com.ticketing.bookingservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_reference", columnList = "bookingReference", unique = true),
        @Index(name = "idx_booking_user", columnList = "userId")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String bookingReference;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false, length = 64)
    private String userId;

    @Column(nullable = false, length = 160)
    private String userEmail;

    /** E.164 for SMS (Twilio); optional. */
    @Column(length = 32)
    private String userPhone;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(length = 64)
    private String paymentReference;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<BookingItem> items = new ArrayList<>();

    public void addItem(BookingItem item) {
        items.add(item);
        item.setBooking(this);
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = BookingStatus.PENDING_PAYMENT;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
