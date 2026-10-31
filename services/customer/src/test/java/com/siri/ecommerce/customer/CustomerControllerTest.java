package com.siri.ecommerce.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CustomerService service;

    @InjectMocks
    private CustomerController controller;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CustomerRequest request;
    private CustomerResponse response;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        Address address = new Address("Main St", "123", "500001");
        request = new CustomerRequest("cust-1", "John", "Doe", "john.doe@example.com", address);
        response = new CustomerResponse("cust-1", "John", "Doe", "john.doe@example.com", address);
    }

    @Test
    @DisplayName("POST /api/v1/customers should return 200 OK with created customer ID")
    void shouldCreateCustomer() throws Exception {
        when(service.createCustomer(any(CustomerRequest.class))).thenReturn("cust-1");

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("cust-1"));

        verify(service).createCustomer(any(CustomerRequest.class));
    }

    @Test
    @DisplayName("PUT /api/v1/customers should return 202 Accepted")
    void shouldUpdateCustomer() throws Exception {
        mockMvc.perform(put("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(service).updateCustomer(any(CustomerRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/customers should return 200 OK with list of customers")
    void shouldFindAllCustomers() throws Exception {
        when(service.findAllCustomers()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value("cust-1"))
                .andExpect(jsonPath("$[0].firstname").value("John"))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));

        verify(service).findAllCustomers();
    }

    @Test
    @DisplayName("GET /api/v1/customers/exits/{customer-id} should return 200 OK with boolean")
    void shouldCheckCustomerExists() throws Exception {
        when(service.existsById("cust-1")).thenReturn(true);

        mockMvc.perform(get("/api/v1/customers/exits/{customer-id}", "cust-1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(service).existsById("cust-1");
    }

    @Test
    @DisplayName("GET /api/v1/customers/{customer-id} should return 200 OK with CustomerResponse")
    void shouldFindCustomerById() throws Exception {
        when(service.findById("cust-1")).thenReturn(response);

        mockMvc.perform(get("/api/v1/customers/{customer-id}", "cust-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("cust-1"))
                .andExpect(jsonPath("$.firstname").value("John"))
                .andExpect(jsonPath("$.lastname").value("Doe"))
                .andExpect(jsonPath("$.address.street").value("Main St"));

        verify(service).findById("cust-1");
    }

    @Test
    @DisplayName("DELETE /api/v1/customers/{customer-id} should return 202 Accepted")
    void shouldDeleteCustomer() throws Exception {
        mockMvc.perform(delete("/api/v1/customers/{customer-id}", "cust-1"))
                .andExpect(status().isAccepted());

        verify(service).deleteCustomer("cust-1");
    }
}
