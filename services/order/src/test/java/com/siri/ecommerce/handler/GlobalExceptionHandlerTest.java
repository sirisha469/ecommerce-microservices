package com.siri.ecommerce.handler;

import com.siri.ecommerce.exception.BusinessException;
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
    @DisplayName("handle BusinessException should return 400 BAD_REQUEST with message")
    void shouldHandleBusinessException() {
        BusinessException exception = new BusinessException("Order cannot be created");

        ResponseEntity<String> response = exceptionHandler.handle(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Order cannot be created");
    }

    @Test
    @DisplayName("handle EntityNotFoundException should return 400 BAD_REQUEST with message")
    void shouldHandleEntityNotFoundException() {
        EntityNotFoundException exception = new EntityNotFoundException("Order not found with ID: 1");

        ResponseEntity<String> response = exceptionHandler.handle(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Order not found with ID: 1");
    }

    @Test
    @DisplayName("handle MethodArgumentNotValidException should return 400 BAD_REQUEST with field errors map")
    void shouldHandleMethodArgumentNotValidException() throws NoSuchMethodException {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "orderRequest");
        bindingResult.addError(new FieldError("orderRequest", "customerId", "Customer should be present"));
        bindingResult.addError(new FieldError("orderRequest", "amount", "Order amount should be greater than zero"));

        MethodParameter parameter = new MethodParameter(
                this.getClass().getDeclaredMethod("setUp"), -1
        );
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handle(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error())
                .containsEntry("customerId", "Customer should be present")
                .containsEntry("amount", "Order amount should be greater than zero");
    }
}
