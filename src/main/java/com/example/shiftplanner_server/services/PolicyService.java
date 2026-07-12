package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.model.PolicyParam;
import com.example.shiftplanner_server.model.PolicyUpdateRequest;
import com.example.shiftplanner_server.entities.Policy;
import com.example.shiftplanner_server.repositories.PolicyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PolicyService {
    private final PolicyRepository policyRepository;

    public PolicyService(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    public List<Policy> getAll() {
        return policyRepository.findAll();
    }

    public Policy save(Policy policy) {
        return policyRepository.save(policy);
    }

    public List<PolicyParam> getAllParams() {
        // TODO: map entity list to PolicyParam list.
        return List.of();
    }

    public List<PolicyParam> update(Integer policyId, PolicyUpdateRequest request) {
        // TODO: update policy by id and return current list.
        return List.of();
    }
}

