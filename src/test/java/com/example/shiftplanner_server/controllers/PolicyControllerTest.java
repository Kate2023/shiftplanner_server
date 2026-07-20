package com.example.shiftplanner_server.controllers;

import com.example.shiftplanner_server.model.PolicyParam;
import com.example.shiftplanner_server.model.PolicyUpdateRequest;
import com.example.shiftplanner_server.services.PolicyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PolicyControllerTest {

    @Mock
    private PolicyService policyService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PolicyController(policyService)).build();
    }

    @Test
    void getAllReturnsOkAndPolicies() throws Exception {
        List<PolicyParam> policies = List.of(new PolicyParam()
            .policyId(1L)
            .description("Min team size")
            .param1(2L));

        when(policyService.getAllParams()).thenReturn(policies);

        mockMvc.perform(get("/api/policies"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].policyId").value(1))
            .andExpect(jsonPath("$[0].description").value("Min team size"))
            .andExpect(jsonPath("$[0].param_1").value(2));

        verify(policyService).getAllParams();
    }

    @Test
    void updateReturnsOkAndUpdatedPolicies() throws Exception {
        List<PolicyParam> updated = List.of(new PolicyParam()
            .policyId(3L)
            .description("Break minutes")
            .param1(45L));

        when(policyService.update(eq(3), any(PolicyUpdateRequest.class))).thenReturn(updated);

        PolicyUpdateRequest request = new PolicyUpdateRequest().param1(45L);

        mockMvc.perform(put("/api/policies/{policyId}", 3)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].policyId").value(3))
            .andExpect(jsonPath("$[0].description").value("Break minutes"))
            .andExpect(jsonPath("$[0].param_1").value(45));

        verify(policyService).update(eq(3), any(PolicyUpdateRequest.class));
    }
}

