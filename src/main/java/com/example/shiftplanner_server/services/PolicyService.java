package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.entities.Policy;
import com.example.shiftplanner_server.entities.ScheduleAssignment;
import com.example.shiftplanner_server.entities.Task;
import com.example.shiftplanner_server.model.PolicyParam;
import com.example.shiftplanner_server.model.PolicyUpdateRequest;
import com.example.shiftplanner_server.repositories.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.shiftplanner_server.services.ServiceConstant.*;

@Service
@RequiredArgsConstructor
public class PolicyService {
    private final PolicyRepository policyRepository;
    private final ScheduleAssignmentService scheduleAssignmentService;
    private final TaskService taskService;

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
     * @param assignments assignments to be checked.
     * @param policyIds   list of policy IDs to be checked.
     */
    public void checkPolicy_1(List<ScheduleAssignment> assignments, List<Long> policyIds) {
        if (policyIds == null || policyIds.isEmpty() || !policyIds.contains(1L)) {
            return; // Policy 1 is not applicable
        }
//        List<Integer> staffIds = scheduleAssignmentService.getDistinctStaffs(assignments);
        int requiredStaff = policyRepository.findById(1).orElseThrow().getParam1();
        Task lunchTask = taskService.getLunchTask();
        Task blockTask = taskService.getBlockTask();
//        if (!(staffIds.size() >= requiredStaff)) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(ERROR_FORMAT_1, "1",
//                "Need at least " + requiredStaff + " staff members"));
//        }

        assignments.stream()
            .collect(Collectors.groupingBy(ScheduleAssignment::getTimeSlot))
            .forEach((timeSlot, ssa) -> {
                if (!timeSlot.isBefore(LUNCH_START) && timeSlot.isBefore(LUNCH_END)) {
                    long count = ssa.stream()
                        .filter(sa -> !lunchTask.equals(sa.getTask())
                            && !blockTask.equals(sa.getTask()))
                        .count();
                    boolean hasEnoughStaffs = count >= requiredStaff;

                    if (!hasEnoughStaffs) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            String.format(ERROR_FORMAT_1, "1", "Need at least " + requiredStaff + " staff members at " + timeSlot));
                    }
                }
            });
    }

    /**
     * Check with Policy 2:
     * Between 12:00 p.m. and 2:00 p.m., every staff member must be allocated one of the four tasks:
     * lunch break, lunch/ check-in, lunch/bell, lunch/roaming or block.
     *
     * @param assignments assignments to be checked.
     * @param policyIds   list of policy IDs to be checked.
     */

    public void checkPolicy_2(List<ScheduleAssignment> assignments, List<Long> policyIds) {
        if (policyIds == null || policyIds.isEmpty() || !policyIds.contains(2L)) {
            return; // Policy 2 is not applicable
        }
        List<Task> lunches = taskService.getLunchTasks();

        assignments.stream()
            .collect(Collectors.groupingBy(ScheduleAssignment::getStaff))
            .forEach((staff, ssa) -> {
                boolean hasLunch = ssa.stream().anyMatch(sa ->
                    !sa.getTimeSlot().isBefore(LUNCH_START)
                        && sa.getTimeSlot().isBefore(LUNCH_END)
                        && lunches.contains(sa.getTask())
                );

                if (!hasLunch) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format(ERROR_FORMAT_1, "2", staff.getStaffName() + " needs to have Lunch"));
                }
            });

    }

    /**
     * Check with Policy 3:
     * At least one staff member must be assigned to the service desk during every hourly time slot.
     *
     * @param assignments assignments to be checked.
     * @param policyIds   list of policy IDs to be checked.
     */

    public void checkPolicy_3(List<ScheduleAssignment> assignments, List<Long> policyIds) {
        if (policyIds == null || policyIds.isEmpty() || !policyIds.contains(3L)) {
            return; // Policy 3 is not applicable
        }
        Task deskTask = taskService.getDeskTask();
        Task optionalTask = taskService.getOptionalTask();

        // Group tasks by their timeSlot, then check if every timeSlot contains the Desk or Optional task
        for (LocalTime lt : assignments.stream().map(ScheduleAssignment::getTimeSlot).distinct().toList()) {
            boolean hasDeskTask = assignments.stream()
                .anyMatch(sa -> lt.equals(sa.getTimeSlot())
                    && (deskTask.equals(sa.getTask()) || optionalTask.equals(sa.getTask())));
            if (lt.isBefore(WORK_END) && !hasDeskTask) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format(ERROR_FORMAT_1, "3", lt + " Service Desk needs people"));
            }
        }
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
     * @param assignments assignments to be checked.
     * @param policyIds   list of policy IDs to be checked.
     */

    public void checkPolicy_4(List<ScheduleAssignment> assignments, List<Long> policyIds) {
        if (policyIds == null || policyIds.isEmpty() || !policyIds.contains(4L)) {
            return; // Policy 4 is not applicable
        }

        // Fetch the list of restricted tasks and map their IDs to a Set for O(1) lookups
        List<Task> consecutiveTasks = taskService.getConsecutiveTasks();

        // Step 1: Group assignments by Staff
        assignments.stream()
            .sorted(Comparator.comparing(ScheduleAssignment::getTimeSlot))
            .collect(Collectors.groupingBy(ScheduleAssignment::getStaff))
            .forEach((staff, ssa) -> {
                for (int i = 0; i < ssa.size() - 1; i++) {
                    ScheduleAssignment current = ssa.get(i);
                    ScheduleAssignment next = ssa.get(i + 1);
                    Task currentTask = current.getTask();
                    Task nextTask = next.getTask();

                    // Guard clause to safely skip null tasks or IDs
                    if (currentTask == null || nextTask == null ||
                        currentTask.getTaskId() == null || nextTask.getTaskId() == null) {
                        continue;
                    }

                    // Check if it's the exact same task type by ID
                    if ((currentTask.equals(nextTask) || currentTask.equals(nextTask.getTaskAlias()))
                        // Check if this task ID is one of the restricted consecutive tasks
                        && (consecutiveTasks.contains(currentTask) || consecutiveTasks.contains(currentTask.getTaskAlias()))
                        // Check if the timeSlots are exactly consecutive (1 hour apart)
                        && (current.getTimeSlot().plusHours(1).equals(next.getTimeSlot()))) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            String.format(ERROR_FORMAT_1, "4",
                                current.getStaff().getStaffName() + ". Consecutive tasks:\n"
                                    + current.getTimeSlot() + " " + currentTask.getTaskName() + "\n"
                                    + next.getTimeSlot() + " " + nextTask.getTaskName()));
                    }
                }
            });
    }

    /**
     * Check with Policy 5:
     * At most two staff members should be assigned to the desk or Check-in during each hourly time slot whenever possible.
     * Each task can have up to two for each timeSlot.
     *
     * @param assignments assignments to be checked.
     * @param policyIds   list of policy IDs to be checked.
     */

    public void checkPolicy_5(List<ScheduleAssignment> assignments, List<Long> policyIds) {
        if (policyIds == null || policyIds.isEmpty() || !policyIds.contains(5L)) {
            return; // Policy 5 is not applicable
        }

        Task desk = taskService.getDeskTask();
        Task checkin = taskService.getCheckinTask();

        assignments.stream()
            .collect(Collectors.groupingBy(ScheduleAssignment::getTimeSlot))
            .forEach((timeSlot, sat) -> {
                int count = (int) sat.stream().filter(sa -> desk.equals(sa.getTask())).count();
                if (count > 2) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format(ERROR_FORMAT_1, "5", timeSlot + " " + count + " people at Service Desk"));
                }

                count = (int) sat.stream()
                    .filter(sa -> checkin.equals(sa.getTask())
                        || (checkin.getTaskAlias() != null
                        && checkin.getTaskAlias().equals(sa.getTask())))
                    .count();
                if (checkin != null && count > 2) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format(ERROR_FORMAT_1, "5", timeSlot + " " + count + " people at Check-in"));
                }
            });
    }

    /**
     * Check with Policy 6:
     * Staff members working an eight-hour shift (no Block) must be allocated at least one Optional (unassigned) time slot during the day.
     *
     * @param assignments assignments to be checked.
     * @param policyIds   list of policy IDs to be checked.
     */

    public void checkPolicy_6(List<ScheduleAssignment> assignments, List<Long> policyIds) {
        if (policyIds == null || policyIds.isEmpty() || !policyIds.contains(6L)) {
            return; // Policy 6 is not applicable
        }

        Task block = taskService.getBlockTask();
        Task optional = taskService.getOptionalTask();

        assignments.stream()
            .collect(Collectors.groupingBy(ScheduleAssignment::getStaff))
            .forEach((staff, ssa) -> {
                boolean hasBlock = ssa.stream()
                    .anyMatch(sa -> block.equals(sa.getTask()) && sa.getTimeSlot().isBefore(WORK_END));

                if (!hasBlock) {
                    boolean hasOptionalSlot = ssa.stream().anyMatch(sa -> optional.equals(sa.getTask()));

                    if (!hasOptionalSlot) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            String.format(ERROR_FORMAT_1, "6", staff.getStaffName() + " needs an Optional slot"));
                    }
                }
            });
    }
}
