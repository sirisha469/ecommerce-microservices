package com.siri.ecommerce.kafka;

import com.siri.ecommerce.customer.CustomerResponse;
import com.siri.ecommerce.order.PaymentMethod;
import com.siri.ecommerce.product.PurchaseResponse;

import java.math.BigDecimal;
import java.util.List;

public record OrderConfirmation(
        String orderReference,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        CustomerResponse customer,
        List<PurchaseResponse> products
) {
}
