package com.siri.ecommerce.handler;

import com.siri.ecommerce.exception.ProductPurchaseException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handle ProductPurchaseException should return 404 with error message")
    void shouldHandleProductPurchaseException() {
        ProductPurchaseException ex = new ProductPurchaseException("Insufficient stock");

        ResponseEntity<String> response = exceptionHandler.handle(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Insufficient stock");
    }

    @Test
    @DisplayName("handle EntityNotFoundException should return 404 with error message")
    void shouldHandleEntityNotFoundException() {
        EntityNotFoundException ex = new EntityNotFoundException("Product not found with ID: 1");

        ResponseEntity<String> response = exceptionHandler.handle(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Product not found with ID: 1");
    }

    @Test
    @DisplayName("handle MethodArgumentNotValidException should return 404 with field error map")
    void shouldHandleMethodArgumentNotValidException() throws NoSuchMethodException {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "productRequest");
        bindingResult.addError(new FieldError("productRequest", "name", "Product name is required"));
        bindingResult.addError(new FieldError("productRequest", "price", "Product price should be positive"));

        MethodParameter parameter = new MethodParameter(
                this.getClass().getDeclaredMethod("setUp"), -1
        );
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handle(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error())
                .containsEntry("name", "Product name is required")
                .containsEntry("price", "Product price should be positive");
    }
}
