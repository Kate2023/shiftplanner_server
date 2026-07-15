package com.example.shiftplanner_server.controllers;

import com.example.shiftplanner_server.model.LoginRequest;
import com.example.shiftplanner_server.model.LoginResponse;
import com.example.shiftplanner_server.services.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AppUserService appUserService;


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(appUserService.login(request));
    }
}

