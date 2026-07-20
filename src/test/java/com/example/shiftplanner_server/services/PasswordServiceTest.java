package com.example.shiftplanner_server.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordService passwordService;

    @Test
    void encodeDelegatesToPasswordEncoder() {
        when(passwordEncoder.encode("raw-pass")).thenReturn("encoded-pass");

        String encoded = passwordService.encode("raw-pass");

        assertEquals("encoded-pass", encoded);
        verify(passwordEncoder).encode("raw-pass");
    }

    @Test
    void verifyDelegatesToPasswordEncoderMatches() {
        when(passwordEncoder.matches("candidate", "stored-hash")).thenReturn(true);

        boolean matches = passwordService.verify("candidate", "stored-hash");

        assertTrue(matches);
        verify(passwordEncoder).matches("candidate", "stored-hash");
    }
}

