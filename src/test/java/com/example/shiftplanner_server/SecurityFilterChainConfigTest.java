package com.example.shiftplanner_server;

import com.example.shiftplanner_server.services.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig
@WebAppConfiguration
@ContextConfiguration(classes = SecurityFilterChainConfigTest.SecurityMvcTestConfig.class)
class SecurityFilterChainConfigTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private jakarta.servlet.Filter springSecurityFilterChain;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .addFilters(springSecurityFilterChain)
            .build();
    }

    @Test
    void apiEndpointWithoutAuthorizationHeaderReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/test").servletPath("/api/test"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("{\"reason\":\"Missing or invalid Authorization header\"}"));
    }

    @Test
    void loginEndpointIsAccessibleWithoutAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/auth/login").servletPath("/api/auth/login"))
            .andExpect(status().isOk())
            .andExpect(content().string("login"));
    }

    @Test
    void nonApiEndpointIsAccessibleWithoutAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/public/test").servletPath("/public/test"))
            .andExpect(status().isOk())
            .andExpect(content().string("public"));
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import(SecurityFilterChainConfig.class)
    static class SecurityMvcTestConfig {

        @Bean
        TokenService tokenService() {
            TokenService tokenService = mock(TokenService.class);
            when(tokenService.getByToken(anyString())).thenReturn(Optional.empty());
            return tokenService;
        }

        @Bean
        TokenAuthenticationFilter tokenAuthenticationFilter(TokenService tokenService) {
            return new TokenAuthenticationFilter(tokenService);
        }

        @Bean
        TestController testController() {
            return new TestController();
        }
    }

    @RestController
    static class TestController {

        @GetMapping("/api/test")
        String apiTest() {
            return "api";
        }

        @GetMapping("/api/auth/login")
        String login() {
            return "login";
        }

        @GetMapping("/public/test")
        String publicEndpoint() {
            return "public";
        }
    }
}

