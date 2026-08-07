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
        Task picking = task(3);
        Task roaming = task(4);
        Task shelving = task(5);

        ScheduleAssignment optionalSlot = assignment(staff, optional, 9);
        ScheduleAssignment nextSlot = assignment(staff, desk, 10);

        AutoData data = new AutoData(List.of(optionalSlot, nextSlot), List.of(), desk, task(6));

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

    private static ScheduleAssignment assignment(Staff staff, Task task, int hour) {
        ScheduleAssignment assignment = new ScheduleAssignment();
        assignment.setStaff(staff);
        assignment.setTask(task);
        assignment.setTimeSlot(LocalTime.of(hour, 0));
        return assignment;
    }
}

