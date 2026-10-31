package com.siri.ecommerce.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    private OrderMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrderMapper();
    }

    @Test
    @DisplayName("toOrder should map OrderRequest to Order entity correctly")
    void shouldMapOrderRequestToOrder() {
        OrderRequest request = new OrderRequest(
                10,
                "REF-555",
                BigDecimal.valueOf(450.0),
                PaymentMethod.CREDIT_CARD,
                "cust-99",
                Collections.emptyList()
        );

        Order order = mapper.toOrder(request);

        assertThat(order).isNotNull();
        assertThat(order.getId()).isEqualTo(10);
        assertThat(order.getReference()).isEqualTo("REF-555");
        assertThat(order.getTotalAmount()).isEqualTo(BigDecimal.valueOf(450.0));
        assertThat(order.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(order.getCustomerId()).isEqualTo("cust-99");
    }

    @Test
    @DisplayName("fromOrder should map Order entity to OrderResponse correctly")
    void shouldMapOrderToOrderResponse() {
        Order order = Order.builder()
                .id(10)
                .reference("REF-555")
                .totalAmount(BigDecimal.valueOf(450.0))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .customerId("cust-99")
                .build();

        OrderResponse response = mapper.fromOrder(order);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(10);
        assertThat(response.reference()).isEqualTo("REF-555");
        assertThat(response.amount()).isEqualTo(BigDecimal.valueOf(450.0));
        assertThat(response.paymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(response.customerId()).isEqualTo("cust-99");
    }
}
