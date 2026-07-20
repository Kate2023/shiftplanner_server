package com.example.shiftplanner_server.entities;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaffTest {

    @Test
    void gettersAndSettersWork() {
        Staff staff = new Staff();

        staff.setStaffId(7);
        staff.setStaffName("Alex");
        staff.setWorkingHours(new BigDecimal("37.50"));
        staff.setLunchBreak(new BigDecimal("1.00"));
        staff.setActive(true);

        assertEquals(7, staff.getStaffId());
        assertEquals("Alex", staff.getStaffName());
        assertEquals(new BigDecimal("37.50"), staff.getWorkingHours());
        assertEquals(new BigDecimal("1.00"), staff.getLunchBreak());
        assertTrue(staff.isActive());

        staff.setActive(false);
        assertFalse(staff.isActive());
    }
}

