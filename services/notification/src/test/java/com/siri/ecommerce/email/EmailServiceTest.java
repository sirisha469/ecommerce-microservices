package com.siri.ecommerce.email;

import com.siri.ecommerce.kafka.order.Product;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("sentPaymentSuccessEmail should process template and send email successfully")
    void shouldSendPaymentSuccessEmail() throws MessagingException {
        when(templateEngine.process(eq(EmailTemplates.PAYMENT_CONFIRMATION.getTemplate()), any(Context.class)))
                .thenReturn("<html>Payment Confirmation</html>");

        emailService.sentPaymentSuccessEmail(
                "customer@example.com",
                "Alice Wonder",
                BigDecimal.valueOf(150.0),
                "ORD-999"
        );

        verify(templateEngine).process(eq(EmailTemplates.PAYMENT_CONFIRMATION.getTemplate()), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("sentOrderConfirmationEmail should process template and send email successfully")
    void shouldSendOrderConfirmationEmail() throws MessagingException {
        when(templateEngine.process(eq(EmailTemplates.ORDER_CONFIRMATION.getTemplate()), any(Context.class)))
                .thenReturn("<html>Order Confirmation</html>");

        Product product = new Product(1, "Headphones", "Wireless", BigDecimal.valueOf(99.0), 1.0);

        emailService.sentOrderConfirmationEmail(
                "customer@example.com",
                "Alice Wonder",
                BigDecimal.valueOf(99.0),
                "ORD-1000",
                List.of(product)
        );

        verify(templateEngine).process(eq(EmailTemplates.ORDER_CONFIRMATION.getTemplate()), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("sentPaymentSuccessEmail should handle exception gracefully when send fails")
    void shouldHandleExceptionInPaymentSuccessEmail() throws MessagingException {
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Payment Confirmation</html>");
        doThrow(new RuntimeException("Mail server down")).when(mailSender).send(any(MimeMessage.class));

        org.assertj.core.api.Assertions.assertThatCode(() ->
                emailService.sentPaymentSuccessEmail(
                        "customer@example.com",
                        "Alice Wonder",
                        BigDecimal.valueOf(150.0),
                        "ORD-999"
                )
        ).isInstanceOf(RuntimeException.class);
    }
}
