package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.entities.Policy;
import com.example.shiftplanner_server.model.PolicyParam;
import com.example.shiftplanner_server.model.PolicyUpdateRequest;
import com.example.shiftplanner_server.repositories.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyService {
    private final PolicyRepository policyRepository;


    public List<Policy> getAll() {
        return policyRepository.findAll();
    }

    public Policy save(Policy policy) {
        return policyRepository.save(policy);
    }

    public List<PolicyParam> getAllParams() {
        return getAll().stream()
            .map(policy -> new PolicyParam()
                .policyId(Long.valueOf(policy.getPolicyId()))
                .description(policy.getDescription())
                .param1(policy.getParam1() == null ? 0L : Long.valueOf(policy.getParam1())))
            .toList();
    }

    public List<PolicyParam> update(Integer policyId, PolicyUpdateRequest request) {
        Policy policy = policyRepository.findById(policyId)
            .orElseThrow(() -> new RuntimeException("Policy not found with id: " + policyId));
        policy.setParam1(Math.toIntExact(request.getParam1()));
        policyRepository.save(policy);
        return getAllParams();
    }
}

