package com.example.shiftplanner_server.repositories;

import com.example.shiftplanner_server.entities.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class TaskRepositoryTest extends PostgresRepositoryTestBase {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        createTablesFromSql(jdbcTemplate);
    }

    @AfterEach
    void tearDown() {
        dropSchema(jdbcTemplate);
    }

    @Test
    void saveAndFindById() {
        Task task = new Task();
        task.setTaskName("Desk");
        task.setColour("#4da3ff");
        task.setLunch(false);
        task.setAuto(false);

        Task saved = taskRepository.save(task);

        assertTrue(taskRepository.findById(saved.getTaskId()).isPresent());
    }

    @Test
    void findAllReturnsSavedTasks() {
        Task first = new Task();
        first.setTaskName("Desk");
        first.setColour("#4da3ff");
        first.setLunch(false);
        first.setAuto(false);

        Task second = new Task();
        second.setTaskName("Optional");
        second.setColour("#c7d2e2");
        second.setLunch(false);
        second.setAuto(true);

        taskRepository.save(first);
        taskRepository.save(second);

        List<Task> all = taskRepository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void deleteByIdRemovesTask() {
        Task task = new Task();
        task.setTaskName("Training");
        task.setColour("#5f8bff");
        task.setLunch(false);
        task.setAuto(false);

        Task saved = taskRepository.save(task);
        taskRepository.deleteById(saved.getTaskId());

        assertFalse(taskRepository.findById(saved.getTaskId()).isPresent());
    }
}

