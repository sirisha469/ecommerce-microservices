package com.siri.ecommerce.orderline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderLineControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderLineService orderLineService;

    @InjectMocks
    private OrderLineController orderLineController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderLineController).build();
    }

    @Test
    @DisplayName("GET /api/v1/order-lines/order/{order-id} should return 200 OK with list of order lines")
    void shouldFindAllByOrderId() throws Exception {
        OrderLineResponse response = new OrderLineResponse(1, 3.0);
        when(orderLineService.findAllByOrderId(1)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/order-lines/order/{order-id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].quantity").value(3.0));

        verify(orderLineService).findAllByOrderId(1);
    }
}
