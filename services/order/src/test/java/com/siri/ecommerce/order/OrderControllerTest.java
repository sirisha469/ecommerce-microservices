package com.siri.ecommerce.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siri.ecommerce.product.PurchaseRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderController orderController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private OrderRequest orderRequest;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();

        orderRequest = new OrderRequest(
                1,
                "REF-001",
                BigDecimal.valueOf(250.0),
                PaymentMethod.PAYPAL,
                "cust-1",
                List.of(new PurchaseRequest(1, 1.0))
        );

        orderResponse = new OrderResponse(
                1,
                "REF-001",
                BigDecimal.valueOf(250.0),
                PaymentMethod.PAYPAL,
                "cust-1"
        );
    }

    @Test
    @DisplayName("POST /api/v1/orders should return 200 OK with created order ID")
    void shouldCreateOrder() throws Exception {
        when(orderService.createdOrder(any(OrderRequest.class))).thenReturn(1);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(orderService).createdOrder(any(OrderRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/orders should return 200 OK with list of orders")
    void shouldFindAllOrders() throws Exception {
        when(orderService.findAll()).thenReturn(List.of(orderResponse));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].reference").value("REF-001"));

        verify(orderService).findAll();
    }

    @Test
    @DisplayName("GET /api/v1/orders/{order-id} should return 200 OK with order details")
    void shouldFindOrderById() throws Exception {
        when(orderService.findById(1)).thenReturn(orderResponse);

        mockMvc.perform(get("/api/v1/orders/{order-id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.reference").value("REF-001"))
                .andExpect(jsonPath("$.paymentMethod").value("PAYPAL"));

        verify(orderService).findById(1);
    }
}
