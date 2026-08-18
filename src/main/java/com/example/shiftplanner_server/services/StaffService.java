package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.entities.Staff;
import com.example.shiftplanner_server.model.StaffParam;
import com.example.shiftplanner_server.model.StaffUpsertRequest;
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
            .name(staff.getStaffName());
    }

    public List<StaffParam> create(StaffUpsertRequest request) {
        Staff staff = new Staff();
        updateStaffFromRequest(request, staff);
        staffRepository.save(staff);
        return getAll();
    }

    public List<StaffParam> update(Integer staffId, StaffUpsertRequest request) {
        Staff staff = staffRepository.getReferenceById(staffId);
        updateStaffFromRequest(request, staff);
        staffRepository.save(staff);
        return getAll();
    }

    /**
     * Soft delete a staff member by setting their active status to false. So that the existing schedules can
     * still reference the staff member without causing data integrity issues.
     *
     * @param staffId the id of the staff to be deleted
     * @return all active staffs
     */
    public List<StaffParam> delete(Integer staffId) {
        Staff staff = staffRepository.getReferenceById(staffId);
        staff.setActive(false);
        staffRepository.save(staff);
        return getAll();
    }

    private static void updateStaffFromRequest(StaffUpsertRequest request, Staff staff) {
        staff.setStaffName(request.getName());
        staff.setActive(true);
    }
}

