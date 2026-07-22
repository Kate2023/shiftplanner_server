package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.entities.Policy;
import com.example.shiftplanner_server.entities.Schedule;
import com.example.shiftplanner_server.entities.ScheduleAssignment;
import com.example.shiftplanner_server.entities.Staff;
import com.example.shiftplanner_server.entities.Task;
import com.example.shiftplanner_server.model.Assignment;
import com.example.shiftplanner_server.model.ScheduleParam;
import com.example.shiftplanner_server.repositories.PolicyRepository;
import com.example.shiftplanner_server.repositories.ScheduleRepository;
import com.example.shiftplanner_server.repositories.StaffRepository;
import com.example.shiftplanner_server.repositories.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private ScheduleAssignmentService scheduleAssignmentService;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    @Test
    void getByDateDelegatesToRepository() {
        LocalDate date = LocalDate.of(2026, 7, 22);
        Schedule schedule = new Schedule();
        schedule.setScheduleId(7);

        when(scheduleRepository.findByDate(date)).thenReturn(Optional.of(schedule));

        Optional<Schedule> result = scheduleService.getByDate(date);

        assertTrue(result.isPresent());
        assertEquals(7, result.get().getScheduleId());
        verify(scheduleRepository).findByDate(date);
    }

    @Test
    void getScheduleByDateThrowsNotFoundWhenMissing() {
        LocalDate date = LocalDate.of(2026, 7, 22);
        when(scheduleRepository.findByDate(date)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> scheduleService.getScheduleByDate(date));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Schedule not found for date: 2026-07-22", ex.getReason());
    }

    @Test
    void getScheduleByDateMapsScheduleAndAssignments() {
        LocalDate date = LocalDate.of(2026, 7, 22);

        Staff roster = staff(1);
        Staff banking = staff(2);
        Staff backup = staff(3);
        Staff inspector = staff(4);

        Schedule schedule = new Schedule();
        schedule.setScheduleId(99);
        schedule.setDate(date);
        schedule.setNotes("Normal day");
        schedule.setRosterStaff(roster);
        schedule.setBankingStaff(banking);
        schedule.setBankingBackupStaff(backup);
        schedule.setBuildingInspector(inspector);
        schedule.setPolicies("10,20");

        Task task = task(7);
        ScheduleAssignment assignment = new ScheduleAssignment();
        assignment.setSchedule(schedule);
        assignment.setStaff(roster);
        assignment.setTask(task);
        assignment.setTimeSlot(LocalTime.of(9, 0));

        when(scheduleRepository.findByDate(date)).thenReturn(Optional.of(schedule));
        when(scheduleAssignmentService.getById(99)).thenReturn(List.of(assignment));

        ScheduleParam result = scheduleService.getScheduleByDate(date);

        assertEquals("2026-07-22", result.getDate());
        assertEquals(1L, result.getRosterStaffId());
        assertEquals(2L, result.getBankingStaffId());
        assertEquals(3L, result.getBackupStaffId());
        assertEquals(4L, result.getInspectionStaffId());
        assertEquals("Normal day", result.getNotes());
        assertEquals(List.of(10L, 20L), result.getPolicies());
        assertEquals(1, result.getAssignments().size());
        assertEquals(1L, result.getAssignments().get(0).getStaffId());
        assertEquals("09:00", result.getAssignments().get(0).getTimeSlot());
        assertEquals(7L, result.getAssignments().get(0).getTaskId());
    }

    @Test
    void saveByDateRejectsAssignmentOutsideAllowedTimeRange() {
        LocalDate date = LocalDate.now().plusDays(1);
        ScheduleParam request = baseRequest(date)
                .assignments(List.of(new Assignment().staffId(1L).taskId(10L).timeSlot("08:59")));

        stubValidationLookups();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> scheduleService.saveByDate(date, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("between 09:00 and 18:00"));
        verify(scheduleRepository, never()).save(any(Schedule.class));
    }

    @Test
    void saveByDateSavesAndReturnsMappedResult() {
        LocalDate date = LocalDate.now().plusDays(1);
        ScheduleParam request = baseRequest(date)
                .policies(List.of(10L))
                .assignments(List.of(new Assignment().staffId(1L).taskId(10L).timeSlot("09:00")));

        Staff s1 = staff(1);
        Staff s2 = staff(2);
        Staff s3 = staff(3);
        Staff s4 = staff(4);
        Task t10 = task(10);

        Map<Integer, Staff> staffById = new HashMap<>();
        staffById.put(1, s1);
        staffById.put(2, s2);
        staffById.put(3, s3);
        staffById.put(4, s4);

        when(staffRepository.findAllById(any())).thenAnswer(invocation -> {
            Iterable<Integer> ids = invocation.getArgument(0);
            List<Staff> result = new ArrayList<>();
            for (Integer id : ids) {
                Staff found = staffById.get(id);
                if (found != null) {
                    result.add(found);
                }
            }
            return result;
        });
        when(staffRepository.findById(anyInt())).thenAnswer(invocation -> Optional.ofNullable(staffById.get(invocation.getArgument(0))));

        when(taskRepository.findAllById(any())).thenReturn(List.of(t10));
        when(taskRepository.findById(10)).thenReturn(Optional.of(t10));

        Policy p10 = new Policy();
        p10.setPolicyId(10);
        when(policyRepository.findAllById(any())).thenReturn(List.of(p10));

        Schedule existing = new Schedule();
        existing.setScheduleId(11);
        existing.setDate(date);

        Schedule saved = new Schedule();
        saved.setScheduleId(12);
        saved.setDate(date);
        saved.setNotes("Auto");
        saved.setRosterStaff(s1);
        saved.setBankingStaff(s2);
        saved.setBankingBackupStaff(s3);
        saved.setBuildingInspector(s4);
        saved.setPolicies("10");

        ScheduleAssignment savedAssignment = new ScheduleAssignment();
        savedAssignment.setSchedule(saved);
        savedAssignment.setStaff(s1);
        savedAssignment.setTask(t10);
        savedAssignment.setTimeSlot(LocalTime.of(9, 0));

        when(scheduleRepository.findByDate(date)).thenReturn(Optional.of(existing), Optional.of(saved));
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(saved);
        when(scheduleAssignmentService.getById(12)).thenReturn(List.of(savedAssignment));

        ScheduleParam result = scheduleService.saveByDate(date, request);

        assertEquals(date.toString(), result.getDate());
        assertEquals(1, result.getAssignments().size());
        assertEquals("09:00", result.getAssignments().get(0).getTimeSlot());
        assertEquals(List.of(10L), result.getPolicies());

        verify(scheduleAssignmentService).deleteByDate(date);
        verify(scheduleRepository).delete(existing);
        verify(scheduleRepository).save(any(Schedule.class));
        verify(scheduleAssignmentService).saveToDate(any(LocalDate.class), any());
    }

    private static Staff staff(int id) {
        Staff staff = new Staff();
        staff.setStaffId(id);
        return staff;
    }

    private static Task task(int id) {
        Task task = new Task();
        task.setTaskId(id);
        return task;
    }

    private static ScheduleParam baseRequest(LocalDate date) {
        return new ScheduleParam()
                .date(date.toString())
                .rosterStaffId(1L)
                .bankingStaffId(2L)
                .backupStaffId(3L)
                .inspectionStaffId(4L)
                .notes("Auto")
                .policies(List.of());
    }

    private void stubValidationLookups() {
        Staff s1 = staff(1);
        Staff s2 = staff(2);
        Staff s3 = staff(3);
        Staff s4 = staff(4);
        Task t10 = task(10);

        when(policyRepository.findAllById(any())).thenReturn(List.of());
        when(staffRepository.findAllById(any())).thenAnswer(invocation -> {
            Iterable<Integer> ids = invocation.getArgument(0);
            List<Staff> result = new ArrayList<>();
            for (Integer id : ids) {
                if (id == 1) {
                    result.add(s1);
                } else if (id == 2) {
                    result.add(s2);
                } else if (id == 3) {
                    result.add(s3);
                } else if (id == 4) {
                    result.add(s4);
                }
            }
            return result;
        });
        when(taskRepository.findAllById(any())).thenReturn(List.of(t10));
    }
}

