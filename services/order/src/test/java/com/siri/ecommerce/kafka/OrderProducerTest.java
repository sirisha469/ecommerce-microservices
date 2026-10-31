package com.siri.ecommerce.kafka;

import com.siri.ecommerce.customer.CustomerResponse;
import com.siri.ecommerce.order.PaymentMethod;
import com.siri.ecommerce.product.PurchaseResponse;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderProducerTest {

    @Mock
    private KafkaTemplate<String, OrderConfirmation> kafkaTemplate;

    @InjectMocks
    private OrderProducer orderProducer;

    @Test
    @DisplayName("sendOrderConfirmation should build and send message to order-topic")
    void shouldSendOrderConfirmation() {
        CustomerResponse customer = new CustomerResponse("cust-1", "John", "Doe", "john@example.com");
        PurchaseResponse product = new PurchaseResponse(1, "Prod", "Desc", BigDecimal.TEN, 1.0);
        OrderConfirmation confirmation = new OrderConfirmation(
                "REF-001",
                BigDecimal.valueOf(100),
                PaymentMethod.CREDIT_CARD,
                customer,
                List.of(product)
        );

        orderProducer.sendOrderConfirmation(confirmation);

        ArgumentCaptor<Message<OrderConfirmation>> captor = ArgumentCaptor.forClass(Message.class);
        verify(kafkaTemplate).send(captor.capture());

        Message<OrderConfirmation> sentMessage = captor.getValue();
        assertThat(sentMessage).isNotNull();
        assertThat(sentMessage.getPayload()).isEqualTo(confirmation);
        assertThat(sentMessage.getHeaders().get(KafkaHeaders.TOPIC)).isEqualTo("order-topic");
    }
}
