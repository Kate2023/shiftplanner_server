package com.example.shiftplanner_server.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordService {
    private final PasswordEncoder passwordEncoder;

    // Call this during user registration
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    // Call this during login verification
    public boolean verify(String passwordAttempt, String storedDbHash) {
        // Automatically extracts the salt from the hash and validates safely
        return passwordEncoder.matches(passwordAttempt, storedDbHash);
    }
}
