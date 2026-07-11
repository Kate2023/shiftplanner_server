package com.example.shiftplanner_server.repositories;

import com.example.shiftplanner_server.entities.Schedule;
import com.example.shiftplanner_server.entities.ScheduleAssignment;
import com.example.shiftplanner_server.entities.Staff;
import com.example.shiftplanner_server.entities.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class ScheduleAssignmentRepositoryTest extends PostgresRepositoryTestBase {

    @Autowired
    private ScheduleAssignmentRepository scheduleAssignmentRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private TaskRepository taskRepository;

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
        ScheduleAssignment assignment = buildAssignment(LocalDate.of(2026, 7, 14), "Desk", LocalTime.of(9, 0));

        ScheduleAssignment saved = scheduleAssignmentRepository.save(assignment);

        assertTrue(scheduleAssignmentRepository.findById(saved.getAssignmentId()).isPresent());
    }

    @Test
    void findAllByScheduleDateReturnsAssignments() {
        LocalDate targetDate = LocalDate.of(2026, 7, 15);
        scheduleAssignmentRepository.save(buildAssignment(targetDate, "Desk", LocalTime.of(10, 0)));
        scheduleAssignmentRepository.save(buildAssignment(targetDate, "Check-in", LocalTime.of(11, 0)));

        List<ScheduleAssignment> assignments = scheduleAssignmentRepository.findAllBySchedule_Date(targetDate);

        assertEquals(2, assignments.size());
    }

    @Test
    @Transactional
    void deleteAllByScheduleDateRemovesAssignments() {
        LocalDate targetDate = LocalDate.of(2026, 7, 16);
        scheduleAssignmentRepository.save(buildAssignment(targetDate, "Picking", LocalTime.of(12, 0)));
        assertFalse(scheduleAssignmentRepository.findAllBySchedule_Date(targetDate).isEmpty());

        scheduleAssignmentRepository.deleteAllBySchedule_Date(targetDate);
        assertTrue(scheduleAssignmentRepository.findAllBySchedule_Date(targetDate).isEmpty());
    }

    @Test
    void deleteByIdRemovesAssignment() {
        ScheduleAssignment assignment = buildAssignment(LocalDate.of(2026, 7, 17), "Roaming", LocalTime.of(13, 0));
        ScheduleAssignment saved = scheduleAssignmentRepository.save(assignment);

        scheduleAssignmentRepository.deleteById(saved.getAssignmentId());

        assertFalse(scheduleAssignmentRepository.findById(saved.getAssignmentId()).isPresent());
    }

    private ScheduleAssignment buildAssignment(LocalDate date, String taskName, LocalTime slot) {
        Staff staff = new Staff();
        staff.setStaffName("Staff " + taskName + " " + slot);
        staff.setWorkingHours(BigDecimal.valueOf(35));
        staff.setLunchBreak(BigDecimal.valueOf(60));
        staff.setActive(true);
        Staff savedStaff = staffRepository.save(staff);

        Task task = new Task();
        task.setTaskName(taskName + " " + slot);
        task.setColour("#1234ab");
        task.setLunch(false);
        task.setAuto(false);
        Task savedTask = taskRepository.save(task);

        Schedule savedSchedule = scheduleRepository.findByDate(date)
                .orElseGet(() -> {
                    Schedule schedule = new Schedule();
                    schedule.setDate(date);
                    schedule.setNotes("Schedule for " + date);
                    schedule.setRosterStaff(savedStaff);
                    return scheduleRepository.save(schedule);
                });

        ScheduleAssignment assignment = new ScheduleAssignment();
        assignment.setSchedule(savedSchedule);
        assignment.setStaff(savedStaff);
        assignment.setTask(savedTask);
        assignment.setTimeSlot(slot);
        return assignment;
    }
}

