package com.example.shiftplanner_server.services.auto;

import com.example.shiftplanner_server.entities.Staff;
import com.example.shiftplanner_server.entities.Task;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChangeTest {

    @Test
    void recordExposesAssignedStaffTimeAndTask() {
        Staff staff = new Staff();
        staff.setStaffId(2);

        Task task = new Task();
        task.setTaskId(10);

        LocalTime slot = LocalTime.of(13, 0);

        Change change = new Change(staff, slot, task);

        assertEquals(staff, change.staff());
        assertEquals(slot, change.timeSlot());
        assertEquals(task, change.task());
    }
}

