package com.example.shiftplanner_server.controllers;

import com.example.shiftplanner_server.model.StaffParam;
import com.example.shiftplanner_server.model.StaffUpsertRequest;
import com.example.shiftplanner_server.services.StaffService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StaffControllerTest {

    @Mock
    private StaffService staffService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new StaffController(staffService)).build();
    }

    @Test
    void getAllReturnsOkAndStaffList() throws Exception {
        when(staffService.getAll()).thenReturn(sampleStaffList());

        mockMvc.perform(get("/api/staff"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].staffId").value(1))
            .andExpect(jsonPath("$[0].name").value("Alex"));

        verify(staffService).getAll();
    }

    @Test
    void createReturnsCreatedAndStaffList() throws Exception {
        when(staffService.create(any(StaffUpsertRequest.class))).thenReturn(sampleStaffList());

        StaffUpsertRequest request = new StaffUpsertRequest()
            .name("Alex")
            .title("Cashier")
            .workingHours(38.5)
            .lunchBreak(1.0);

        mockMvc.perform(post("/api/staff")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$[0].staffId").value(1))
            .andExpect(jsonPath("$[0].name").value("Alex"));

        verify(staffService).create(any(StaffUpsertRequest.class));
    }

    @Test
    void updateReturnsOkAndStaffList() throws Exception {
        when(staffService.update(eq(7), any(StaffUpsertRequest.class))).thenReturn(sampleStaffList());

        StaffUpsertRequest request = new StaffUpsertRequest()
            .name("Alex")
            .title("Cashier")
            .workingHours(36.0)
            .lunchBreak(0.5);

        mockMvc.perform(put("/api/staff/{staffId}", 7)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].staffId").value(1));

        verify(staffService).update(eq(7), any(StaffUpsertRequest.class));
    }

    @Test
    void deleteReturnsOkAndStaffList() throws Exception {
        when(staffService.delete(7)).thenReturn(sampleStaffList());

        mockMvc.perform(delete("/api/staff/{staffId}", 7))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].staffId").value(1));

        verify(staffService).delete(7);
    }

    private List<StaffParam> sampleStaffList() {
        return List.of(new StaffParam()
            .staffId(1L)
            .name("Alex")
            .title("Cashier")
            .workingHours(38.5)
            .lunchBreak(1.0));
    }
}

