package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.entities.AppUser;
import com.example.shiftplanner_server.entities.Token;
import com.example.shiftplanner_server.repositories.TokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenService tokenService;

    @Test
    void issueTokenCreatesAndSavesTokenForUser() {
        AppUser appUser = new AppUser();
        appUser.setUserId(1);

        when(tokenRepository.save(any(Token.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String issuedToken = tokenService.issueToken(appUser);

        ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        Token savedToken = tokenCaptor.getValue();

        assertFalse(issuedToken.isBlank());
        assertEquals(issuedToken, savedToken.getToken());
        assertSame(appUser, savedToken.getUser());
        assertNotNull(savedToken.getCreatedOn());
        assertNotNull(UUID.fromString(issuedToken));
    }

    @Test
    void getByTokenDelegatesToRepository() {
        Token token = new Token();
        token.setToken("known-token");

        when(tokenRepository.findByToken("known-token")).thenReturn(Optional.of(token));

        Optional<Token> result = tokenService.getByToken("known-token");

        assertTrue(result.isPresent());
        assertSame(token, result.get());
        verify(tokenRepository).findByToken("known-token");
    }

    @Test
    void saveSetsCreatedOnWhenMissing() {
        Token token = new Token();
        token.setToken("token-1");
        token.setUser(new AppUser());
        token.setCreatedOn(null);

        when(tokenRepository.save(token)).thenReturn(token);

        LocalDateTime before = LocalDateTime.now();
        Token saved = tokenService.save(token);
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(saved.getCreatedOn());
        assertTrue(!saved.getCreatedOn().isBefore(before) && !saved.getCreatedOn().isAfter(after));
        verify(tokenRepository).save(token);
    }

    @Test
    void savePreservesCreatedOnWhenAlreadySet() {
        LocalDateTime fixedTime = LocalDateTime.of(2026, 7, 20, 12, 0);

        Token token = new Token();
        token.setToken("token-2");
        token.setUser(new AppUser());
        token.setCreatedOn(fixedTime);

        when(tokenRepository.save(token)).thenReturn(token);

        Token saved = tokenService.save(token);

        assertEquals(fixedTime, saved.getCreatedOn());
        verify(tokenRepository).save(token);
    }
}

