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
import static org.mockito.Mockito.verify;
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
}

