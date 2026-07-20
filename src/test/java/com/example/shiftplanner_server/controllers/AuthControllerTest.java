package com.example.shiftplanner_server.controllers;

import com.example.shiftplanner_server.model.LoginRequest;
import com.example.shiftplanner_server.model.LoginResponse;
import com.example.shiftplanner_server.services.AppUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AppUserService appUserService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(appUserService)).build();
    }

    @Test
    void loginReturnsOkAndResponseBody() throws Exception {
        LoginResponse response = new LoginResponse()
            .token("token-123")
            .username("alice")
            .role("manager");

        when(appUserService.login(any(LoginRequest.class))).thenReturn(response);

        LoginRequest request = new LoginRequest()
            .role("manager")
            .username("alice")
            .password("secret");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("token-123"))
            .andExpect(jsonPath("$.username").value("alice"))
            .andExpect(jsonPath("$.role").value("manager"));

        verify(appUserService).login(any(LoginRequest.class));
    }
}

