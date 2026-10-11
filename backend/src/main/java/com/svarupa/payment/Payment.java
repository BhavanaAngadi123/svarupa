package com.svarupa.payment;

import com.svarupa.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Payment record (Stripe test mode). */
@Entity
@Table(name = "payments")
public class Payment {

  public enum Status {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
  }

  @Id private UUID id = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "stripe_session_id", unique = true, length = 255)
  private String stripeSessionId;

  @Column(name = "amount_cents", nullable = false)
  private int amountCents;

  @Column(nullable = false, length = 3)
  private String currency = "USD";

  @Column(nullable = false, length = 64)
  private String purpose = "PREMIUM";

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Status status = Status.PENDING;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  protected Payment() {}

  public UUID getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getStripeSessionId() {
    return stripeSessionId;
  }

  public void setStripeSessionId(String stripeSessionId) {
    this.stripeSessionId = stripeSessionId;
  }

  public int getAmountCents() {
    return amountCents;
  }

  public void setAmountCents(int amountCents) {
    this.amountCents = amountCents;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public String getPurpose() {
    return purpose;
  }

  public void setPurpose(String purpose) {
    this.purpose = purpose;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  @PreUpdate
  void touch() {
    updatedAt = Instant.now();
  }
}
