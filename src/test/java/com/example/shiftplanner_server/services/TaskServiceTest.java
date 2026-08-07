package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.entities.Task;
import com.example.shiftplanner_server.repositories.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void getAllReturnsTasksFromRepository() {
        Task first = new Task();
        first.setTaskId(1);
        first.setTaskName("Desk");

        Task second = new Task();
        second.setTaskId(2);
        second.setTaskName("Check-in");

        when(taskRepository.findAll()).thenReturn(List.of(first, second));

        List<Task> result = taskService.getAll();

        assertEquals(2, result.size());
        assertEquals("Desk", result.get(0).getTaskName());
        assertEquals("Check-in", result.get(1).getTaskName());
        verify(taskRepository).findAll();
    }

    @Test
    void getLLunchTasksReturnsOnlyLunchTasksAndCachesResult() {
        Task lunch = task(1, "Lunch break");
        Task lunchCheckin = task(2, "Lunch/Check-in");
        Task desk = task(3, "Desk");
        when(taskRepository.findAll()).thenReturn(List.of(lunch, lunchCheckin, desk));

        List<Task> firstCall = taskService.getLunchTasks();
        List<Task> secondCall = taskService.getLunchTasks();

        assertEquals(2, firstCall.size());
        assertSame(firstCall, secondCall);
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void getConsecutiveTasksReturnsMatchingConfiguredTaskNames() {
        Task desk = task(1, "Desk");
        Task checkin = task(2, "Check-in Evening");
        Task picking = task(3, "Picking");
        Task lunch = task(4, "Lunch break");
        when(taskRepository.findAll()).thenReturn(List.of(desk, checkin, picking, lunch));

        List<Task> result = taskService.getConsecutiveTasks();

        assertEquals(3, result.size());
        assertEquals(List.of(desk, checkin, picking), result);
    }

    @Test
    void getDeskTaskReturnsDeskTaskAndCachesIt() {
        Task desk = task(1, "Main Desk");
        Task other = task(2, "Roaming");
        when(taskRepository.findAll()).thenReturn(List.of(other, desk));

        Task first = taskService.getDeskTask();
        Task second = taskService.getDeskTask();

        assertSame(desk, first);
        assertSame(first, second);
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void getCheckinTaskThrowsWhenTaskMissing() {
        when(taskRepository.findAll()).thenReturn(List.of(task(1, "Desk")));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> taskService.getCheckinTask());

        assertEquals("Check-in task not found", exception.getMessage());
    }

    @Test
    void getBlockTaskReturnsMatchingTask() {
        Task block = task(7, "Block");
        when(taskRepository.findAll()).thenReturn(List.of(task(1, "Desk"), block));

        Task result = taskService.getBlockTask();

        assertSame(block, result);
    }

    @Test
    void getOptionalTaskReturnsMatchingTask() {
        Task optional = task(8, "Optional");
        when(taskRepository.findAll()).thenReturn(List.of(task(1, "Desk"), optional));

        Task result = taskService.getOptionalTask();

        assertSame(optional, result);
    }

    private static Task task(int id, String name) {
        Task task = new Task();
        task.setTaskId(id);
        task.setTaskName(name);
        return task;
    }
}

