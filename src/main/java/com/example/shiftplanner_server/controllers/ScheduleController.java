package com.example.shiftplanner_server.controllers;

import com.example.shiftplanner_server.model.ScheduleParam;
import com.example.shiftplanner_server.services.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;


    @GetMapping("/{date}")
    public ResponseEntity<ScheduleParam> getByDate(@PathVariable LocalDate date) {
        return ResponseEntity.ok(scheduleService.getScheduleByDate(date));
    }

    @PostMapping("/{date}")
    public ResponseEntity<ScheduleParam> autoSchedule(
            @PathVariable LocalDate date,
            @RequestParam(value = "ruleCount", required = false) Integer ruleCount,
            @RequestBody ScheduleParam request
    ) {
        if (ruleCount != null && request.getPolicies() != null && ruleCount != request.getPolicies().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ruleCount does not match selected policies");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.autoSchedule(date, request));
    }

    @PutMapping("/{date}")
    public ResponseEntity<ScheduleParam> saveByDate(
            @PathVariable LocalDate date,
            @RequestBody ScheduleParam request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.saveByDate(date, request));
    }
}

