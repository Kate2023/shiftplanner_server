package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.entities.Staff;
import com.example.shiftplanner_server.model.StaffParam;
import com.example.shiftplanner_server.model.StaffUpsertRequest;
import com.example.shiftplanner_server.repositories.StaffRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffServiceTest {

    @Mock
    private StaffRepository staffRepository;

    @InjectMocks
    private StaffService staffService;

    @Test
    void saveDelegatesToRepository() {
        Staff staff = new Staff();
        staff.setStaffName("Alex");

        when(staffRepository.save(staff)).thenReturn(staff);

        Staff saved = staffService.save(staff);

        assertEquals(staff, saved);
        verify(staffRepository).save(staff);
    }

    @Test
    void getAllMapsActiveStaffToApiModel() {
        Staff staff = new Staff();
        staff.setStaffId(5);
        staff.setStaffName("Riley");
        staff.setActive(true);

        when(staffRepository.findAllByActiveTrue()).thenReturn(List.of(staff));

        List<StaffParam> result = staffService.getAll();

        assertEquals(1, result.size());
        assertEquals(5L, result.getFirst().getStaffId());
        assertEquals("Riley", result.getFirst().getName());
    }

    @Test
    void createSavesMappedStaffAndReturnsCurrentActiveStaff() {
        StaffUpsertRequest request = new StaffUpsertRequest()
            .name("Alex");

        Staff active = new Staff();
        active.setStaffId(1);
        active.setStaffName("Alex");
        active.setActive(true);

        when(staffRepository.findAllByActiveTrue()).thenReturn(List.of(active));

        List<StaffParam> result = staffService.create(request);

        ArgumentCaptor<Staff> staffCaptor = ArgumentCaptor.forClass(Staff.class);
        verify(staffRepository).save(staffCaptor.capture());
        Staff saved = staffCaptor.getValue();

        assertEquals("Alex", saved.getStaffName());
        assertTrue(saved.isActive());

        assertEquals(1, result.size());
        assertEquals("Alex", result.getFirst().getName());
    }

    @Test
    void updateUsesReferenceUpdatesFieldsAndReturnsActiveStaff() {
        Staff existing = new Staff();
        existing.setStaffId(10);
        existing.setStaffName("Before");
        existing.setActive(false);

        StaffUpsertRequest request = new StaffUpsertRequest()
            .name("After");

        when(staffRepository.getReferenceById(10)).thenReturn(existing);
        when(staffRepository.findAllByActiveTrue()).thenReturn(List.of(existing));
        when(staffRepository.save(any(Staff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<StaffParam> result = staffService.update(10, request);

        assertEquals("After", existing.getStaffName());
        assertTrue(existing.isActive());
        verify(staffRepository).save(existing);
        assertEquals(1, result.size());
        assertEquals("After", result.getFirst().getName());
    }

    @Test
    void deletePerformsSoftDeleteAndReturnsRemainingActiveStaff() {
        Staff existing = new Staff();
        existing.setStaffId(22);
        existing.setActive(true);

        when(staffRepository.getReferenceById(22)).thenReturn(existing);
        when(staffRepository.findAllByActiveTrue()).thenReturn(List.of());

        List<StaffParam> result = staffService.delete(22);

        assertFalse(existing.isActive());
        verify(staffRepository).save(existing);
        assertTrue(result.isEmpty());
    }
}

