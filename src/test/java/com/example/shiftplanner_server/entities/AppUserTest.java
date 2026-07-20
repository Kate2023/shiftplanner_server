package com.example.shiftplanner_server.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppUserTest {

    @Test
    void gettersAndSettersWork() {
        AppUser appUser = new AppUser();

        appUser.setUserId(1);
        appUser.setUsername("manager");
        appUser.setPassword("secret");
        appUser.setManager(true);

        assertEquals(1, appUser.getUserId());
        assertEquals("manager", appUser.getUsername());
        assertEquals("secret", appUser.getPassword());
        assertTrue(appUser.isManager());

        appUser.setManager(false);
        assertFalse(appUser.isManager());
    }
}

