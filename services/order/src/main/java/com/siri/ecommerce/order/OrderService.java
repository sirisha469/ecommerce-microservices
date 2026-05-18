package com.siri.ecommerce.order;

import com.siri.ecommerce.customer.CustomerClient;
import com.siri.ecommerce.exception.BusinessException;
import com.siri.ecommerce.kafka.OrderConfirmation;
import com.siri.ecommerce.kafka.OrderProducer;
import com.siri.ecommerce.orderline.OrderLineRequest;
import com.siri.ecommerce.orderline.OrderLineService;
import com.siri.ecommerce.payment.PaymentClient;
import com.siri.ecommerce.payment.PaymentRequest;
import com.siri.ecommerce.product.ProductClient;
import com.siri.ecommerce.product.PurchaseRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final OrderMapper mapper;
    private final OrderLineService orderLineService;
    private final OrderProducer orderProducer;
    private final PaymentClient paymentClient;
    public Integer createdOrder(OrderRequest request) {
        // check the customer --> OpenFeign
        var customer = this.customerClient.findCustomerById(request.customerId())
                .orElseThrow(() -> new BusinessException("Cannot create order :: No customer exists with the provided ID"));

        // purchase the products --> product-ms
        var purchasedProducts = productClient.purchaseProducts(request.products());
//        System.out.println("Purchased Products: " + request.products());

        // persist order
        var order = this.orderRepository.save(mapper.toOrder(request));
        // persists order lines

        for(PurchaseRequest purchaseRequest: request.products()){
            orderLineService.saveOrderLine(
                    new OrderLineRequest(
                            null,
                            order.getId(),
                            purchaseRequest.productId(),
                            purchaseRequest.quantity()
                    )
            );
        }

        // start payment process
        var paymentRequest = new PaymentRequest(
                request.amount(),
                request.paymentMethod(),
                order.getId(),
                order.getReference(),
                customer
        );
        paymentClient.requestOrderPayment(paymentRequest);

        // send the order confirmation --> notification-ms (kafka)
        orderProducer.sendOrderConfirmation(
                new OrderConfirmation(
                        request.reference(),
                        request.amount(),
                        request.paymentMethod(),
                        customer,
                        purchasedProducts
                )
        );
        return order.getId();
    }

    public List<OrderResponse> findAll() {
        return orderRepository.findAll()
                .stream()
                .map(mapper::fromOrder)
                .collect(Collectors.toList());
    }

    public OrderResponse findById(Integer orderId) {
        return orderRepository.findById(orderId)
                .map(mapper::fromOrder)
                .orElseThrow(() -> new EntityNotFoundException(String.format("No order found with the provided ID: %d", orderId)));
    }
}
