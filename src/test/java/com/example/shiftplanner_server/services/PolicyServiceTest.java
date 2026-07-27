package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.entities.Policy;
import com.example.shiftplanner_server.entities.ScheduleAssignment;
import com.example.shiftplanner_server.entities.Staff;
import com.example.shiftplanner_server.entities.Task;
import com.example.shiftplanner_server.model.PolicyParam;
import com.example.shiftplanner_server.model.PolicyUpdateRequest;
import com.example.shiftplanner_server.repositories.PolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private ScheduleAssignmentService scheduleAssignmentService;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private PolicyService policyService;

    @Test
    void getAllDelegatesToRepository() {
        Policy p1 = new Policy();
        p1.setPolicyId(1);
        p1.setDescription("Min staff");
        p1.setParam1(2);

        when(policyRepository.findAll()).thenReturn(List.of(p1));

        List<Policy> result = policyService.getAll();

        assertEquals(1, result.size());
        assertEquals("Min staff", result.get(0).getDescription());
        verify(policyRepository).findAll();
    }

    @Test
    void saveDelegatesToRepository() {
        Policy policy = new Policy();
        policy.setDescription("Break minutes");
        policy.setParam1(45);

        when(policyRepository.save(policy)).thenReturn(policy);

        Policy saved = policyService.save(policy);

        assertEquals(policy, saved);
        verify(policyRepository).save(policy);
    }

    @Test
    void getAllParamsMapsPolicyFieldsAndDefaultsNullParam1ToZero() {
        Policy withParam = new Policy();
        withParam.setPolicyId(10);
        withParam.setDescription("Max hours");
        withParam.setParam1(40);

        Policy withoutParam = new Policy();
        withoutParam.setPolicyId(11);
        withoutParam.setDescription("Optional rule");
        withoutParam.setParam1(null);

        when(policyRepository.findAll()).thenReturn(List.of(withParam, withoutParam));

        List<PolicyParam> result = policyService.getAllParams();

        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).getPolicyId());
        assertEquals("Max hours", result.get(0).getDescription());
        assertEquals(40L, result.get(0).getParam1());

        assertEquals(11L, result.get(1).getPolicyId());
        assertEquals("Optional rule", result.get(1).getDescription());
        assertEquals(0L, result.get(1).getParam1());
    }

    @Test
    void updateUpdatesPolicyAndReturnsAllParams() {
        Policy target = new Policy();
        target.setPolicyId(7);
        target.setDescription("Break minutes");
        target.setParam1(30);

        Policy updated = new Policy();
        updated.setPolicyId(7);
        updated.setDescription("Break minutes");
        updated.setParam1(60);

        PolicyUpdateRequest request = new PolicyUpdateRequest().param1(60L);

        when(policyRepository.findById(7)).thenReturn(Optional.of(target));
        when(policyRepository.save(any(Policy.class))).thenReturn(updated);
        when(policyRepository.findAll()).thenReturn(List.of(updated));

        List<PolicyParam> result = policyService.update(7, request);

        assertEquals(60, target.getParam1());
        verify(policyRepository).save(target);
        assertEquals(1, result.size());
        assertEquals(7L, result.get(0).getPolicyId());
        assertEquals(60L, result.get(0).getParam1());
    }

    @Test
    void updateThrowsWhenPolicyDoesNotExist() {
        when(policyRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> policyService.update(99, new PolicyUpdateRequest().param1(5L)));

        assertEquals("Policy not found with id: 99", ex.getMessage());
    }

    @Test
    void meetPolicy1ReturnsTrueWhenDistinctStaffCountMeetsThreshold() {
        Policy minStaffPolicy = new Policy();
        minStaffPolicy.setPolicyId(1);
        minStaffPolicy.setParam1(2);

        when(scheduleAssignmentService.getDistinctStaffs(any())).thenReturn(List.of(10, 11));
        when(policyRepository.findById(1)).thenReturn(Optional.of(minStaffPolicy));

        boolean result = policyService.meetPolicy_1(List.of(new ScheduleAssignment()));

        assertTrue(result);
    }

    @Test
    void meetPolicy1ReturnsFalseWhenDistinctStaffCountIsBelowThreshold() {
        Policy minStaffPolicy = new Policy();
        minStaffPolicy.setPolicyId(1);
        minStaffPolicy.setParam1(3);

        when(scheduleAssignmentService.getDistinctStaffs(any())).thenReturn(List.of(10, 11));
        when(policyRepository.findById(1)).thenReturn(Optional.of(minStaffPolicy));

        boolean result = policyService.meetPolicy_1(List.of(new ScheduleAssignment()));

        assertFalse(result);
    }

    @Test
    void meetPolicy2ReturnsTrueWhenEachStaffHasAtLeastOneLunchAssignmentInWindow() {
        Task lunch = task(1, null);
        Task nonLunch = task(2, null);
        Staff staffA = staff(100);
        Staff staffB = staff(101);

        when(taskService.getLLunchTasks()).thenReturn(List.of(lunch));

        List<ScheduleAssignment> assignments = List.of(
            assignment(staffA, nonLunch, 12, 30),
            assignment(staffA, lunch, 13, 0),
            assignment(staffB, lunch, 13, 30)
        );

        boolean result = policyService.meetPolicy_2(assignments);

        assertTrue(result);
    }

    @Test
    void meetPolicy2ReturnsFalseWhenAStaffMemberHasNoLunchAssignmentInWindow() {
        Task lunch = task(1, null);
        Task nonLunch = task(2, null);
        Staff staffA = staff(100);
        Staff staffB = staff(101);

        when(taskService.getLLunchTasks()).thenReturn(List.of(lunch));

        List<ScheduleAssignment> assignments = List.of(
            assignment(staffA, lunch, 12, 30),
            assignment(staffB, nonLunch, 13, 30)
        );

        boolean result = policyService.meetPolicy_2(assignments);

        assertFalse(result);
    }

    @Test
    void meetPolicy3ReturnsTrueWhenEveryTimeslotHasDeskTask() {
        Task deskTask = task(5, null);
        Task otherTask = task(6, null);
        Staff s1 = staff(1);
        Staff s2 = staff(2);

        when(taskService.getDeskTask()).thenReturn(deskTask);

        List<ScheduleAssignment> assignments = List.of(
            assignment(s1, deskTask, 9, 0),
            assignment(s2, otherTask, 9, 0),
            assignment(s1, otherTask, 10, 0),
            assignment(s2, deskTask, 10, 0)
        );

        boolean result = policyService.meetPolicy_3(assignments);

        assertTrue(result);
    }

    @Test
    void meetPolicy3ReturnsFalseWhenAnyTimeslotMissesDeskTask() {
        Task deskTask = task(5, null);
        Task otherTask = task(6, null);
        Staff s1 = staff(1);
        Staff s2 = staff(2);

        when(taskService.getDeskTask()).thenReturn(deskTask);

        List<ScheduleAssignment> assignments = List.of(
            assignment(s1, deskTask, 9, 0),
            assignment(s2, otherTask, 9, 0),
            assignment(s1, otherTask, 10, 0),
            assignment(s2, otherTask, 10, 0)
        );

        boolean result = policyService.meetPolicy_3(assignments);

        assertFalse(result);
    }

    @Test
    void meetPolicy4ReturnsFalseForConsecutiveRestrictedTasksForSameStaff() {
        Task checkIn = task(8, null);
        Staff s1 = staff(1);

        when(taskService.getConsecutiveTasks()).thenReturn(List.of(checkIn));

        List<ScheduleAssignment> assignments = List.of(
            assignment(s1, checkIn, 9, 0),
            assignment(s1, checkIn, 10, 0)
        );

        boolean result = policyService.meetPolicy_4(assignments);

        assertFalse(result);
    }

    @Test
    void meetPolicy4ReturnsTrueWhenSameRestrictedTaskIsNotBackToBack() {
        Task roaming = task(9, null);
        Staff s1 = staff(1);

        when(taskService.getConsecutiveTasks()).thenReturn(List.of(roaming));

        List<ScheduleAssignment> assignments = List.of(
            assignment(s1, roaming, 9, 0),
            assignment(s1, roaming, 11, 0)
        );

        boolean result = policyService.meetPolicy_4(assignments);

        assertTrue(result);
    }

    @Test
    void meetPolicy4ReturnsFalseWhenNextTaskAliasesCurrentRestrictedTask() {
        Task checkIn = task(2, null);
        Task lunchCheckIn = task(20, 2);
        Staff s1 = staff(1);

        when(taskService.getConsecutiveTasks()).thenReturn(List.of(checkIn));

        List<ScheduleAssignment> assignments = List.of(
            assignment(s1, checkIn, 12, 0),
            assignment(s1, lunchCheckIn, 13, 0)
        );

        boolean result = policyService.meetPolicy_4(assignments);

        assertFalse(result);
    }

    @Test
    void meetPolicy5ReturnsTrueWhenDeskAndCheckinAreAtMostTwoPerTimeslot() {
        Task desk = task(10, null);
        Task checkin = task(11, null);
        Task other = task(12, null);
        Staff s1 = staff(1);
        Staff s2 = staff(2);
        Staff s3 = staff(3);
        Staff s4 = staff(4);

        when(taskService.getDeskTask()).thenReturn(desk);
        when(taskService.getCheckinTask()).thenReturn(checkin);

        List<ScheduleAssignment> assignments = List.of(
            assignment(s1, desk, 9, 0),
            assignment(s2, desk, 9, 0),
            assignment(s3, checkin, 9, 0),
            assignment(s4, other, 9, 0),
            assignment(s1, checkin, 10, 0),
            assignment(s2, checkin, 10, 0),
            assignment(s3, desk, 10, 0)
        );

        boolean result = policyService.meetPolicy_5(assignments);

        assertTrue(result);
    }

    @Test
    void meetPolicy5ReturnsFalseWhenDeskCountExceedsTwoInAnyTimeslot() {
        Task desk = task(10, null);
        Task checkin = task(11, null);

        when(taskService.getDeskTask()).thenReturn(desk);
        when(taskService.getCheckinTask()).thenReturn(checkin);

        List<ScheduleAssignment> assignments = List.of(
            assignment(staff(1), desk, 9, 0),
            assignment(staff(2), desk, 9, 0),
            assignment(staff(3), desk, 9, 0)
        );

        boolean result = policyService.meetPolicy_5(assignments);

        assertFalse(result);
    }

    @Test
    void meetPolicy5ReturnsFalseWhenCheckinCountExceedsTwoInAnyTimeslot() {
        Task desk = task(10, null);
        Task checkin = task(11, null);

        when(taskService.getDeskTask()).thenReturn(desk);
        when(taskService.getCheckinTask()).thenReturn(checkin);

        List<ScheduleAssignment> assignments = List.of(
            assignment(staff(1), checkin, 11, 0),
            assignment(staff(2), checkin, 11, 0),
            assignment(staff(3), checkin, 11, 0)
        );

        boolean result = policyService.meetPolicy_5(assignments);

        assertFalse(result);
    }

    @Test
    void meetPolicy6ReturnsTrueWhenStaffWithoutBlockHasOptionalSlot() {
        Task block = task(30, null);
        Task optional = task(31, null);
        Task desk = task(32, null);
        Staff s1 = staff(1);
        Staff s2 = staff(2);

        when(taskService.getBlockTask()).thenReturn(block);
        when(taskService.getOptionalTask()).thenReturn(optional);

        List<ScheduleAssignment> assignments = List.of(
            assignment(s1, desk, 9, 0),
            assignment(s1, optional, 10, 0),
            assignment(s2, desk, 9, 0),
            assignment(s2, block, 10, 0)
        );

        boolean result = policyService.meetPolicy_6(assignments);

        assertTrue(result);
    }

    @Test
    void meetPolicy6ReturnsFalseWhenStaffWithoutBlockHasNoOptionalSlot() {
        Task block = task(30, null);
        Task optional = task(31, null);
        Task desk = task(32, null);
        Staff s1 = staff(1);

        when(taskService.getBlockTask()).thenReturn(block);
        when(taskService.getOptionalTask()).thenReturn(optional);

        List<ScheduleAssignment> assignments = List.of(
            assignment(s1, desk, 9, 0),
            assignment(s1, desk, 10, 0)
        );

        boolean result = policyService.meetPolicy_6(assignments);

        assertFalse(result);
    }

    @Test
    void meetPolicy6IgnoresOptionalRequirementWhenStaffHasBlockTask() {
        Task block = task(30, null);
        Task optional = task(31, null);
        Task desk = task(32, null);
        Staff s1 = staff(1);

        when(taskService.getBlockTask()).thenReturn(block);
        when(taskService.getOptionalTask()).thenReturn(optional);

        List<ScheduleAssignment> assignments = List.of(
            assignment(s1, desk, 9, 0),
            assignment(s1, block, 10, 0)
        );

        boolean result = policyService.meetPolicy_6(assignments);

        assertTrue(result);
    }

    private static Staff staff(int id) {
        Staff staff = new Staff();
        staff.setStaffId(id);
        return staff;
    }

    private static Task task(int id, Integer alias) {
        Task task = new Task();
        task.setTaskId(id);
        task.setTaskAlias(alias);
        return task;
    }

    private static ScheduleAssignment assignment(Staff staff, Task task, int hour, int minute) {
        ScheduleAssignment assignment = new ScheduleAssignment();
        assignment.setStaff(staff);
        assignment.setTask(task);
        assignment.setTimeSlot(LocalTime.of(hour, minute));
        return assignment;
    }
}

