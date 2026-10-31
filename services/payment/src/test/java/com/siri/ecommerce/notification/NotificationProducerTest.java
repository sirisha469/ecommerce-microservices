package com.siri.ecommerce.notification;

import com.siri.ecommerce.payment.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationProducerTest {

    @Mock
    private KafkaTemplate<String, PaymentNotificationRequest> kafkaTemplate;

    @InjectMocks
    private NotificationProducer notificationProducer;

    @Test
    @DisplayName("sendNotification should build and send Message to payment-topic")
    void shouldSendNotification() {
        PaymentNotificationRequest request = new PaymentNotificationRequest(
                "ORD-999",
                BigDecimal.valueOf(99.00),
                PaymentMethod.CREDIT_CARD,
                "John",
                "Doe",
                "john.doe@example.com"
        );

        notificationProducer.sendNotification(request);

        ArgumentCaptor<Message<PaymentNotificationRequest>> captor = ArgumentCaptor.forClass(Message.class);
        verify(kafkaTemplate).send(captor.capture());

        Message<PaymentNotificationRequest> sentMessage = captor.getValue();
        assertThat(sentMessage).isNotNull();
        assertThat(sentMessage.getPayload()).isEqualTo(request);
        assertThat(sentMessage.getHeaders().get(KafkaHeaders.TOPIC)).isEqualTo("payment-topic");
    }
}
