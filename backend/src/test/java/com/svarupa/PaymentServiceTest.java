package com.svarupa;

import static org.assertj.core.api.Assertions.*;

import com.svarupa.payment.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

class PaymentServiceTest extends IntegrationTestBase {

  @Autowired PaymentService paymentService;

  @Test
  void checkoutDisabledByDefault() {
    assertThatThrownBy(
            () ->
                paymentService.createPremiumCheckout(
                    student.getId(), "https://example.com/ok", "https://example.com/cancel"))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("disabled");
  }

  @Test
  void emptyHistoryForNewUser() {
    assertThat(paymentService.myPayments(student.getId())).isEmpty();
  }
}
