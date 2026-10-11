package com.svarupa.payment;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
  Optional<Payment> findByStripeSessionId(String stripeSessionId);

  java.util.List<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
