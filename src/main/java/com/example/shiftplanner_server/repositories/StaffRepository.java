package com.example.shiftplanner_server.repositories;

import com.example.shiftplanner_server.entities.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {
    List<Staff> findAllByActiveTrue();
}

