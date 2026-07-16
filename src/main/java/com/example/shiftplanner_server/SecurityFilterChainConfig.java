package com.example.shiftplanner_server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityFilterChainConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for stateless REST APIs
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/password/encode").permitAll() // Allow public access to this route
                .anyRequest().authenticated()                    // Protect everything else
            );
        return http.build();
    }
}