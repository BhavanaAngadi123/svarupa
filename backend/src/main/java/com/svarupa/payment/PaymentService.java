package com.svarupa.payment;

import com.svarupa.config.SvarupaProperties;
import com.svarupa.user.UserRepository;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Premium subscriptions via Stripe (TEST mode only). Disabled by default
 * ({@code STRIPE_ENABLED=false}) — the app boots and runs fully without keys.
 */
@Service
public class PaymentService {

  private final PaymentRepository payments;
  private final UserRepository users;
  private final SvarupaProperties props;

  public PaymentService(
      PaymentRepository payments, UserRepository users, SvarupaProperties props) {
    this.payments = payments;
    this.users = users;
    this.props = props;
  }

  @Transactional
  public CheckoutResult createPremiumCheckout(UUID userId, String successUrl, String cancelUrl) {
    ensureEnabled();
    var user =
        users
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    Stripe.apiKey = props.getStripe().getSecretKey();

    var payment = new Payment();
    payment.setUser(user);
    payment.setAmountCents(999); // $9.99/mo premium
    payment.setCurrency("USD");
    payment.setPurpose("PREMIUM_MONTHLY");
    payments.save(payment);

    try {
      var params =
          SessionCreateParams.builder()
              .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
              .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
              .setCancelUrl(cancelUrl)
              .putMetadata("payment_id", payment.getId().toString())
              .putMetadata("user_id", userId.toString())
              .addLineItem(
                  SessionCreateParams.LineItem.builder()
                      .setQuantity(1L)
                      .setPriceData(
                          SessionCreateParams.LineItem.PriceData.builder()
                              .setCurrency("usd")
                              .setUnitAmount(999L)
                              .setRecurring(
                                  SessionCreateParams.LineItem.PriceData.Recurring.builder()
                                      .setInterval(
                                          SessionCreateParams.LineItem.PriceData.Recurring
                                              .Interval.MONTH)
                                      .build())
                              .setProductData(
                                  SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                      .setName("Svarupa Premium")
                                      .build())
                              .build())
                      .build())
              .build();
      Session session = Session.create(params);
      payment.setStripeSessionId(session.getId());
      return new CheckoutResult(session.getUrl(), payment.getId());
    } catch (Exception e) {
      payment.setStatus(Payment.Status.FAILED);
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY, "stripe checkout failed: " + e.getMessage());
    }
  }

  @Transactional
  public void markPaid(String stripeSessionId) {
    payments
        .findByStripeSessionId(stripeSessionId)
        .ifPresent(
            p -> {
              p.setStatus(Payment.Status.PAID);
            });
  }

  private void ensureEnabled() {
    if (!props.getStripe().isEnabled()) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "payments are disabled (test mode not configured)");
    }
  }

  public record CheckoutResult(String checkoutUrl, UUID paymentId) {}

  public record PaymentView(
      UUID id, int amountCents, String currency, String purpose, Payment.Status status) {
    static PaymentView of(Payment p) {
      return new PaymentView(
          p.getId(), p.getAmountCents(), p.getCurrency(), p.getPurpose(), p.getStatus());
    }
  }

  @Transactional(readOnly = true)
  public java.util.List<PaymentView> myPayments(UUID userId) {
    return payments.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(PaymentView::of)
        .toList();
  }
}
