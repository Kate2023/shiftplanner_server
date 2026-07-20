package com.example.shiftplanner_server.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PolicyTest {

    @Test
    void gettersAndSettersWork() {
        Policy policy = new Policy();

        policy.setPolicyId(5);
        policy.setDescription("Minimum staff required");
        policy.setParam1(2);

        assertEquals(5, policy.getPolicyId());
        assertEquals("Minimum staff required", policy.getDescription());
        assertEquals(2, policy.getParam1());
    }
}

