package com.siri.ecommerce.orderline;

import com.siri.ecommerce.order.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderLineMapperTest {

    private OrderLineMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrderLineMapper();
    }

    @Test
    @DisplayName("toOrderLine should map OrderLineRequest to OrderLine entity")
    void shouldMapOrderLineRequestToOrderLine() {
        OrderLineRequest request = new OrderLineRequest(5, 10, 101, 4.0);

        OrderLine orderLine = mapper.toOrderLine(request);

        assertThat(orderLine).isNotNull();
        assertThat(orderLine.getId()).isEqualTo(5);
        assertThat(orderLine.getOrder()).isNotNull();
        assertThat(orderLine.getOrder().getId()).isEqualTo(10);
        assertThat(orderLine.getProductId()).isEqualTo(101);
        assertThat(orderLine.getQuantity()).isEqualTo(4.0);
    }

    @Test
    @DisplayName("toOrderLineResponse should map OrderLine entity to OrderLineResponse")
    void shouldMapOrderLineToOrderLineResponse() {
        OrderLine orderLine = OrderLine.builder()
                .id(5)
                .order(Order.builder().id(10).build())
                .productId(101)
                .quantity(4.0)
                .build();

        OrderLineResponse response = mapper.toOrderLineResponse(orderLine);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(5);
        assertThat(response.quantity()).isEqualTo(4.0);
    }
}
