# ecommerce-microservices

## Overview

This repository implements an e-commerce microservices architecture using Spring Boot, Spring Cloud, Kafka, MySQL, MongoDB, and Spring Cloud Gateway.

The system is composed of the following services:

- `config-server` - Spring Cloud Config Server serving centralized config for all services.
- `discovery` - Eureka service registry.
- `gateway` - Spring Cloud Gateway with OAuth2 JWT security.
- `customer` - Customer management service backed by MongoDB.
- `product` - Product catalog and purchase validation service backed by MySQL.
- `order` - Order management service backed by MySQL.
- `payment` - Payment persistence and notification producer service backed by MySQL.
- `notification` - Kafka consumer service for order/payment notifications and email sending backed by MongoDB.

## Architecture & Flow

### High-level flow

1. External clients send requests to the API Gateway at `http://localhost:8222`.
2. Gateway routes requests to downstream services using service discovery and configured routes.
3. `order-service` orchestrates order creation by calling:
   - `customer-service` to validate customer existence
   - `product-service` to reserve/purchase products
   - `payment-service` to create payment records
4. `order-service` publishes an order confirmation event to Kafka on `order-topic`.
5. `payment-service` publishes a payment notification event to Kafka on `payment-topic`.
6. `notification-service` consumes both topics and saves notifications while sending emails.

## Service Ports & Responsibilities

| Service | Port | Primary responsibility |
|---|---|---|
| config-server | 8888 | Centralized configuration provider.
| discovery | 8761 | Eureka service registry.
| gateway | 8222 | API gateway, route mapping, OAuth2 resource server.
| customer | 8090 | Customer CRUD and lookup.
| product | 8030 | Product creation, listing, purchase validation.
| order | 8070 | Order creation, order listing, order details, order-line lookups.
| payment | 8060 | Payment storage and notification publishing.
| notification | 8040 | Kafka notification consumer, email handling.

## Config Server Profiles

Configuration is stored under `services/config-server/src/main/resources/configurations/`.

Key service config files:

- `application.yml` - common config and Eureka registry settings.
- `discovery-service.yml` - Eureka server config.
- `gateway-service.yml` - gateway routes and discovery locator.
- `order-service.yml` - URL configuration for customer, payment, and product services, plus Kafka producer setup.
- `payment-service.yml` - Kafka producer setup and MySQL settings.
- `product-service.yml` - MySQL connection.
- `customer-service.yml` - MongoDB connection.
- `notification-service.yml` - MongoDB connection, Kafka consumer settings, and mail server settings.

## Gateway Routing

`gateway-service.yml` defines route mapping via service discovery:

- `/api/v1/customers/**` -> `lb://CUSTOMER-SERVICE`
- `/api/v1/orders/**` -> `lb://ORDER-SERVICE`
- `/api/v1/order-lines/**` -> `lb://ORDER-SERVICE`
- `/api/v1/products/**` -> `lb://PRODUCT-SERVICE`
- `/api/v1/payments/**` -> `lb://PAYMENT-SERVICE`

### Security

The gateway config secures all routes except `/eureka/**` and requires JWT authentication using OAuth2 resource server configuration.

## APIs by Service

### Customer Service (`/api/v1/customers`)

Endpoints:

- `POST /api/v1/customers`
  - Request: `CustomerRequest`
  - Response: `String` (message or created id)
- `PUT /api/v1/customers`
  - Request: `CustomerRequest`
  - Response: `204 Accepted`
- `GET /api/v1/customers`
  - Response: `List<CustomerResponse>`
- `GET /api/v1/customers/exits/{customer-id}`
  - Response: `Boolean`
- `GET /api/v1/customers/{customer-id}`
  - Response: `CustomerResponse`
- `DELETE /api/v1/customers/{customer-id}`
  - Response: `204 Accepted`

Request/response payload shapes:

CustomerRequest:

```json
{
  "id": "string",
  "firstname": "string",
  "lastname": "string",
  "email": "user@example.com",
  "address": {
    "street": "string",
    "houseNumber": "string",
    "zipCode": "string"
  }
}
```

CustomerResponse includes the same fields.

### Product Service (`/api/v1/products`)

Endpoints:

- `POST /api/v1/products`
  - Request: `ProductRequest`
  - Response: `Integer`
- `POST /api/v1/products/purchase`
  - Request: `List<ProductPurchaseRequest>`
  - Response: `List<ProductPurchaseResponse>`
- `GET /api/v1/products`
  - Response: `List<ProductResponse>`
- `GET /api/v1/products/{product-id}` *(intended)*
  - Response: `ProductResponse`

> Note: the current `ProductController` implementation has `@GetMapping("/product-id")` but uses `@PathVariable("product-id")`, which is inconsistent. The intended endpoint should likely be `GET /api/v1/products/{product-id}`.

Request/response payload shapes:

ProductRequest:

```json
{
  "id": 1,
  "name": "string",
  "description": "string",
  "availableQuantity": 10,
  "price": 100.00,
  "categoryId": 1
}
```

ProductPurchaseRequest:

```json
{
  "productId": 1,
  "quantity": 2
}
```

ProductPurchaseResponse:

```json
{
  "productId": 1,
  "name": "string",
  "description": "string",
  "price": 100.00,
  "quantity": 2
}
```

ProductResponse includes category metadata fields as well.

### Order Service (`/api/v1/orders` and `/api/v1/order-lines`)

Endpoints:

- `POST /api/v1/orders`
  - Request: `OrderRequest`
  - Response: `Integer` (created order id)
- `GET /api/v1/orders`
  - Response: `List<OrderResponse>`
- `GET /api/v1/orders/{order-id}`
  - Response: `OrderResponse`
- `GET /api/v1/order-lines/order/{order-id}`
  - Response: `List<OrderLineResponse>`

OrderRequest:

```json
{
  "id": 1,
  "reference": "ORD-001",
  "amount": 100.00,
  "paymentMethod": "CREDIT_CARD",
  "customerId": "CUST1",
  "products": [
    { "productId": 1, "quantity": 2 }
  ]
}
```

OrderResponse:

```json
{
  "id": 1,
  "reference": "ORD-001",
  "amount": 100.00,
  "paymentMethod": "CREDIT_CARD",
  "customerId": "CUST1"
}
```

OrderLineResponse:

```json
{
  "id": 1,
  "quantity": 2
}
```

### Payment Service (`/api/v1/payments`)

Endpoints:

- `POST /api/v1/payments`
  - Request: `PaymentRequest`
  - Response: `Integer`

PaymentRequest:

```json
{
  "amount": 100.00,
  "paymentMethod": "CREDIT_CARD",
  "orderId": 1,
  "orderReference": "ORD-001",
  "customer": {
    "id": "CUST1",
    "firstname": "John",
    "lastname": "Doe",
    "email": "john@example.com"
  }
}
```

### Notification Service

The notification service does not expose HTTP endpoints.
It consumes Kafka topics and processes events:

- `payment-topic` -> `PaymentConfirmation`
- `order-topic` -> `OrderConfirmation`

It stores notifications in MongoDB and sends email messages via configured mail properties.

## Service Interaction Details

### Order creation flow

1. Client calls `POST /api/v1/orders` through the gateway.
2. `OrderController` delegates to `OrderService.createdOrder()`.
3. `OrderService` calls `CustomerClient.findCustomerById()` to verify the customer.
4. `OrderService` calls `ProductClient.purchaseProducts()` to reserve products.
5. The order record is persisted in the order database.
6. Order line items are persisted using `OrderLineService`.
7. `OrderService` calls `PaymentClient.requestOrderPayment()` to create a payment.
8. `OrderService` publishes an `OrderConfirmation` event to Kafka via `OrderProducer`.

### Payment flow

1. Client calls `POST /api/v1/payments`.
2. `PaymentController` delegates to `PaymentService.createPayment()`.
3. Payment record is saved.
4. A `PaymentNotificationRequest` is published to Kafka via `NotificationProducer`.

### Notification flow

1. `NotificationConsumer` listens for `payment-topic` and `order-topic`.
2. On payment events, it stores a payment notification and sends a payment email.
3. On order events, it stores an order notification and sends an order confirmation email.

## Infrastructure Requirements

This project requires the following external services:

- Kafka broker at `localhost:9092`
- MySQL databases for `order_db`, `payment`, and `product`
- MongoDB databases for `customer` and `notification`
- Mail server at `localhost:1025` (configured in notification service)

## Notes & Observations

- `order-service` uses Spring Cloud OpenFeign to call `customer-service` and `payment-service`.
- `order-service` uses `RestTemplate` to call `product-service`.
- `gateway-service` is configured as an OAuth2 resource server and protects all routes except `/eureka/**`.
- `notification-service` is event-driven and consumes Kafka payloads to build notifications.
- `product-service` contains a likely endpoint bug in the product lookup method, which should be corrected to include `{product-id}` in the path.

## Running the project

Each service includes a Maven wrapper.
Use the following pattern from the root service folder:

```powershell
cd services\config-server
./mvnw.cmd spring-boot:run
```

Start services in this order:

1. `config-server`
2. `discovery`
3. `gateway`
4. `customer`
5. `product`
6. `order`
7. `payment`
8. `notification`

> Ensure Kafka, MySQL, MongoDB, and the SMTP server are running before starting the services.

**Zipkin start -> java -jar zipkin-server-3.4.1-exec.jar
--tracing is enabled for all services, and you can view traces at `http://localhost:9411` after making requests through the gateway.
** run from the zip file path keycloak start -> kc.bat start-dev
--keycloak is used for authentication, and you can obtain JWT tokens from the Keycloak server to access protected endpoints through the gateway. url: `http://localhost:8080/realms/master/protocol/openid-connect/auth?client_id=security-admin-console&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fadmin%2Fmaster%2Fconsole%2F%23%2Fmicro-services%2Fclients%2Fa40d47b7-c075-4f94-8ed7-c5d56aee43b6%2Fcredentials&state=99c0c7e8-711a-413d-9935-acb0fb63d346&response_mode=query&response_type=code&scope=openid&nonce=ef62d8ec-e06e-4583-8c13-ebda8c947f74&code_challenge=HTrt0ZCgYa8aLoDiLsZhesrkYZ-DLQZwp6jx0ASVSoY&code_challenge_method=S256` with appropriate client credentials
--keycloak email password: ztdd svnz mwoy mjki. which is generated in Google account https://myaccount.google.com/security?rapt=AEjHL4NoafLcoQ3_rywHW-gFoLgAxGTe-kzCAk34JwkF7kpsJp0PhY4xVVrKtHWHMSoseTZ9Uq7fQtVFwJ2SirINn4nocYIxYDT7Ek0FkY-I2-FWoa28yS4.
--Host *smtp.gmail.com
--Port 465
--Enable SSL true
**mail server can be set up using a tool like MailHog for local testing, which captures outgoing emails and provides a web interface to view them at `http://localhost:1080/#/email/AN0X67mQ`.
--run in terminal mail install - `npm install -g maildev`
--mail server - `maildev`
