package com.hms.user.service;

import com.hms.user.dto.Roles;
import com.hms.user.dto.UserDTO;
import com.hms.user.entity.User;
import com.hms.user.exception.HmsException;
import com.hms.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Alice", "alice@example.com", "encodedPassword", Roles.ADMIN);
        userDTO = new UserDTO(1L, "Alice", "alice@example.com", "plainPassword", Roles.ADMIN);
    }

    @Test
    @DisplayName("registerUser should encode password and save user when email is not taken")
    void shouldRegisterUserSuccessfully() throws HmsException {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.registerUser(userDTO);

        verify(userRepository).findByEmail("alice@example.com");
        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("registerUser should throw HmsException when email already exists")
    void shouldThrowExceptionWhenRegisteringExistingUser() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.registerUser(userDTO))
                .isInstanceOf(HmsException.class)
                .hasMessage("USER_ALREADY_EXISTS");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("loginUser should succeed and return UserDTO with masked password when credentials are valid")
    void shouldLoginSuccessfully() throws HmsException {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plainPassword", "encodedPassword")).thenReturn(true);

        UserDTO result = userService.loginUser(userDTO);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        assertThat(result.getPassword()).isEqualTo("null");
        verify(passwordEncoder).matches("plainPassword", "encodedPassword");
    }

    @Test
    @DisplayName("loginUser should throw HmsException when user not found")
    void shouldThrowExceptionWhenUserNotFoundForLogin() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        userDTO.setEmail("unknown@example.com");

        assertThatThrownBy(() -> userService.loginUser(userDTO))
                .isInstanceOf(HmsException.class)
                .hasMessage("USER_NOT_FOUND");
    }

    @Test
    @DisplayName("loginUser should throw HmsException when credentials do not match")
    void shouldThrowExceptionWhenCredentialsInvalid() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);
        userDTO.setPassword("wrongPassword");

        assertThatThrownBy(() -> userService.loginUser(userDTO))
                .isInstanceOf(HmsException.class)
                .hasMessage("INVALID_CREDENTIALS");
    }

    @Test
    @DisplayName("getUserById should return user when found")
    void shouldGetUserById() throws HmsException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("getUserById should throw HmsException when not found")
    void shouldThrowExceptionWhenUserNotFoundById() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(HmsException.class)
                .hasMessage("USER_NOT_FOUND");
    }
}
