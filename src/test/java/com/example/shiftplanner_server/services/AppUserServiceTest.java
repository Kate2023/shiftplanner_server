package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.entities.AppUser;
import com.example.shiftplanner_server.model.LoginRequest;
import com.example.shiftplanner_server.model.LoginResponse;
import com.example.shiftplanner_server.repositories.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private AppUserService appUserService;

    @Test
    void loginReturnsTokenUsernameAndManagerRole() {
        LoginRequest request = new LoginRequest()
            .username("manager1")
            .password("raw-password")
            .role("manager");

        AppUser appUser = new AppUser();
        appUser.setUsername("manager1");
        appUser.setPassword("hashed-password");
        appUser.setManager(true);

        when(appUserRepository.findAppUserByUsername("manager1")).thenReturn(Optional.of(appUser));
        when(passwordService.verify("raw-password", "hashed-password")).thenReturn(true);
        when(tokenService.issueToken(appUser)).thenReturn("token-123");

        LoginResponse response = appUserService.login(request);

        assertEquals("token-123", response.getToken());
        assertEquals("manager1", response.getUsername());
        assertEquals("manager", response.getRole());
        verify(tokenService).issueToken(appUser);
    }

    @Test
    void loginThrowsUnauthorizedWhenUserNotFound() {
        LoginRequest request = new LoginRequest()
            .username("unknown")
            .password("raw-password")
            .role("staff");

        when(appUserRepository.findAppUserByUsername("unknown")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> appUserService.login(request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid credentials", exception.getReason());
    }

    @Test
    void loginThrowsUnauthorizedWhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest()
            .username("staff1")
            .password("wrong-password")
            .role("staff");

        AppUser appUser = new AppUser();
        appUser.setUsername("staff1");
        appUser.setPassword("hashed-password");
        appUser.setManager(false);

        when(appUserRepository.findAppUserByUsername("staff1")).thenReturn(Optional.of(appUser));
        when(passwordService.verify("wrong-password", "hashed-password")).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> appUserService.login(request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid credentials", exception.getReason());
    }
}

