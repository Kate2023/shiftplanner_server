package com.example.shiftplanner_server.controllers;

import com.example.shiftplanner_server.model.ScheduleParam;
import com.example.shiftplanner_server.services.ScheduleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ScheduleControllerTest {

    @Mock
    private ScheduleService scheduleService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new ScheduleController(scheduleService))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
    }

    @Test
    void getByDateReturnsOkAndSchedule() throws Exception {
        LocalDate date = LocalDate.of(2026, 7, 20);
        when(scheduleService.getScheduleByDate(date)).thenReturn(sampleSchedule(date));

        mockMvc.perform(get("/api/schedules/{date}", "2026-07-20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.date").value("2026-07-20"))
            .andExpect(jsonPath("$.rosterStaffId").value(1));

        verify(scheduleService).getScheduleByDate(date);
    }

    @Test
    void autoScheduleAndSaveReturnsCreatedAndSchedule() throws Exception {
        LocalDate date = LocalDate.of(2026, 7, 20);
        ScheduleParam response = sampleSchedule(date);
        when(scheduleService.autoScheduleAndSave(eq(date), any(ScheduleParam.class))).thenReturn(response);

        ScheduleParam request = sampleSchedule(date);

        mockMvc.perform(post("/api/schedules/{date}", "2026-07-20")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.date").value("2026-07-20"));

        verify(scheduleService).autoScheduleAndSave(eq(date), any(ScheduleParam.class));
    }

    @Test
    void saveByDateReturnsCreatedAndSchedule() throws Exception {
        LocalDate date = LocalDate.of(2026, 7, 20);
        ScheduleParam response = sampleSchedule(date);
        when(scheduleService.saveByDate(eq(date), any(ScheduleParam.class))).thenReturn(response);

        ScheduleParam request = sampleSchedule(date);

        mockMvc.perform(put("/api/schedules/{date}", "2026-07-20")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.date").value("2026-07-20"));

        verify(scheduleService).saveByDate(eq(date), any(ScheduleParam.class));
    }

    @Test
    void saveByDateReturnsErrorReasonWhenBadRequest() throws Exception {
        LocalDate date = LocalDate.of(2026, 7, 20);
        when(scheduleService.saveByDate(eq(date), any(ScheduleParam.class)))
            .thenThrow(new ResponseStatusException(BAD_REQUEST, "Path date must match request date"));

        ScheduleParam request = sampleSchedule(LocalDate.of(2026, 7, 21));

        mockMvc.perform(put("/api/schedules/{date}", "2026-07-20")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.reason").value("Path date must match request date"));

        verify(scheduleService).saveByDate(eq(date), any(ScheduleParam.class));
    }

    private ScheduleParam sampleSchedule(LocalDate date) {
        return new ScheduleParam()
            .date(date.toString())
            .rosterStaffId(1L)
            .bankingStaffId(2L)
            .backupStaffId(3L)
            .inspectionStaffId(4L)
            .notes("Normal day")
            .policies(List.of(10L, 20L))
            .assignments(List.of());
    }
}

