package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.entities.Policy;
import com.example.shiftplanner_server.model.PolicyParam;
import com.example.shiftplanner_server.model.PolicyUpdateRequest;
import com.example.shiftplanner_server.repositories.PolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private PolicyService policyService;

    @Test
    void getAllDelegatesToRepository() {
        Policy p1 = new Policy();
        p1.setPolicyId(1);
        p1.setDescription("Min staff");
        p1.setParam1(2);

        when(policyRepository.findAll()).thenReturn(List.of(p1));

        List<Policy> result = policyService.getAll();

        assertEquals(1, result.size());
        assertEquals("Min staff", result.get(0).getDescription());
        verify(policyRepository).findAll();
    }

    @Test
    void saveDelegatesToRepository() {
        Policy policy = new Policy();
        policy.setDescription("Break minutes");
        policy.setParam1(45);

        when(policyRepository.save(policy)).thenReturn(policy);

        Policy saved = policyService.save(policy);

        assertEquals(policy, saved);
        verify(policyRepository).save(policy);
    }

    @Test
    void getAllParamsMapsPolicyFieldsAndDefaultsNullParam1ToZero() {
        Policy withParam = new Policy();
        withParam.setPolicyId(10);
        withParam.setDescription("Max hours");
        withParam.setParam1(40);

        Policy withoutParam = new Policy();
        withoutParam.setPolicyId(11);
        withoutParam.setDescription("Optional rule");
        withoutParam.setParam1(null);

        when(policyRepository.findAll()).thenReturn(List.of(withParam, withoutParam));

        List<PolicyParam> result = policyService.getAllParams();

        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).getPolicyId());
        assertEquals("Max hours", result.get(0).getDescription());
        assertEquals(40L, result.get(0).getParam1());

        assertEquals(11L, result.get(1).getPolicyId());
        assertEquals("Optional rule", result.get(1).getDescription());
        assertEquals(0L, result.get(1).getParam1());
    }

    @Test
    void updateUpdatesPolicyAndReturnsAllParams() {
        Policy target = new Policy();
        target.setPolicyId(7);
        target.setDescription("Break minutes");
        target.setParam1(30);

        Policy updated = new Policy();
        updated.setPolicyId(7);
        updated.setDescription("Break minutes");
        updated.setParam1(60);

        PolicyUpdateRequest request = new PolicyUpdateRequest().param1(60L);

        when(policyRepository.findById(7)).thenReturn(Optional.of(target));
        when(policyRepository.save(any(Policy.class))).thenReturn(updated);
        when(policyRepository.findAll()).thenReturn(List.of(updated));

        List<PolicyParam> result = policyService.update(7, request);

        assertEquals(60, target.getParam1());
        verify(policyRepository).save(target);
        assertEquals(1, result.size());
        assertEquals(7L, result.get(0).getPolicyId());
        assertEquals(60L, result.get(0).getParam1());
    }

    @Test
    void updateThrowsWhenPolicyDoesNotExist() {
        when(policyRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> policyService.update(99, new PolicyUpdateRequest().param1(5L)));

        assertEquals("Policy not found with id: 99", ex.getMessage());
    }
}

