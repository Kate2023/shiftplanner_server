package com.example.shiftplanner_server.repositories;

import com.example.shiftplanner_server.entities.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
//    default Schedule saveSchedule(Schedule schedule) {
//        return save(schedule);
//    }

    Optional<Schedule> findByDate(LocalDate date);
}

