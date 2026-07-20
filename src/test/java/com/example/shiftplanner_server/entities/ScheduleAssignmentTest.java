package com.example.shiftplanner_server.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScheduleAssignmentTest {

    @Test
    void gettersAndSettersWork() {
        Schedule schedule = new Schedule();
        schedule.setScheduleId(11);

        Staff staff = new Staff();
        staff.setStaffId(12);

        Task task = new Task();
        task.setTaskId(13);

        ScheduleAssignment assignment = new ScheduleAssignment();
        assignment.setAssignmentId(99);
        assignment.setSchedule(schedule);
        assignment.setStaff(staff);
        assignment.setTimeSlot(LocalTime.of(9, 30));
        assignment.setTask(task);

        assertEquals(99, assignment.getAssignmentId());
        assertEquals(schedule, assignment.getSchedule());
        assertEquals(staff, assignment.getStaff());
        assertEquals(LocalTime.of(9, 30), assignment.getTimeSlot());
        assertEquals(task, assignment.getTask());
    }
}

