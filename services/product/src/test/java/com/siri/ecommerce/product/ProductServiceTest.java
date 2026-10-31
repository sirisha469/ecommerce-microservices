package com.siri.ecommerce.product;

import com.siri.ecommerce.category.Category;
import com.siri.ecommerce.exception.ProductPurchaseException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper mapper;

    @InjectMocks
    private ProductService productService;

    private Product product1;
    private Product product2;
    private Category category;
    private ProductRequest productRequest;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1)
                .name("Electronics")
                .description("Electronic gadgets")
                .build();

        product1 = Product.builder()
                .id(1)
                .name("Smartphone")
                .description("Latest smartphone")
                .availableQuantity(50.0)
                .price(BigDecimal.valueOf(699.99))
                .category(category)
                .build();

        product2 = Product.builder()
                .id(2)
                .name("Laptop")
                .description("Pro laptop")
                .availableQuantity(20.0)
                .price(BigDecimal.valueOf(1299.99))
                .category(category)
                .build();

        productRequest = new ProductRequest(
                1,
                "Smartphone",
                "Latest smartphone",
                50.0,
                BigDecimal.valueOf(699.99),
                1
        );

        productResponse = new ProductResponse(
                1,
                "Smartphone",
                "Latest smartphone",
                50.0,
                BigDecimal.valueOf(699.99),
                1,
                "Electronics",
                "Electronic gadgets"
        );
    }

    @Test
    @DisplayName("createProduct should map, save entity and return generated ID")
    void shouldCreateProductSuccessfully() {
        when(mapper.toProduct(productRequest)).thenReturn(product1);
        when(productRepository.save(product1)).thenReturn(product1);

        Integer resultId = productService.createProduct(productRequest);

        assertThat(resultId).isEqualTo(1);
        verify(mapper).toProduct(productRequest);
        verify(productRepository).save(product1);
    }

    @Test
    @DisplayName("purchaseProducts should deduct quantity, save updated product and return purchase response")
    void shouldPurchaseProductsSuccessfully() {
        List<ProductPurchaseRequest> requests = List.of(
                new ProductPurchaseRequest(1, 5.0),
                new ProductPurchaseRequest(2, 2.0)
        );

        when(productRepository.findAllByIdInOrderById(List.of(1, 2))).thenReturn(List.of(product1, product2));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductPurchaseResponse resp1 = new ProductPurchaseResponse(1, "Smartphone", "Latest smartphone", BigDecimal.valueOf(699.99), 5.0);
        ProductPurchaseResponse resp2 = new ProductPurchaseResponse(2, "Laptop", "Pro laptop", BigDecimal.valueOf(1299.99), 2.0);

        when(mapper.toProductPurchaseResponse(product1, 5.0)).thenReturn(resp1);
        when(mapper.toProductPurchaseResponse(product2, 2.0)).thenReturn(resp2);

        List<ProductPurchaseResponse> responses = productService.purchaseProducts(requests);

        assertThat(responses).hasSize(2);
        assertThat(product1.getAvailableQuantity()).isEqualTo(45.0);
        assertThat(product2.getAvailableQuantity()).isEqualTo(18.0);
        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    @DisplayName("purchaseProducts should throw ProductPurchaseException when a product does not exist")
    void shouldThrowExceptionWhenProductNotFoundForPurchase() {
        List<ProductPurchaseRequest> requests = List.of(
                new ProductPurchaseRequest(1, 5.0),
                new ProductPurchaseRequest(99, 1.0)
        );

        when(productRepository.findAllByIdInOrderById(List.of(1, 99))).thenReturn(List.of(product1));

        assertThatThrownBy(() -> productService.purchaseProducts(requests))
                .isInstanceOf(ProductPurchaseException.class)
                .hasMessageContaining("One or more products does not exits");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("purchaseProducts should throw ProductPurchaseException when stock is insufficient")
    void shouldThrowExceptionWhenInsufficientStock() {
        List<ProductPurchaseRequest> requests = List.of(
                new ProductPurchaseRequest(1, 100.0) // requested 100, available 50
        );

        when(productRepository.findAllByIdInOrderById(List.of(1))).thenReturn(List.of(product1));

        assertThatThrownBy(() -> productService.purchaseProducts(requests))
                .isInstanceOf(ProductPurchaseException.class)
                .hasMessageContaining("Insufficient stock quantity for product with ID: 1");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("findById should return ProductResponse when product exists")
    void shouldFindProductById() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product1));
        when(mapper.toProductResponse(product1)).thenReturn(productResponse);

        ProductResponse response = productService.findById(1);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1);
        assertThat(response.name()).isEqualTo("Smartphone");
        verify(productRepository).findById(1);
    }

    @Test
    @DisplayName("findById should throw EntityNotFoundException when product does not exist")
    void shouldThrowExceptionWhenProductNotFoundById() {
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(999))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found with ID: 999");

        verify(productRepository).findById(999);
    }

    @Test
    @DisplayName("findAll should return list of all products mapped to ProductResponse")
    void shouldFindAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(product1, product2));
        when(mapper.toProductResponse(product1)).thenReturn(productResponse);

        ProductResponse productResponse2 = new ProductResponse(
                2, "Laptop", "Pro laptop", 20.0, BigDecimal.valueOf(1299.99), 1, "Electronics", "Electronic gadgets"
        );
        when(mapper.toProductResponse(product2)).thenReturn(productResponse2);

        List<ProductResponse> allProducts = productService.findAll();

        assertThat(allProducts).hasSize(2);
        assertThat(allProducts.get(0).id()).isEqualTo(1);
        assertThat(allProducts.get(1).id()).isEqualTo(2);
        verify(productRepository).findAll();
    }
}
