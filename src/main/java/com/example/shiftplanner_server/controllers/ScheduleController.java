package com.example.shiftplanner_server.controllers;

import com.example.shiftplanner_server.model.ScheduleParam;
import com.example.shiftplanner_server.services.ScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {
    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/{date}")
    public ResponseEntity<ScheduleParam> getByDate(@PathVariable LocalDate date) {
        return ResponseEntity.ok(scheduleService.getScheduleByDate(date));
    }

    @PostMapping("/{date}")
    public ResponseEntity<ScheduleParam> autoScheduleAndSave(
            @PathVariable LocalDate date,
            @RequestBody ScheduleParam request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.autoScheduleAndSave(date, request));
    }

    @PutMapping("/{date}")
    public ResponseEntity<ScheduleParam> saveByDate(
            @PathVariable LocalDate date,
            @RequestBody ScheduleParam request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.saveByDate(date, request));
    }
}

