package com.siri.ecommerce.order;

import com.siri.ecommerce.customer.CustomerClient;
import com.siri.ecommerce.customer.CustomerResponse;
import com.siri.ecommerce.exception.BusinessException;
import com.siri.ecommerce.kafka.OrderConfirmation;
import com.siri.ecommerce.kafka.OrderProducer;
import com.siri.ecommerce.orderline.OrderLineRequest;
import com.siri.ecommerce.orderline.OrderLineService;
import com.siri.ecommerce.payment.PaymentClient;
import com.siri.ecommerce.payment.PaymentRequest;
import com.siri.ecommerce.product.ProductClient;
import com.siri.ecommerce.product.PurchaseRequest;
import com.siri.ecommerce.product.PurchaseResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CustomerClient customerClient;
    @Mock
    private ProductClient productClient;
    @Mock
    private OrderMapper mapper;
    @Mock
    private OrderLineService orderLineService;
    @Mock
    private OrderProducer orderProducer;
    @Mock
    private PaymentClient paymentClient;

    @InjectMocks
    private OrderService orderService;

    private OrderRequest orderRequest;
    private CustomerResponse customerResponse;
    private Order order;
    private OrderResponse orderResponse;
    private PurchaseResponse purchaseResponse;

    @BeforeEach
    void setUp() {
        customerResponse = new CustomerResponse("cust-1", "John", "Doe", "john@example.com");

        PurchaseRequest purchaseRequest = new PurchaseRequest(1, 2.0);
        orderRequest = new OrderRequest(
                1,
                "ORD-123",
                BigDecimal.valueOf(199.99),
                PaymentMethod.CREDIT_CARD,
                "cust-1",
                List.of(purchaseRequest)
        );

        order = Order.builder()
                .id(1)
                .reference("ORD-123")
                .totalAmount(BigDecimal.valueOf(199.99))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .customerId("cust-1")
                .build();

        orderResponse = new OrderResponse(
                1,
                "ORD-123",
                BigDecimal.valueOf(199.99),
                PaymentMethod.CREDIT_CARD,
                "cust-1"
        );

        purchaseResponse = new PurchaseResponse(1, "Product 1", "Desc 1", BigDecimal.valueOf(99.99), 2.0);
    }

    @Test
    @DisplayName("createdOrder should orchestrate customer check, product purchase, save order, save lines, call payment and produce event")
    void shouldCreateOrderSuccessfully() {
        when(customerClient.findCustomerById("cust-1")).thenReturn(Optional.of(customerResponse));
        when(productClient.purchaseProducts(orderRequest.products())).thenReturn(List.of(purchaseResponse));
        when(mapper.toOrder(orderRequest)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(paymentClient.requestOrderPayment(any(PaymentRequest.class))).thenReturn(1);
        doNothing().when(orderProducer).sendOrderConfirmation(any(OrderConfirmation.class));

        Integer createdOrderId = orderService.createdOrder(orderRequest);

        assertThat(createdOrderId).isEqualTo(1);
        verify(customerClient).findCustomerById("cust-1");
        verify(productClient).purchaseProducts(orderRequest.products());
        verify(orderRepository).save(order);
        verify(orderLineService).saveOrderLine(any(OrderLineRequest.class));
        verify(paymentClient).requestOrderPayment(any(PaymentRequest.class));
        verify(orderProducer).sendOrderConfirmation(any(OrderConfirmation.class));
    }

    @Test
    @DisplayName("createdOrder should throw BusinessException when customer not found")
    void shouldThrowExceptionWhenCustomerDoesNotExist() {
        when(customerClient.findCustomerById("cust-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createdOrder(orderRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot create order :: No customer exists with the provided ID");

        verify(productClient, never()).purchaseProducts(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("findAll should return all orders mapped to OrderResponse")
    void shouldFindAllOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(mapper.fromOrder(order)).thenReturn(orderResponse);

        List<OrderResponse> orders = orderService.findAll();

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).reference()).isEqualTo("ORD-123");
        verify(orderRepository).findAll();
    }

    @Test
    @DisplayName("findById should return OrderResponse when order exists")
    void shouldFindOrderById() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(mapper.fromOrder(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.findById(1);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1);
        assertThat(result.reference()).isEqualTo("ORD-123");
        verify(orderRepository).findById(1);
    }

    @Test
    @DisplayName("findById should throw EntityNotFoundException when order does not exist")
    void shouldThrowExceptionWhenOrderNotFoundById() {
        when(orderRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(99))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("No order found with the provided ID: 99");

        verify(orderRepository).findById(99);
    }
}
