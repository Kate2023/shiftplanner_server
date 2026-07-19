package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.model.StaffParam;
import com.example.shiftplanner_server.model.StaffUpsertRequest;
import com.example.shiftplanner_server.entities.Staff;
import com.example.shiftplanner_server.repositories.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffService {
    private final StaffRepository staffRepository;

    public Staff save(Staff staff) {
        return staffRepository.save(staff);
    }

    public List<StaffParam> getAll() {
        return staffRepository.findAllByActiveTrue()
            .stream()
            .map(this::toStaffParam)
            .toList();
    }

    private StaffParam toStaffParam(Staff staff) {
        return new StaffParam()
            .staffId(staff.getStaffId() == null ? null : staff.getStaffId().longValue())
            .name(staff.getStaffName())
            // Title is required by the API model but not yet present on the entity.
            .title("")
            .workingHours(staff.getWorkingHours() == null ? null : staff.getWorkingHours().doubleValue())
            .lunchBreak(staff.getLunchBreak() == null ? null : staff.getLunchBreak().doubleValue());
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

