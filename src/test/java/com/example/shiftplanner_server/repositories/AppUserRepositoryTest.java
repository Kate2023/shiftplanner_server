package com.example.shiftplanner_server.repositories;

import com.example.shiftplanner_server.entities.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class AppUserRepositoryTest extends PostgresRepositoryTestBase {

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
        AppUser appUser = new AppUser();
        appUser.setUsername("manager.user");
        appUser.setPassword("secret123");
        appUser.setManager(true);

        AppUser saved = appUserRepository.save(appUser);
        Optional<AppUser> found = appUserRepository.findById(saved.getUserId());

        assertTrue(found.isPresent());
        assertEquals("manager.user", found.get().getUsername());
        assertTrue(found.get().isManager());
    }

    @Test
    void findAppUserByUsername() {
        AppUser appUser = new AppUser();
        appUser.setUsername("staff.member");
        appUser.setPassword("pwd");
        appUser.setManager(false);
        appUserRepository.save(appUser);

        Optional<AppUser> found = appUserRepository.findAppUserByUsername("staff.member");

        assertTrue(found.isPresent());
        assertNotNull(found.get().getUserId());
        assertFalse(found.get().isManager());
    }

    @Test
    void deleteByIdRemovesRow() {
        AppUser appUser = new AppUser();
        appUser.setUsername("to.delete");
        appUser.setPassword("pwd");
        appUser.setManager(false);

        AppUser saved = appUserRepository.save(appUser);
        appUserRepository.deleteById(saved.getUserId());

        assertFalse(appUserRepository.findById(saved.getUserId()).isPresent());
    }
}

