package com.example.shiftplanner_server.controllers;

import com.example.shiftplanner_server.model.StaffParam;
import com.example.shiftplanner_server.model.StaffUpsertRequest;
import com.example.shiftplanner_server.services.StaffService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {
    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public ResponseEntity<List<StaffParam>> getAll() {
        return ResponseEntity.ok(staffService.getAll());
    }

    @PostMapping
    public ResponseEntity<List<StaffParam>> create(@RequestBody StaffUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(staffService.create(request));
    }

    @PutMapping("/{staffId}")
    public ResponseEntity<List<StaffParam>> update(@PathVariable Integer staffId, @RequestBody StaffUpsertRequest request) {
        return ResponseEntity.ok(staffService.update(staffId, request));
    }

    @DeleteMapping("/{staffId}")
    public ResponseEntity<List<StaffParam>> delete(@PathVariable Integer staffId) {
        return ResponseEntity.ok(staffService.delete(staffId));
    }
}

