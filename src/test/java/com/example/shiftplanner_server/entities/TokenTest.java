package com.example.shiftplanner_server.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenTest {

    @Test
    void gettersAndSettersWork() {
        AppUser appUser = new AppUser();
        appUser.setUserId(20);

        Token token = new Token();
        LocalDateTime createdOn = LocalDateTime.of(2026, 7, 20, 10, 0);

        token.setToken("abc123");
        token.setUser(appUser);
        token.setCreatedOn(createdOn);

        assertEquals("abc123", token.getToken());
        assertEquals(appUser, token.getUser());
        assertEquals(createdOn, token.getCreatedOn());
    }

    @Test
    void constructorSetsTokenUserAndCreationTime() {
        AppUser appUser = new AppUser();
        LocalDateTime before = LocalDateTime.now();

        Token token = new Token("jwt-token", appUser);

        LocalDateTime after = LocalDateTime.now();

        assertEquals("jwt-token", token.getToken());
        assertEquals(appUser, token.getUser());
        assertNotNull(token.getCreatedOn());
        assertTrue(!token.getCreatedOn().isBefore(before) && !token.getCreatedOn().isAfter(after));
    }
}

