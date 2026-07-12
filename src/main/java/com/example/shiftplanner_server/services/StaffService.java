package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.model.StaffParam;
import com.example.shiftplanner_server.model.StaffUpsertRequest;
import com.example.shiftplanner_server.entities.Staff;
import com.example.shiftplanner_server.repositories.StaffRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaffService {
    private final StaffRepository staffRepository;

    public StaffService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    public List<Staff> getActive() {
        return staffRepository.findAllByActiveTrue();
    }

    public Staff save(Staff staff) {
        return staffRepository.save(staff);
    }

    public List<StaffParam> getAll() {
        // TODO: map entity list to StaffParam list.
        return List.of();
    }

    public List<StaffParam> create(StaffUpsertRequest request) {
        // TODO: create staff from request and return current list.
        return List.of();
    }

    public List<StaffParam> update(Integer staffId, StaffUpsertRequest request) {
        // TODO: update staff by id and return current list.
        return List.of();
    }

    public List<StaffParam> delete(Integer staffId) {
        // TODO: delete staff by id and return current list.
        return List.of();
    }
}

