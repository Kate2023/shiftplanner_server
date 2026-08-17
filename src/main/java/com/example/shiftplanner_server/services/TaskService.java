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
    private Task CHECKIN_TASK;
    private Task BLOCK_TASK;
    private Task OPTIONAL_TASK;
    private Task PICKING_TASK;
    private Task ROAMING_TASK;
    private Task SHELVING_TASK;
    private Task LUNCH_TASK;
    private Task OFFSITE_TASK;

    public List<Task> getAll() {
        return taskRepository.findAll();
    }

    public List<Task> getLunchTasks() {
        if (LUNCH_TASKS.isEmpty()) {
            getAll().stream()
                .filter(Task::isLunch)
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

    public Task getCheckinTask() {
        if (CHECKIN_TASK == null) {
            CHECKIN_TASK = getAll().stream()
                .filter(task -> task.getTaskName().toLowerCase().contains("check-in"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Check-in task not found"));
        }
        return CHECKIN_TASK;
    }

    public Task getBlockTask() {
        if (BLOCK_TASK == null) {
            BLOCK_TASK = getAll().stream()
                .filter(task -> task.getTaskName().toLowerCase().contains("block"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Block task not found"));
        }
        return BLOCK_TASK;
    }

    public Task getOptionalTask() {
        if (OPTIONAL_TASK == null) {
            OPTIONAL_TASK = getAll().stream()
                .filter(task -> task.getTaskName().toLowerCase().contains("optional"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Optional task not found"));
        }
        return OPTIONAL_TASK;
    }

    public Task getPickingTask() {
        if (PICKING_TASK == null) {
            PICKING_TASK = getAll().stream()
                .filter(task -> task.getTaskName().toLowerCase().contains("picking"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Picking task not found"));
        }
        return PICKING_TASK;
    }

    public Task getRoamingTask() {
        if (ROAMING_TASK == null) {
            ROAMING_TASK = getAll().stream()
                .filter(task -> task.getTaskName().toLowerCase().contains("roaming"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Roaming task not found"));
        }
        return ROAMING_TASK;
    }

    public Task getShelvingTask() {
        if (SHELVING_TASK == null) {
            SHELVING_TASK = getAll().stream()
                .filter(task -> task.getTaskName().toLowerCase().contains("shelving"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Shelving task not found"));
        }
        return SHELVING_TASK;
    }

    public Task getLunchTask() {
        if (LUNCH_TASK == null) {
            LUNCH_TASK = getAll().stream()
                .filter(task -> task.getTaskName().toLowerCase().contains("lunch"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Lunch task not found"));
        }
        return LUNCH_TASK;
    }

    public Task getOffsiteTask() {
        if (OFFSITE_TASK == null) {
            OFFSITE_TASK = getAll().stream()
                .filter(task -> task.getTaskName().toLowerCase().contains("off-site"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Off-site task not found"));
        }
        return OFFSITE_TASK;
    }
}
