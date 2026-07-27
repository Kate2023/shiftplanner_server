package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.entities.Policy;
import com.example.shiftplanner_server.entities.ScheduleAssignment;
import com.example.shiftplanner_server.entities.Staff;
import com.example.shiftplanner_server.entities.Task;
import com.example.shiftplanner_server.model.PolicyParam;
import com.example.shiftplanner_server.model.PolicyUpdateRequest;
import com.example.shiftplanner_server.repositories.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicyService {
    private final PolicyRepository policyRepository;
    private final ScheduleAssignmentService scheduleAssignmentService;
    private final TaskService taskService;

    private final LocalTime start = LocalTime.of(12, 0); // 12:00
    private final LocalTime end = LocalTime.of(14, 0);   // 14:00

    public List<Policy> getAll() {
        return policyRepository.findAll();
    }

    public Policy save(Policy policy) {
        return policyRepository.save(policy);
    }

    public List<PolicyParam> getAllParams() {
        return getAll().stream()
            .map(policy -> new PolicyParam()
                .policyId(Long.valueOf(policy.getPolicyId()))
                .description(policy.getDescription())
                .param1(policy.getParam1() == null ? 0L : Long.valueOf(policy.getParam1())))
            .toList();
    }

    public List<PolicyParam> update(Integer policyId, PolicyUpdateRequest request) {
        Policy policy = policyRepository.findById(policyId)
            .orElseThrow(() -> new RuntimeException("Policy not found with id: " + policyId));
        policy.setParam1(Math.toIntExact(request.getParam1()));
        policyRepository.save(policy);
        return getAllParams();
    }

    /**
     * Check with Policy 1:
     * Excluding staff who are on their lunch break, there must be at least {param_1}
     * staff members present in the library at all times.
     *
     * @param scheduleAssignments scheduleAssignments to be checked.
     * @return if the number of staffs meets the policy requirement.
     */
    public boolean meetPolicy_1(List<ScheduleAssignment> scheduleAssignments) {
        List<Integer> staffIds = scheduleAssignmentService.getDistinctStaffs(scheduleAssignments);
        return staffIds.size() >= policyRepository.findById(1).orElseThrow().getParam1();
    }

    /**
     * Check with Policy 2:
     * Between 12:00 p.m. and 2:00 p.m., every staff member must be allocated one of the four tasks:
     * lunch break, lunch/ check-in, lunch/bell or lunch/roaming.
     *
     * @param scheduleAssignments scheduleAssignments to be checked.
     * @return if the scheduleAssignments meets the policy requirement.
     */

    public boolean meetPolicy_2(List<ScheduleAssignment> scheduleAssignments) {
        List<Integer> lunchIds = taskService.getLLunchTasks().stream()
            .map(Task::getTaskId)
            .toList();

        // Step 1: Group assignments by Staff
        Map<Staff, List<ScheduleAssignment>> staffSchedules = scheduleAssignments.stream()
            .collect(Collectors.groupingBy(ScheduleAssignment::getStaff));
        // Step 2: Check each staff member's assignments
        for (List<ScheduleAssignment> staffAssignments : staffSchedules.values()) {
            // Step 3: Check if the staff member has at least one assignment in the lunch period
            boolean hasLunchAssignment = staffAssignments.stream()
                .anyMatch(scheduleAssignment ->
                    scheduleAssignment.getTimeSlot().isAfter(start)
                        && scheduleAssignment.getTimeSlot().isBefore(end)
                        && lunchIds.contains(scheduleAssignment.getTask().getTaskId()));
            if (!hasLunchAssignment) {
                return false; // Found a staff member without a lunch assignment
            }
        }
        return true; // All staff members have at least one lunch assignment
    }

    /**
     * Check with Policy 3:
     * At least one staff member must be assigned to the service desk during every hourly time slot.
     *
     * @param scheduleAssignments scheduleAssignments to be checked.
     * @return if the scheduleAssignments meets the policy requirement.
     */

    public boolean meetPolicy_3(List<ScheduleAssignment> scheduleAssignments) {
        Integer deskTaskId = taskService.getDeskTask().getTaskId();

        // Group tasks by their timeSlot, then check if every timeSlot contains the deskTask
        return scheduleAssignments.stream()
            .collect(Collectors.groupingBy(
                ScheduleAssignment::getTimeSlot,
                Collectors.mapping(ScheduleAssignment::getTask, Collectors.toList())
            ))
            .values()
            .stream()
            .allMatch(tasksAtSlot -> tasksAtSlot.stream()
                .anyMatch(task -> task != null && task.getTaskId().equals(deskTaskId))
            );
    }

    /**
     * Check with Policy 4:
     * A staff member must not be assigned to two consecutive Desk, Check-in, Picking, Roaming or Shelving tasks.
     * Note:
     * lunch/check-in is an alias to check-in,
     * lunch/roaming is an alias to roaming.
     * That is, there should be
     * no lunch/check-in after a check-in task,
     * and no lunch/roaming after a roaming task.
     * And vice versa.
     *
     * @param scheduleAssignments scheduleAssignments to be checked.
     * @return if the scheduleAssignments meets the policy requirement.
     */

    public boolean meetPolicy_4(List<ScheduleAssignment> scheduleAssignments) {

        // Fetch the list of restricted tasks and map their IDs to a Set for O(1) lookups
        List<Task> consecutiveTasks = taskService.getConsecutiveTasks();
        if (consecutiveTasks == null || consecutiveTasks.isEmpty()) {
            return true;
        }

        Set<Integer> restrictedTaskIds = consecutiveTasks.stream()
            .map(Task::getTaskId)
            .collect(Collectors.toSet());

        // Step 1: Group assignments by Staff
        Map<Staff, List<ScheduleAssignment>> staffSchedules = scheduleAssignments.stream()
            .collect(Collectors.groupingBy(ScheduleAssignment::getStaff));

        // Step 2: Check each staff member's assignments
        for (List<ScheduleAssignment> staffAssignments : staffSchedules.values()) {

            // Sort the staff's assignments chronologically by timeSlot
            List<ScheduleAssignment> sortedAssignments = staffAssignments.stream()
                .sorted(Comparator.comparing(ScheduleAssignment::getTimeSlot))
                .toList();

            // Step 3: Compare adjacent, back-to-back assignments
            for (int i = 0; i < sortedAssignments.size() - 1; i++) {
                ScheduleAssignment current = sortedAssignments.get(i);
                ScheduleAssignment next = sortedAssignments.get(i + 1);

                Task currentTask = current.getTask();
                Task nextTask = next.getTask();

                // Skip if either task or ID is missing
                if (currentTask == null || nextTask == null ||
                    currentTask.getTaskId() == null || nextTask.getTaskId() == null) {
                    continue;
                }

                // Check if it's the exact same task type by ID
                if (currentTask.getTaskId().equals(nextTask.getTaskId())
                    || currentTask.getTaskId().equals(nextTask.getTaskAlias())) {

                    // Check if this task ID is one of the restricted consecutive tasks
                    if (restrictedTaskIds.contains(currentTask.getTaskId()) ||
                        restrictedTaskIds.contains(currentTask.getTaskAlias())) {

                        // Check if the timeslots are exactly consecutive (1 hour apart)
                        if (current.getTimeSlot().plusHours(1).equals(next.getTimeSlot())) {
                            return false; // Found consecutive matching restricted tasks!
                        }
                    }
                }
            }
        }
        return true; // No violations found across any staff schedules
    }


    /**
     * Check with Policy 5:
     * At most two staff members should be assigned to the desk or Check-in during each hourly time slot whenever possible.
     * Each task can have up to two for each timeslot.
     *
     * @param scheduleAssignments scheduleAssignments to be checked.
     * @return if the scheduleAssignments meets the policy requirement.
     */

    public boolean meetPolicy_5(List<ScheduleAssignment> scheduleAssignments) {

        Integer deskTaskId = taskService.getDeskTask().getTaskId();
        Integer checkinTaskId = taskService.getCheckinTask().getTaskId();

        // Group tasks by their timeSlot
        Map<LocalTime, List<ScheduleAssignment>> groupedByTimeSlot = scheduleAssignments.stream()
            .collect(Collectors.groupingBy(ScheduleAssignment::getTimeSlot));

        // Check if every timeSlot contains more than 2 desk or check-in tasks
        for (Map.Entry<LocalTime, List<ScheduleAssignment>> entry : groupedByTimeSlot.entrySet()) {
            List<ScheduleAssignment> assignmentsAtSlot = entry.getValue();
            long deskCount = assignmentsAtSlot.stream()
                .filter(sa -> sa.getTask() != null && sa.getTask().getTaskId().equals(deskTaskId))
                .count();
            long checkinCount = assignmentsAtSlot.stream()
                .filter(sa -> sa.getTask() != null && sa.getTask().getTaskId().equals(checkinTaskId))
                .count();

            if (deskCount > 2 || checkinCount > 2) {
                return false;
            }
        }
        return true; // No violations found across any staff schedules
    }

    /**
     * Check with Policy 6:
     * Staff members working an eight-hour shift (no Block) must be allocated at least one Optional (unassigned) time slot during the day.
     *
     * @param scheduleAssignments scheduleAssignments to be checked.
     * @return if the scheduleAssignments meets the policy requirement.
     */

    public boolean meetPolicy_6(List<ScheduleAssignment> scheduleAssignments) {
        Integer blockTaskId = taskService.getBlockTask().getTaskId();
        Integer optionalTaskId = taskService.getOptionalTask().getTaskId();

        // Step 1: Group assignments by Staff
        Map<Staff, List<ScheduleAssignment>> staffSchedules = scheduleAssignments.stream()
            .collect(Collectors.groupingBy(ScheduleAssignment::getStaff));

        // Step 2: Check each staff member's assignments
        for (List<ScheduleAssignment> staffAssignments : staffSchedules.values()) {
            // Step 3: Check if the staff member has an 8-hour shift without a Block task
            boolean hasBlockTask = staffAssignments.stream()
                .anyMatch(sa -> sa.getTask() != null && sa.getTask().getTaskId().equals(blockTaskId));

            if (!hasBlockTask) {
                // Step 4: Check if the staff member has at least one Optional (unassigned) time slot
                boolean hasOptionalSlot = staffAssignments.stream()
                    .anyMatch(sa -> sa.getTask() != null && sa.getTask().getTaskId().equals(optionalTaskId));

                if (!hasOptionalSlot) {
                    return false; // Found a staff member with an 8-hour shift and no Optional slot
                }
            }
        }
        return true; // No violations found across any staff schedules
    }


}
