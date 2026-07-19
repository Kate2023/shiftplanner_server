package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.entities.AppUser;
import com.example.shiftplanner_server.entities.Token;
import com.example.shiftplanner_server.repositories.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenRepository tokenRepository;

    public String issueToken(AppUser appUser) {
        String token = UUID.randomUUID().toString();
        save(new Token(token, appUser));
        return token;
    }

    public Optional<Token> getByToken(String token) {
        return tokenRepository.findByToken(token);
    }

    public Token save(Token token) {
        if (token.getCreatedOn() == null) {
            token.setCreatedOn(LocalDateTime.now());
        }
        return tokenRepository.save(token);
    }
}

