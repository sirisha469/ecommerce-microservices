package com.siri.ecommerce.kafka;

import com.siri.ecommerce.email.EmailService;
import com.siri.ecommerce.kafka.order.Customer;
import com.siri.ecommerce.kafka.order.OrderConfirmation;
import com.siri.ecommerce.kafka.order.Product;
import com.siri.ecommerce.kafka.payment.PaymentConfirmation;
import com.siri.ecommerce.kafka.payment.PaymentMethod;
import com.siri.ecommerce.notification.Notification;
import com.siri.ecommerce.notification.NotificationRepository;
import com.siri.ecommerce.notification.NotificationType;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationConsumer consumer;

    @Test
    @DisplayName("consumePaymentSuccessNotification should save notification and call email service")
    void shouldConsumePaymentSuccessNotification() throws MessagingException {
        PaymentConfirmation confirmation = new PaymentConfirmation(
                "ORD-111",
                BigDecimal.valueOf(199.99),
                PaymentMethod.CREDIT_CARD,
                "John",
                "Doe",
                "john.doe@example.com"
        );

        consumer.consumePaymentSuccessNotification(confirmation);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved).isNotNull();
        assertThat(saved.getType()).isEqualTo(NotificationType.PAYMENT_CONFIRMATION);
        assertThat(saved.getPaymentConfirmation()).isEqualTo(confirmation);

        verify(emailService).sentPaymentSuccessEmail(
                eq("john.doe@example.com"),
                eq("John Doe"),
                eq(BigDecimal.valueOf(199.99)),
                eq("ORD-111")
        );
    }

    @Test
    @DisplayName("consumeOrderConfirmationNotification should save notification and call email service")
    void shouldConsumeOrderConfirmationNotification() throws MessagingException {
        Customer customer = new Customer("c-1", "Jane", "Doe", "jane.doe@example.com");
        Product product = new Product(1, "Book", "Novel", BigDecimal.valueOf(19.99), 2.0);
        OrderConfirmation confirmation = new OrderConfirmation(
                "ORD-222",
                BigDecimal.valueOf(39.98),
                PaymentMethod.PAYPAL,
                customer,
                List.of(product)
        );

        consumer.consumeOrderConfirmationNotification(confirmation);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved).isNotNull();
        assertThat(saved.getType()).isEqualTo(NotificationType.ORDER_CONFIRMATION);
        assertThat(saved.getOrderConfirmation()).isEqualTo(confirmation);

        verify(emailService).sentOrderConfirmationEmail(
                eq("jane.doe@example.com"),
                eq("Jane Doe"),
                eq(BigDecimal.valueOf(39.98)),
                eq("ORD-222"),
                eq(List.of(product))
        );
    }
}
