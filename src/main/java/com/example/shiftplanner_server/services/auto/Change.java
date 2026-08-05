package com.example.shiftplanner_server.services.auto;

import com.example.shiftplanner_server.entities.Staff;
import com.example.shiftplanner_server.entities.Task;

import java.time.LocalTime;

public record Change(Staff staff, LocalTime timeslot, Task task) {
}
