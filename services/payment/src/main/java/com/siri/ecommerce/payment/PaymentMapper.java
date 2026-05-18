package com.siri.ecommerce.payment;

import org.springframework.stereotype.Service;

@Service
public class PaymentMapper {

    public Payment toPayment(PaymentRequest paymentRequest) {
        if (paymentRequest == null) {
            return null;
        }
        return Payment.builder()
                .paymentMethod(paymentRequest.paymentMethod())
                .amount(paymentRequest.amount())
                .orderId(paymentRequest.orderId())
                .build();
    }
}
