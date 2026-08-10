package com.example.shiftplanner_server.services.auto;

import com.example.shiftplanner_server.entities.ScheduleAssignment;
import com.example.shiftplanner_server.entities.Staff;
import com.example.shiftplanner_server.entities.Task;
import com.example.shiftplanner_server.services.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;

import static com.example.shiftplanner_server.services.ServiceConstant.WORK_END;
import static com.example.shiftplanner_server.services.ServiceConstant.WORK_START;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutoServiceTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private AutoService autoService;

    @Test
    void tryStage4ReplacesOptionalWhenSuitableTaskExists() {
        Staff staff = staff();

        Task optional = task(1);
        Task desk = task(2);
        Task block = task(7);
        Task picking = task(3);
        Task roaming = task(4);
        Task shelving = task(5);

        List<ScheduleAssignment> assignments = new java.util.ArrayList<>();
        // AutoService iterates all working-hour slots, so populate a full day for this staff.
        for (LocalTime time = WORK_START; time.isBefore(WORK_END); time = time.plusHours(1)) {
            assignments.add(assignment(staff, block, time));
        }

        ScheduleAssignment optionalSlot = assignments.getFirst(); // 09:00
        optionalSlot.setTask(optional);
        assignments.get(1).setTask(desk); // 10:00

        AutoData data = new AutoData(assignments, List.of(), desk, task(6));

        when(taskService.getOptionalTask()).thenReturn(optional);
        when(taskService.getPickingTask()).thenReturn(picking);
        when(taskService.getRoamingTask()).thenReturn(roaming);
        when(taskService.getShelvingTask()).thenReturn(shelving);

        autoService.tryStage_4(data);

        assertSame(picking, optionalSlot.getTask());
    }

    private static Staff staff() {
        Staff staff = new Staff();
        staff.setStaffId(1);
        return staff;
    }

    private static Task task(int id) {
        Task task = new Task();
        task.setTaskId(id);
        return task;
    }

    private static ScheduleAssignment assignment(Staff staff, Task task, LocalTime time) {
        ScheduleAssignment assignment = new ScheduleAssignment();
        assignment.setStaff(staff);
        assignment.setTask(task);
        assignment.setTimeSlot(time);
        return assignment;
    }
}

