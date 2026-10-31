package com.siri.ecommerce.product;

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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ProductRequest productRequest;
    private ProductResponse productResponse;
    private ProductPurchaseResponse purchaseResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();

        productRequest = new ProductRequest(
                1,
                "Keyboard",
                "Mechanical Keyboard",
                100.0,
                BigDecimal.valueOf(89.99),
                1
        );

        productResponse = new ProductResponse(
                1,
                "Keyboard",
                "Mechanical Keyboard",
                100.0,
                BigDecimal.valueOf(89.99),
                1,
                "Accessories",
                "Computer accessories"
        );

        purchaseResponse = new ProductPurchaseResponse(
                1,
                "Keyboard",
                "Mechanical Keyboard",
                BigDecimal.valueOf(89.99),
                2.0
        );
    }

    @Test
    @DisplayName("POST /api/v1/products should return 200 OK with created product ID")
    void shouldCreateProduct() throws Exception {
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(1);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(productService).createProduct(any(ProductRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/products/purchase should return 200 OK with purchase responses")
    void shouldPurchaseProducts() throws Exception {
        List<ProductPurchaseRequest> purchaseRequests = List.of(new ProductPurchaseRequest(1, 2.0));
        when(productService.purchaseProducts(any())).thenReturn(List.of(purchaseResponse));

        mockMvc.perform(post("/api/v1/products/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseRequests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].name").value("Keyboard"))
                .andExpect(jsonPath("$[0].quantity").value(2.0));

        verify(productService).purchaseProducts(any());
    }

    @Test
    @DisplayName("GET /api/v1/products/{product-id} should return 200 OK with ProductResponse")
    void shouldFindProductById() throws Exception {
        when(productService.findById(1)).thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/products/{product-id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.categoryName").value("Accessories"));

        verify(productService).findById(1);
    }

    @Test
    @DisplayName("GET /api/v1/products should return 200 OK with all products")
    void shouldFindAllProducts() throws Exception {
        when(productService.findAll()).thenReturn(List.of(productResponse));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Keyboard"));

        verify(productService).findAll();
    }
}
