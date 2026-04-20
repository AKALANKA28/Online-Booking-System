package com.ticketing.paymentnotificationservice.repository;

import com.ticketing.paymentnotificationservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentReference(String paymentReference);

    Optional<Payment> findByProviderReference(String providerReference);
}
