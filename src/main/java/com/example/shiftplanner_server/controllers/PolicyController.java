package com.example.shiftplanner_server.controllers;

import com.example.shiftplanner_server.model.PolicyParam;
import com.example.shiftplanner_server.model.PolicyUpdateRequest;
import com.example.shiftplanner_server.services.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {
    private final PolicyService policyService;


    @GetMapping
    public ResponseEntity<List<PolicyParam>> getAll() {
        return ResponseEntity.ok(policyService.getAllParams());
    }

    @PutMapping("/{policyId}")
    public ResponseEntity<List<PolicyParam>> update(@PathVariable Integer policyId, @RequestBody PolicyUpdateRequest request) {
        return ResponseEntity.ok(policyService.update(policyId, request));
    }
}

