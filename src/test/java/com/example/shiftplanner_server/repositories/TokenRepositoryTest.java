package com.example.shiftplanner_server.repositories;

import com.example.shiftplanner_server.entities.AppUser;
import com.example.shiftplanner_server.entities.Token;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class TokenRepositoryTest extends PostgresRepositoryTestBase {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        createTablesFromSql(jdbcTemplate);
    }

    @AfterEach
    void tearDown() {
        dropSchema(jdbcTemplate);
    }

    @Test
    void saveAndFindById() {
        Token token = buildToken("session-abc", "token.user1");

        Token saved = tokenRepository.save(token);
        Optional<Token> found = tokenRepository.findById(saved.getToken());

        assertTrue(found.isPresent());
        assertEquals("session-abc", found.get().getToken());
        assertEquals("token.user1", found.get().getUser().getUsername());
    }

    @Test
    void findByTokenReturnsSavedToken() {
        tokenRepository.save(buildToken("session-def", "token.user2"));

        Optional<Token> found = tokenRepository.findByToken("session-def");

        assertTrue(found.isPresent());
        assertEquals("token.user2", found.get().getUser().getUsername());
    }

    @Test
    void deleteByIdRemovesRow() {
        Token saved = tokenRepository.save(buildToken("session-delete", "token.user3"));

        tokenRepository.deleteById(saved.getToken());

        assertFalse(tokenRepository.findById(saved.getToken()).isPresent());
    }

    private Token buildToken(String value, String username) {
        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setPassword("pwd");
        appUser.setManager(false);
        AppUser savedUser = appUserRepository.save(appUser);

        Token token = new Token();
        token.setToken(value);
        token.setUser(savedUser);
        token.setCreatedOn(LocalDateTime.of(2026, 7, 15, 9, 0));
        return token;
    }
}

