package com.example.shiftplanner_server.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScheduleTest {

    @Test
    void defaultPoliciesIsEmptyString() {
        Schedule schedule = new Schedule();

        assertEquals("", schedule.getPolicies());
    }

    @Test
    void gettersAndSettersWork() {
        Staff roster = new Staff();
        roster.setStaffId(1);

        Staff banking = new Staff();
        banking.setStaffId(2);

        Staff backup = new Staff();
        backup.setStaffId(3);

        Staff inspector = new Staff();
        inspector.setStaffId(4);

        Schedule schedule = new Schedule();
        schedule.setScheduleId(10);
        schedule.setDate(LocalDate.of(2026, 7, 20));
        schedule.setNotes("Busy day");
        schedule.setRosterStaff(roster);
        schedule.setBankingStaff(banking);
        schedule.setBankingBackupStaff(backup);
        schedule.setBuildingInspector(inspector);
        schedule.setPolicies("P1,P2");

        assertEquals(10, schedule.getScheduleId());
        assertEquals(LocalDate.of(2026, 7, 20), schedule.getDate());
        assertEquals("Busy day", schedule.getNotes());
        assertEquals(roster, schedule.getRosterStaff());
        assertEquals(banking, schedule.getBankingStaff());
        assertEquals(backup, schedule.getBankingBackupStaff());
        assertEquals(inspector, schedule.getBuildingInspector());
        assertEquals("P1,P2", schedule.getPolicies());
    }
}

