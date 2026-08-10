package com.example.shiftplanner_server.services.auto;

import com.example.shiftplanner_server.entities.Staff;
import com.example.shiftplanner_server.entities.Task;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayDeque;

import static com.example.shiftplanner_server.services.ServiceConstant.WORK_START;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StageTest {

    @Test
    void resetClearsChangesAndRestoresDefaultPointers() {
        Task task = new Task();
        task.setTaskId(1);

        Task original = new Task();
        original.setTaskId(2);

        Staff staff = new Staff();
        staff.setStaffId(100);

        Change change = new Change(staff, LocalTime.of(11, 0), task, original);
        ArrayDeque<Change> changes = new ArrayDeque<>();
        changes.add(change);

        Stage stage = new Stage(5L, task, 2, false, changes, LocalTime.of(14, 0), staff);

        stage.reset();

        assertTrue(stage.getChanges().isEmpty());
        assertEquals(WORK_START, stage.getTime());
        assertNull(stage.getStaff());
    }
}

