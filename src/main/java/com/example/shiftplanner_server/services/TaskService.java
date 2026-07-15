package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.entities.Task;
import com.example.shiftplanner_server.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;


    public List<Task> getAll() {
        return taskRepository.findAll();
    }
}

