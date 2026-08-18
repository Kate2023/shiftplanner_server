package com.example.shiftplanner_server.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StaffTest {

    @Test
    void gettersAndSettersWork() {
        Staff staff = new Staff();

        staff.setStaffId(7);
        staff.setStaffName("Alex");
        staff.setActive(true);

        assertEquals(7, staff.getStaffId());
        assertEquals("Alex", staff.getStaffName());
        assertTrue(staff.isActive());

        staff.setActive(false);
        assertFalse(staff.isActive());
    }
}

