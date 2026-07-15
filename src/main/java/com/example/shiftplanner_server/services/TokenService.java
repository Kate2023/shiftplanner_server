package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.entities.Token;
import com.example.shiftplanner_server.repositories.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenRepository tokenRepository;


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

