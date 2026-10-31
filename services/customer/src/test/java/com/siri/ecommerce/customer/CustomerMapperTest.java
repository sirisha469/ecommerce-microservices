package com.siri.ecommerce.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerMapperTest {

    private CustomerMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CustomerMapper();
    }

    @Test
    @DisplayName("toCustomer should return null when request is null")
    void shouldReturnNullWhenRequestIsNull() {
        Customer customer = mapper.toCustomer(null);
        assertThat(customer).isNull();
    }

    @Test
    @DisplayName("toCustomer should correctly map CustomerRequest to Customer entity")
    void shouldMapCustomerRequestToCustomerEntity() {
        Address address = new Address("Wall St", "10", "10005");
        CustomerRequest request = new CustomerRequest("cust-10", "Alice", "Wonder", "alice@example.com", address);

        Customer customer = mapper.toCustomer(request);

        assertThat(customer).isNotNull();
        assertThat(customer.getId()).isEqualTo("cust-10");
        assertThat(customer.getFirstname()).isEqualTo("Alice");
        assertThat(customer.getLastname()).isEqualTo("Wonder");
        assertThat(customer.getEmail()).isEqualTo("alice@example.com");
        assertThat(customer.getAddress()).isEqualTo(address);
    }

    @Test
    @DisplayName("fromCustomer should correctly map Customer entity to CustomerResponse")
    void shouldMapCustomerEntityToCustomerResponse() {
        Address address = new Address("Wall St", "10", "10005");
        Customer customer = Customer.builder()
                .id("cust-10")
                .firstname("Alice")
                .lastname("Wonder")
                .email("alice@example.com")
                .address(address)
                .build();

        CustomerResponse response = mapper.fromCustomer(customer);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("cust-10");
        assertThat(response.firstname()).isEqualTo("Alice");
        assertThat(response.lastname()).isEqualTo("Wonder");
        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.address()).isEqualTo(address);
    }
}
