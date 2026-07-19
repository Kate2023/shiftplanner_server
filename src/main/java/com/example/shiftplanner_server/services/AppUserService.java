package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.model.LoginRequest;
import com.example.shiftplanner_server.model.LoginResponse;
import com.example.shiftplanner_server.entities.AppUser;
import com.example.shiftplanner_server.repositories.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AppUserService {
    private final AppUserRepository appUserRepository;
    private final TokenService tokenService;
    private final PasswordService passwordService;

    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        AppUser appUser = appUserRepository.findAppUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        // Use PasswordService to verify the password
        if (!passwordService.verify(password, appUser.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = tokenService.issueToken(appUser);
        return new LoginResponse()
                .token(token)
                .username(appUser.getUsername())
                .role(appUser.isManager() ? "manager" : "staff");
    }
}

