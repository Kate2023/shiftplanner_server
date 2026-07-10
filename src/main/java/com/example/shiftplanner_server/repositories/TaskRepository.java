package com.example.shiftplanner_server.repositories;

import com.example.shiftplanner_server.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
//    default List<Task> getAllTasks() {
//        return findAll();
//    }
}

