package com.siri.ecommerce.handler;

import com.siri.ecommerce.exception.CustomerNotFoundException;
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
    @DisplayName("handle CustomerNotFoundException should return 404 NOT_FOUND with exception message")
    void shouldHandleCustomerNotFoundException() {
        CustomerNotFoundException exception = new CustomerNotFoundException("Customer not found with id: 123");

        ResponseEntity<String> response = exceptionHandler.handle(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Customer not found with id: 123");
    }

    @Test
    @DisplayName("handle MethodArgumentNotValidException should return 404 with mapped field errors")
    void shouldHandleMethodArgumentNotValidException() throws NoSuchMethodException {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "customerRequest");
        bindingResult.addError(new FieldError("customerRequest", "firstname", "Customer firstname is required"));
        bindingResult.addError(new FieldError("customerRequest", "email", "Customer email is not a valid email address"));

        MethodParameter parameter = new MethodParameter(
                this.getClass().getDeclaredMethod("setUp"), -1
        );
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handle(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error())
                .containsEntry("firstname", "Customer firstname is required")
                .containsEntry("email", "Customer email is not a valid email address");
    }
}
