package com.hms.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.user.dto.Roles;
import com.hms.user.dto.UserDTO;
import com.hms.user.service.UserService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        userDTO = new UserDTO(1L, "Bob", "bob@example.com", "secret123", Roles.PATIENT);
    }

    @Test
    @DisplayName("POST /users/register should return 201 CREATED with ResponseDTO")
    void shouldRegisterUser() throws Exception {
        doNothing().when(userService).registerUser(any(UserDTO.class));

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Account created."));

        verify(userService).registerUser(any(UserDTO.class));
    }

    @Test
    @DisplayName("POST /users/login should return 200 OK with UserDTO")
    void shouldLoginUser() throws Exception {
        UserDTO loggedInUser = new UserDTO(1L, "Bob", "bob@example.com", "null", Roles.PATIENT);
        when(userService.loginUser(any(UserDTO.class))).thenReturn(loggedInUser);

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Bob"))
                .andExpect(jsonPath("$.email").value("bob@example.com"));

        verify(userService).loginUser(any(UserDTO.class));
    }
}
