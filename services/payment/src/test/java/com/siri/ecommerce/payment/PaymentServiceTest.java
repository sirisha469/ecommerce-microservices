package com.siri.ecommerce.payment;

import com.siri.ecommerce.notification.NotificationProducer;
import com.siri.ecommerce.notification.PaymentNotificationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper mapper;

    @Mock
    private NotificationProducer notificationProducer;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest paymentRequest;
    private Payment payment;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer("cust-1", "Alice", "Wonder", "alice@example.com");
        paymentRequest = new PaymentRequest(
                BigDecimal.valueOf(150.0),
                PaymentMethod.CREDIT_CARD,
                10,
                "ORDER-REF-10",
                customer
        );

        payment = Payment.builder()
                .id(1)
                .amount(BigDecimal.valueOf(150.0))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .orderId(10)
                .build();
    }

    @Test
    @DisplayName("createPayment should save payment, trigger Kafka notification, and return generated payment ID")
    void shouldCreatePaymentSuccessfully() {
        when(mapper.toPayment(paymentRequest)).thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);
        doNothing().when(notificationProducer).sendNotification(any(PaymentNotificationRequest.class));

        Integer paymentId = paymentService.createPayment(paymentRequest);

        assertThat(paymentId).isEqualTo(1);
        verify(mapper).toPayment(paymentRequest);
        verify(paymentRepository).save(payment);
        verify(notificationProducer).sendNotification(argThat(notification ->
                notification.orderReference().equals("ORDER-REF-10") &&
                notification.amount().equals(BigDecimal.valueOf(150.0)) &&
                notification.customerEmail().equals("alice@example.com") &&
                notification.customerFirstname().equals("Alice") &&
                notification.customerLastname().equals("Wonder")
        ));
    }
}
