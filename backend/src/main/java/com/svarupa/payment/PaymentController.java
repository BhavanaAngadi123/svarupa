package com.svarupa.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Payment endpoints (Stripe test mode; disabled by default). */
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments")
public class PaymentController {

  private final PaymentService paymentService;

  public PaymentController(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @PostMapping("/premium/checkout")
  @Operation(summary = "Create a Stripe checkout session for Premium")
  public PaymentService.CheckoutResult checkout(
      @AuthenticationPrincipal String userId, @RequestBody CheckoutRequest req) {
    return paymentService.createPremiumCheckout(
        UUID.fromString(userId), req.successUrl(), req.cancelUrl());
  }

  @GetMapping("/mine")
  @Operation(summary = "My payment history")
  public List<PaymentService.PaymentView> mine(@AuthenticationPrincipal String userId) {
    return paymentService.myPayments(UUID.fromString(userId));
  }

  public record CheckoutRequest(String successUrl, String cancelUrl) {}
}
