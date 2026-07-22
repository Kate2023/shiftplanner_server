package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.entities.ScheduleAssignment;
import com.example.shiftplanner_server.repositories.ScheduleAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleAssignmentService {
    private final ScheduleAssignmentRepository scheduleAssignmentRepository;


    public List<ScheduleAssignment> getById(Integer id) {
        return scheduleAssignmentRepository.findAllBySchedule_ScheduleId(id);
    }

    public List<ScheduleAssignment> saveToDate(LocalDate date, List<ScheduleAssignment> scheduleAssignments) {
        // delete existing assignments (if any) for the date before saving new ones
        deleteByDate(date);
        return scheduleAssignmentRepository.saveAll(scheduleAssignments);
    }

    public void deleteByDate(LocalDate date) {
        scheduleAssignmentRepository.deleteAllBySchedule_Date(date);
    }
}

