package com.siri.ecommerce.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentMapperTest {

    private PaymentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PaymentMapper();
    }

    @Test
    @DisplayName("toPayment should return null when request is null")
    void shouldReturnNullWhenRequestIsNull() {
        Payment payment = mapper.toPayment(null);
        assertThat(payment).isNull();
    }

    @Test
    @DisplayName("toPayment should map PaymentRequest to Payment entity correctly")
    void shouldMapPaymentRequestToPayment() {
        Customer customer = new Customer("c-1", "Bob", "Smith", "bob@example.com");
        PaymentRequest request = new PaymentRequest(
                BigDecimal.valueOf(120.0),
                PaymentMethod.MASTER_CARD,
                5,
                "REF-05",
                customer
        );

        Payment payment = mapper.toPayment(request);

        assertThat(payment).isNotNull();
        assertThat(payment.getAmount()).isEqualTo(BigDecimal.valueOf(120.0));
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.MASTER_CARD);
        assertThat(payment.getOrderId()).isEqualTo(5);
    }
}
