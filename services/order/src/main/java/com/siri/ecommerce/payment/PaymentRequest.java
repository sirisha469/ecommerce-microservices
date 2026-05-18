package com.siri.ecommerce.payment;

import com.siri.ecommerce.customer.CustomerResponse;
import com.siri.ecommerce.order.PaymentMethod;

import java.math.BigDecimal;

public record PaymentRequest(
        BigDecimal amount,
        PaymentMethod paymentMethod,
        Integer orderId,
        String orderReference,
        CustomerResponse customer
) {
}
