package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.entities.ScheduleAssignment;
import com.example.shiftplanner_server.repositories.ScheduleAssignmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.mockito.InOrder;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleAssignmentServiceTest {

    @Mock
    private ScheduleAssignmentRepository scheduleAssignmentRepository;

    @InjectMocks
    private ScheduleAssignmentService scheduleAssignmentService;

    @Test
    void getByIdReturnsAssignmentsForScheduleId() {
        ScheduleAssignment first = new ScheduleAssignment();
        first.setAssignmentId(1);
        ScheduleAssignment second = new ScheduleAssignment();
        second.setAssignmentId(2);

        when(scheduleAssignmentRepository.findAllBySchedule_ScheduleId(99)).thenReturn(List.of(first, second));

        List<ScheduleAssignment> result = scheduleAssignmentService.getById(99);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getAssignmentId());
        assertEquals(2, result.get(1).getAssignmentId());
        verify(scheduleAssignmentRepository).findAllBySchedule_ScheduleId(99);
    }

    @Test
    void saveToDateDeletesExistingAssignmentsBeforeSaveAll() {
        LocalDate date = LocalDate.of(2026, 7, 22);
        ScheduleAssignment assignment = new ScheduleAssignment();
        List<ScheduleAssignment> input = List.of(assignment);

        when(scheduleAssignmentRepository.saveAll(input)).thenReturn(input);

        List<ScheduleAssignment> result = scheduleAssignmentService.saveToDate(date, input);

        assertEquals(1, result.size());
        assertEquals(assignment, result.get(0));
        InOrder inOrder = inOrder(scheduleAssignmentRepository);
        inOrder.verify(scheduleAssignmentRepository).deleteAllBySchedule_Date(date);
        inOrder.verify(scheduleAssignmentRepository).saveAll(input);
    }

    @Test
    void deleteByDateDelegatesToRepository() {
        LocalDate date = LocalDate.of(2026, 7, 23);

        scheduleAssignmentService.deleteByDate(date);

        verify(scheduleAssignmentRepository).deleteAllBySchedule_Date(date);
    }
}

