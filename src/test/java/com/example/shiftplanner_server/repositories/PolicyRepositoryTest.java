package com.example.shiftplanner_server.repositories;

import com.example.shiftplanner_server.entities.Policy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class PolicyRepositoryTest extends PostgresRepositoryTestBase {

    @Autowired
    private PolicyRepository policyRepository;

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
        Policy policy = new Policy();
        policy.setDescription("At least 2 staff on floor");
        policy.setParam1(2);

        Policy saved = policyRepository.save(policy);

        assertTrue(policyRepository.findById(saved.getPolicyId()).isPresent());
    }

    @Test
    void findAllReturnsSavedPolicies() {
        Policy first = new Policy();
        first.setDescription("Policy A");
        first.setParam1(1);

        Policy second = new Policy();
        second.setDescription("Policy B");
        second.setParam1(3);

        policyRepository.save(first);
        policyRepository.save(second);

        List<Policy> all = policyRepository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void deleteByIdRemovesPolicy() {
        Policy policy = new Policy();
        policy.setDescription("Delete me");
        policy.setParam1(null);

        Policy saved = policyRepository.save(policy);
        policyRepository.deleteById(saved.getPolicyId());

        assertFalse(policyRepository.findById(saved.getPolicyId()).isPresent());
    }
}

