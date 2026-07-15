package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.model.ScheduleParam;
import com.example.shiftplanner_server.entities.Schedule;
import com.example.shiftplanner_server.entities.ScheduleAssignment;
import com.example.shiftplanner_server.repositories.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final ScheduleAssignmentService scheduleAssignmentService;


    // TODO: replace Schedule with ScheduleParam, which will be generated from openapi.yaml
    public Schedule save(LocalDate date, Schedule schedule, List<ScheduleAssignment> assignment) {
        // Step 1: check if request is valid
        // 1.1 date cannot be earlier than today
        // 1.2 date must match schedule, assignment
        // 1.3 schedule.policies must all exist

        // Step 2: Policy check

        // Step 3: save schedule and assignments

        // Step 4: return saved schedule
        return scheduleRepository.save(schedule);
    }

    public Optional<Schedule> getByDate(LocalDate date) {
        return scheduleRepository.findByDate(date);
    }

    public void generate(LocalDate date, Schedule schedule) {
    }

    public ScheduleParam getScheduleByDate(LocalDate date) {
        // TODO: fetch and map schedule + assignments for the given date.
        return null;
    }

    public ScheduleParam autoScheduleAndSave(LocalDate date, ScheduleParam request) {
        // TODO: run auto-scheduling flow and persist result.
        return null;
    }

    public ScheduleParam saveByDate(LocalDate date, ScheduleParam request) {
        // TODO: validate policies and save explicit schedule.
        return null;
    }
}

