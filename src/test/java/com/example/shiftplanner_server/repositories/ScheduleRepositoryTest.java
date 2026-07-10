package com.example.shiftplanner_server.repositories;

import com.example.shiftplanner_server.entities.Schedule;
import com.example.shiftplanner_server.entities.Staff;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class ScheduleRepositoryTest extends PostgresRepositoryTestBase {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private StaffRepository staffRepository;

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
        Staff staff = saveStaff("Roster Staff");

        Schedule schedule = new Schedule();
        schedule.setDate(LocalDate.of(2026, 7, 11));
        schedule.setNotes("Morning shift");
        schedule.setRosterStaff(staff);

        Schedule saved = scheduleRepository.save(schedule);

        assertTrue(scheduleRepository.findById(saved.getScheduleId()).isPresent());
    }

    @Test
    void findByDateReturnsExpectedSchedule() {
        Staff staff = saveStaff("Inspector Staff");

        Schedule schedule = new Schedule();
        LocalDate date = LocalDate.of(2026, 7, 12);
        schedule.setDate(date);
        schedule.setNotes("Weekend schedule");
        schedule.setBuildingInspector(staff);

        scheduleRepository.save(schedule);

        assertTrue(scheduleRepository.findByDate(date).isPresent());
        assertEquals("Weekend schedule", scheduleRepository.findByDate(date).get().getNotes());
    }

    @Test
    void deleteByIdRemovesSchedule() {
        Staff staff = saveStaff("Delete Schedule Staff");

        Schedule schedule = new Schedule();
        schedule.setDate(LocalDate.of(2026, 7, 13));
        schedule.setNotes("To be deleted");
        schedule.setBankingStaff(staff);

        Schedule saved = scheduleRepository.save(schedule);
        scheduleRepository.deleteById(saved.getScheduleId());

        assertFalse(scheduleRepository.findById(saved.getScheduleId()).isPresent());
    }

    private Staff saveStaff(String name) {
        Staff staff = new Staff();
        staff.setStaffName(name);
        staff.setWorkingHours(BigDecimal.valueOf(38));
        staff.setLunchBreak(BigDecimal.valueOf(60));
        staff.setActive(true);
        return staffRepository.save(staff);
    }
}

