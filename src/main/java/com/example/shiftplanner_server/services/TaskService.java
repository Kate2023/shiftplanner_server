package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.entities.Task;
import com.example.shiftplanner_server.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private static final List<String> CONSECUTIVE_TASKS = List.of(
        "Desk",
        "Check-in",
        "Picking",
        "Roaming",
        "Shelving");

    private final TaskRepository taskRepository;

    private final List<Task> LUNCH_TASKS = new ArrayList<>();

    private Task DESK_TASK;

    public List<Task> getAll() {
        return taskRepository.findAll();
    }

    public List<Task> getLLunchTasks() {
        if (LUNCH_TASKS.isEmpty()) {
            getAll().stream()
                .filter(task -> task.getTaskName().toLowerCase().contains("lunch"))
                .forEach(LUNCH_TASKS::add);
        }
        return LUNCH_TASKS;
    }

    public List<Task> getConsecutiveTasks() {
        return getAll().stream()
            .filter(task -> CONSECUTIVE_TASKS.stream()
                .anyMatch(consecutiveTask -> task.getTaskName()
                    .toLowerCase()
                    .contains(consecutiveTask.toLowerCase())))
            .toList();
    }

    public Task getDeskTask() {
        if (DESK_TASK == null) {
            DESK_TASK = getAll().stream()
                .filter(task -> task.getTaskName().toLowerCase().contains("desk"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Desk task not found"));
        }
        return DESK_TASK;
    }

}

