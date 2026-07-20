package com.example.shiftplanner_server.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskTest {

    @Test
    void gettersAndSettersWork() {
        Task task = new Task();

        task.setTaskId(3);
        task.setTaskName("Cashier");
        task.setColour("#00AAFF");
        task.setLunch(true);
        task.setAuto(false);

        assertEquals(3, task.getTaskId());
        assertEquals("Cashier", task.getTaskName());
        assertEquals("#00AAFF", task.getColour());
        assertTrue(task.isLunch());
        assertFalse(task.isAuto());
    }
}

