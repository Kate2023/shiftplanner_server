package com.example.shiftplanner_server.repositories;

import com.example.shiftplanner_server.entities.ScheduleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleAssignmentRepository extends JpaRepository<ScheduleAssignment, Integer> {

    List<ScheduleAssignment> findAllBySchedule_Date(LocalDate date);

//    default List<ScheduleAssignment> saveScheduleAssignmentList(List<ScheduleAssignment> scheduleAssignments) {
//        return saveAll(scheduleAssignments);
//    }

    void deleteAllBySchedule_Date(LocalDate date);
}
