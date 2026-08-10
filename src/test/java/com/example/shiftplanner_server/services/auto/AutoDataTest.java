package com.example.shiftplanner_server.services.auto;

import com.example.shiftplanner_server.entities.ScheduleAssignment;
import com.example.shiftplanner_server.entities.Staff;
import com.example.shiftplanner_server.entities.Task;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static com.example.shiftplanner_server.services.ServiceConstant.WORK_START;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoDataTest {

    @Test
    void constructorBuildsStagesInExpectedOrderForEnabledPolicies() {
        Task desk = task(10);
        Task checkIn = task(11);

        AutoData autoData = new AutoData(List.of(), List.of(3L, 5L, 7L), desk, checkIn);

        assertEquals(4, autoData.getStages().size());

        Stage stage0 = autoData.getStages().getFirst();
        assertEquals(3L, stage0.getPolicyId());
        assertSame(desk, stage0.getTask());
        assertEquals(1, stage0.getNumberOfTasks());
        assertTrue(stage0.isMandatory());
        assertEquals(WORK_START, stage0.getTime());

        Stage stage1 = autoData.getStages().get(1);
        assertEquals(5L, stage1.getPolicyId());
        assertSame(desk, stage1.getTask());
        assertEquals(2, stage1.getNumberOfTasks());
        assertFalse(stage1.isMandatory());

        Stage stage2 = autoData.getStages().get(2);
        assertEquals(7L, stage2.getPolicyId());
        assertSame(checkIn, stage2.getTask());
        assertEquals(1, stage2.getNumberOfTasks());
        assertFalse(stage2.isMandatory());

        Stage stage3 = autoData.getStages().get(3);
        assertEquals(5L, stage3.getPolicyId());
        assertSame(checkIn, stage3.getTask());
        assertEquals(2, stage3.getNumberOfTasks());
        assertFalse(stage3.isMandatory());
    }

    @Test
    void constructorCollectsDistinctStaffsFromAssignments() {
        Staff staffA = staff(1);
        Staff staffB = staff(2);
        Task desk = task(10);

        List<ScheduleAssignment> assignments = List.of(
            assignment(staffA, desk, 9),
            assignment(staffA, desk, 10),
            assignment(staffB, desk, 9)
        );

        AutoData autoData = new AutoData(assignments, List.of(3L), desk, task(11));

        assertEquals(2, autoData.getStaffs().size());
        assertTrue(autoData.getStaffs().contains(staffA));
        assertTrue(autoData.getStaffs().contains(staffB));
    }

    @Test
    void getCurrentStageReturnsFirstStageByDefault() {
        Task desk = task(10);

        AutoData autoData = new AutoData(List.of(), List.of(3L), desk, task(11));

        Stage current = autoData.getCurrentStage();
        assertEquals(3L, current.getPolicyId());
        assertSame(desk, current.getTask());
    }

    @Test
    void applyChangeUpdatesMatchingAssignmentTask() {
        Staff staff = staff(1);
        Task oldTask = task(10);
        Task newTask = task(20);
        LocalTime slot = LocalTime.of(9, 0);
        ScheduleAssignment target = assignment(staff, oldTask, 9);

        AutoData autoData = new AutoData(List.of(target), List.of(), task(30), task(31));

        autoData.applyChange(new Change(staff, slot, newTask, oldTask));

        assertSame(newTask, target.getTask());
    }

    @Test
    void applyChangeDoesNothingWhenNoMatchingAssignmentExists() {
        Staff staffA = staff(1);
        Staff staffB = staff(2);
        Task desk = task(10);
        Task changed = task(20);
        ScheduleAssignment existing = assignment(staffA, desk, 9);

        AutoData autoData = new AutoData(List.of(existing), List.of(), desk, task(11));

        autoData.applyChange(new Change(staffB, LocalTime.of(9, 0), changed, desk));

        assertSame(desk, existing.getTask());
    }

    @Test
    void getTaskReturnsAssignmentTaskOrNullWhenNotFound() {
        Staff staffA = staff(1);
        Staff staffB = staff(2);
        Task desk = task(10);
        ScheduleAssignment existing = assignment(staffA, desk, 9);

        AutoData autoData = new AutoData(List.of(existing), List.of(), desk, task(11));

        assertSame(desk, autoData.getTask(staffA, LocalTime.of(9, 0)));
        assertNull(autoData.getTask(staffB, LocalTime.of(9, 0)));
    }

    private static Staff staff(int id) {
        Staff staff = new Staff();
        staff.setStaffId(id);
        return staff;
    }

    private static Task task(int id) {
        Task task = new Task();
        task.setTaskId(id);
        return task;
    }

    private static ScheduleAssignment assignment(Staff staff, Task task, int hour) {
        ScheduleAssignment assignment = new ScheduleAssignment();
        assignment.setStaff(staff);
        assignment.setTask(task);
        assignment.setTimeSlot(LocalTime.of(hour, 0));
        return assignment;
    }
}

