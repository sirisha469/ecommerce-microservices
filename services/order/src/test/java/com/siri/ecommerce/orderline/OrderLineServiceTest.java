package com.siri.ecommerce.orderline;

import com.siri.ecommerce.order.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderLineServiceTest {

    @Mock
    private OrderLineRepository repository;

    @Mock
    private OrderLineMapper mapper;

    @InjectMocks
    private OrderLineService service;

    private OrderLine orderLine;
    private OrderLineRequest request;
    private OrderLineResponse response;

    @BeforeEach
    void setUp() {
        request = new OrderLineRequest(null, 1, 101, 2.0);
        orderLine = OrderLine.builder()
                .id(1)
                .order(Order.builder().id(1).build())
                .productId(101)
                .quantity(2.0)
                .build();
        response = new OrderLineResponse(1, 2.0);
    }

    @Test
    @DisplayName("saveOrderLine should map request, save and return ID")
    void shouldSaveOrderLine() {
        when(mapper.toOrderLine(request)).thenReturn(orderLine);
        when(repository.save(orderLine)).thenReturn(orderLine);

        Integer id = service.saveOrderLine(request);

        assertThat(id).isEqualTo(1);
        verify(mapper).toOrderLine(request);
        verify(repository).save(orderLine);
    }

    @Test
    @DisplayName("findAllByOrderId should return list of OrderLineResponses")
    void shouldFindAllByOrderId() {
        when(repository.findAllByOrderId(1)).thenReturn(List.of(orderLine));
        when(mapper.toOrderLineResponse(orderLine)).thenReturn(response);

        List<OrderLineResponse> results = service.findAllByOrderId(1);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).id()).isEqualTo(1);
        assertThat(results.get(0).quantity()).isEqualTo(2.0);
        verify(repository).findAllByOrderId(1);
        verify(mapper).toOrderLineResponse(orderLine);
    }
}
