package com.example.shiftplanner_server.controllers;

import com.example.shiftplanner_server.services.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
public class PasswordController {
    private final PasswordService passwordService;

    /**
     * Accepts a raw password from a JSON body and returns the securely encoded hash.
     * Endpoint: POST <a href="http://localhost:8080/api/password/encode">http://localhost:8080/api/password/encode</a>
     * Request Body Example:
     * Windows:
     * curl -X POST http://localhost:8080/api/password/encode -H "Content-Type: application/json" -d "{\"password\": \"librarian2026\"}"
     * Linux:
     * curl -X POST http://localhost:8080/api/password/encode -H "Content-Type: application/json" -d '{"password": "MySuperSecretPassword123!"}'
     */
    @PostMapping("/encode")
    public ResponseEntity<Map<String, String>> encodePassword(@RequestBody Map<String, String> request) {
        String rawPassword = request.get("password");

        // Fail early if the password payload is missing or empty
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password field is required"));
        }

        // Return the Generate the secure BCrypt hash
        return ResponseEntity.ok(Map.of("encodedPassword", passwordService.encode(rawPassword)));
    }
}

