package com.hms.user.exception;

import com.hms.user.utility.ErrorInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private Environment environment;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Test
    @DisplayName("exceptionHandler(HmsException) should resolve property from Environment and return 500")
    void shouldHandleHmsException() {
        HmsException exception = new HmsException("USER_ALREADY_EXISTS");
        when(environment.getProperty("USER_ALREADY_EXISTS")).thenReturn("User with given email already exists");

        ResponseEntity<ErrorInfo> response = exceptionHandler.exceptionHandler(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorMessage()).isEqualTo("User with given email already exists");
    }

    @Test
    @DisplayName("exceptionHandler(Exception) should return 500 with default message")
    void shouldHandleGeneralException() {
        Exception exception = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorInfo> response = exceptionHandler.exceptionHandler(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorMessage()).isEqualTo("some error occurred");
    }

    @Test
    @DisplayName("handleValidationException should return 400 with validation errors")
    void shouldHandleValidationException() throws NoSuchMethodException {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "userDTO");
        bindingResult.addError(new FieldError("userDTO", "email", "Invalid email"));

        MethodParameter parameter = new MethodParameter(
                this.getClass().getDeclaredMethod("shouldHandleGeneralException"), -1
        );
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorInfo> response = exceptionHandler.handleValidationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorMessage()).contains("Invalid email");
    }
}
