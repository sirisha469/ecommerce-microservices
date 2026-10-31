package com.siri.ecommerce.customer;

import com.siri.ecommerce.exception.CustomerNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository repository;

    @Mock
    private CustomerMapper mapper;

    @InjectMocks
    private CustomerService service;

    private Customer customer;
    private CustomerRequest request;
    private CustomerResponse response;
    private Address address;

    @BeforeEach
    void setUp() {
        address = new Address("Main St", "123", "500001");
        customer = Customer.builder()
                .id("cust-1")
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .address(address)
                .build();

        request = new CustomerRequest(
                "cust-1",
                "John",
                "Doe",
                "john.doe@example.com",
                address
        );

        response = new CustomerResponse(
                "cust-1",
                "John",
                "Doe",
                "john.doe@example.com",
                address
        );
    }

    @Test
    @DisplayName("createCustomer should map request, save entity and return generated ID")
    void shouldCreateCustomerSuccessfully() {
        when(mapper.toCustomer(request)).thenReturn(customer);
        when(repository.save(customer)).thenReturn(customer);

        String createdId = service.createCustomer(request);

        assertThat(createdId).isEqualTo("cust-1");
        verify(mapper).toCustomer(request);
        verify(repository).save(customer);
    }

    @Test
    @DisplayName("updateCustomer should update all fields when customer exists")
    void shouldUpdateCustomerWhenCustomerExists() {
        Address newAddress = new Address("Oak St", "456", "500002");
        CustomerRequest updateReq = new CustomerRequest(
                "cust-1",
                "Jane",
                "Smith",
                "jane.smith@example.com",
                newAddress
        );

        when(repository.findById("cust-1")).thenReturn(Optional.of(customer));
        when(repository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateCustomer(updateReq);

        assertThat(customer.getFirstname()).isEqualTo("Jane");
        assertThat(customer.getLastname()).isEqualTo("Smith");
        assertThat(customer.getEmail()).isEqualTo("jane.smith@example.com");
        assertThat(customer.getAddress()).isEqualTo(newAddress);
        verify(repository).findById("cust-1");
        verify(repository).save(customer);
    }

    @Test
    @DisplayName("updateCustomer should partially update only non-blank fields")
    void shouldPartiallyUpdateCustomerWhenBlankFieldsPassed() {
        CustomerRequest partialReq = new CustomerRequest(
                "cust-1",
                "",
                "UpdatedLastName",
                null,
                null
        );

        when(repository.findById("cust-1")).thenReturn(Optional.of(customer));

        service.updateCustomer(partialReq);

        assertThat(customer.getFirstname()).isEqualTo("John");
        assertThat(customer.getLastname()).isEqualTo("UpdatedLastName");
        assertThat(customer.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(customer.getAddress()).isEqualTo(address);
        verify(repository).save(customer);
    }

    @Test
    @DisplayName("updateCustomer should throw CustomerNotFoundException when customer not found")
    void shouldThrowExceptionWhenUpdatingNonExistentCustomer() {
        when(repository.findById("cust-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCustomer(request))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Cannot update customer:: No customer found with the provided ID:: cust-1");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("findAllCustomers should return all mapped customer responses")
    void shouldFindAllCustomers() {
        when(repository.findAll()).thenReturn(List.of(customer));
        when(mapper.fromCustomer(customer)).thenReturn(response);

        List<CustomerResponse> results = service.findAllCustomers();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).email()).isEqualTo("john.doe@example.com");
        verify(repository).findAll();
        verify(mapper).fromCustomer(customer);
    }

    @Test
    @DisplayName("existsById should return true when customer exists")
    void shouldReturnTrueWhenCustomerExists() {
        when(repository.findById("cust-1")).thenReturn(Optional.of(customer));

        Boolean exists = service.existsById("cust-1");

        assertThat(exists).isTrue();
        verify(repository).findById("cust-1");
    }

    @Test
    @DisplayName("existsById should return false when customer does not exist")
    void shouldReturnFalseWhenCustomerDoesNotExist() {
        when(repository.findById("cust-unknown")).thenReturn(Optional.empty());

        Boolean exists = service.existsById("cust-unknown");

        assertThat(exists).isFalse();
        verify(repository).findById("cust-unknown");
    }

    @Test
    @DisplayName("findById should return CustomerResponse when customer found")
    void shouldFindCustomerById() {
        when(repository.findById("cust-1")).thenReturn(Optional.of(customer));
        when(mapper.fromCustomer(customer)).thenReturn(response);

        CustomerResponse result = service.findById("cust-1");

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("cust-1");
        assertThat(result.firstname()).isEqualTo("John");
        verify(repository).findById("cust-1");
    }

    @Test
    @DisplayName("findById should throw CustomerNotFoundException when customer not found")
    void shouldThrowExceptionWhenFindByIdNotFound() {
        when(repository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById("unknown-id"))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("No customer found with the provided ID: unknown-id");

        verify(repository).findById("unknown-id");
    }

    @Test
    @DisplayName("deleteCustomer should execute without errors")
    void shouldDeleteCustomer() {
        service.deleteCustomer("cust-1");
    }
}
