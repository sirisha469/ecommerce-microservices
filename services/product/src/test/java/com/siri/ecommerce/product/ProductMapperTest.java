package com.siri.ecommerce.product;

import com.siri.ecommerce.category.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private ProductMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductMapper();
    }

    @Test
    @DisplayName("toProduct should map ProductRequest to Product entity correctly")
    void shouldMapProductRequestToProduct() {
        ProductRequest request = new ProductRequest(
                5,
                "Monitor",
                "4K Gaming Monitor",
                15.0,
                BigDecimal.valueOf(399.99),
                2
        );

        Product product = mapper.toProduct(request);

        assertThat(product).isNotNull();
        assertThat(product.getId()).isEqualTo(5);
        assertThat(product.getName()).isEqualTo("Monitor");
        assertThat(product.getDescription()).isEqualTo("4K Gaming Monitor");
        assertThat(product.getAvailableQuantity()).isEqualTo(15.0);
        assertThat(product.getPrice()).isEqualTo(BigDecimal.valueOf(399.99));
        assertThat(product.getCategory()).isNotNull();
        assertThat(product.getCategory().getId()).isEqualTo(2);
    }

    @Test
    @DisplayName("toProductResponse should map Product entity and Category to ProductResponse")
    void shouldMapProductToProductResponse() {
        Category category = Category.builder()
                .id(2)
                .name("Displays")
                .description("Computer displays and monitors")
                .build();

        Product product = Product.builder()
                .id(5)
                .name("Monitor")
                .description("4K Gaming Monitor")
                .availableQuantity(15.0)
                .price(BigDecimal.valueOf(399.99))
                .category(category)
                .build();

        ProductResponse response = mapper.toProductResponse(product);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(5);
        assertThat(response.name()).isEqualTo("Monitor");
        assertThat(response.description()).isEqualTo("4K Gaming Monitor");
        assertThat(response.availableQuantity()).isEqualTo(15.0);
        assertThat(response.price()).isEqualTo(BigDecimal.valueOf(399.99));
        assertThat(response.categoryId()).isEqualTo(2);
        assertThat(response.categoryName()).isEqualTo("Displays");
        assertThat(response.categoryDescription()).isEqualTo("Computer displays and monitors");
    }

    @Test
    @DisplayName("toProductPurchaseResponse should map Product and purchased quantity")
    void shouldMapProductToProductPurchaseResponse() {
        Product product = Product.builder()
                .id(5)
                .name("Monitor")
                .description("4K Gaming Monitor")
                .price(BigDecimal.valueOf(399.99))
                .build();

        ProductPurchaseResponse purchaseResponse = mapper.toProductPurchaseResponse(product, 3.0);

        assertThat(purchaseResponse).isNotNull();
        assertThat(purchaseResponse.productId()).isEqualTo(5);
        assertThat(purchaseResponse.name()).isEqualTo("Monitor");
        assertThat(purchaseResponse.description()).isEqualTo("4K Gaming Monitor");
        assertThat(purchaseResponse.price()).isEqualTo(BigDecimal.valueOf(399.99));
        assertThat(purchaseResponse.quantity()).isEqualTo(3.0);
    }
}
