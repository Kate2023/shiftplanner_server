package com.example.shiftplanner_server.services;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceConstantTest {

    @Test
    void constantsExposeExpectedTimeBoundaries() {
        assertEquals(LocalTime.of(12, 0), ServiceConstant.LUNCH_START);
        assertEquals(LocalTime.of(14, 0), ServiceConstant.LUNCH_END);
        assertEquals(LocalTime.of(9, 0), ServiceConstant.ASSIGNMENT_START);
        assertEquals(LocalTime.of(18, 0), ServiceConstant.ASSIGNMENT_END);
        assertTrue(ServiceConstant.ASSIGNMENT_START.isBefore(ServiceConstant.ASSIGNMENT_END));
    }
}

