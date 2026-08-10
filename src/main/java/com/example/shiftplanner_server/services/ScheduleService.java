package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.entities.Schedule;
import com.example.shiftplanner_server.entities.ScheduleAssignment;
import com.example.shiftplanner_server.entities.Staff;
import com.example.shiftplanner_server.entities.Task;
import com.example.shiftplanner_server.model.Assignment;
import com.example.shiftplanner_server.model.ScheduleParam;
import com.example.shiftplanner_server.repositories.PolicyRepository;
import com.example.shiftplanner_server.repositories.ScheduleRepository;
import com.example.shiftplanner_server.repositories.StaffRepository;
import com.example.shiftplanner_server.repositories.TaskRepository;
import com.example.shiftplanner_server.services.auto.AutoService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.shiftplanner_server.services.ServiceConstant.*;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleAssignmentService scheduleAssignmentService;
    private final StaffRepository staffRepository;
    private final TaskRepository taskRepository;
    private final PolicyRepository policyRepository;
    private final PolicyService policyService;
    private final AutoService autoService;

    public Optional<Schedule> getByDate(LocalDate date) {
        return scheduleRepository.findByDate(date);
    }

    public ScheduleParam getScheduleByDate(LocalDate date) {
        Schedule schedule = scheduleRepository.findByDate(date).orElse(null);
        if (schedule == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found for date: " + date);
        }
        List<ScheduleAssignment> assignments = scheduleAssignmentService.getById(schedule.getScheduleId());
        return new ScheduleParam()
            .date(schedule.getDate() != null ? schedule.getDate().toString() : date.toString())
            .rosterStaffId(staffIdOf(schedule.getRosterStaff()))
            .bankingStaffId(staffIdOf(schedule.getBankingStaff()))
            .backupStaffId(staffIdOf(schedule.getBankingBackupStaff()))
            .inspectionStaffId(staffIdOf(schedule.getBuildingInspector()))
            .notes(schedule.getNotes())
            .policies(parsePolicies(schedule.getPolicies()))
            .assignments(assignments == null
                ? List.of()
                : assignments.stream()
                .filter(Objects::nonNull)
                .map(this::toAssignmentParam)
                .toList());
    }

    private Long staffIdOf(com.example.shiftplanner_server.entities.Staff staff) {
        return staff == null || staff.getStaffId() == null ? null : staff.getStaffId().longValue();
    }

    private List<Long> parsePolicies(String policies) {
        if (policies == null || policies.isBlank()) {
            return List.of();
        }
        return Arrays.stream(policies.split(","))
            .map(String::trim)
            .filter(token -> !token.isEmpty())
            .map(this::parsePolicyId)
            .filter(Objects::nonNull)
            .toList();
    }

    private Long parsePolicyId(String token) {
        try {
            return Long.parseLong(token);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Assignment toAssignmentParam(ScheduleAssignment assignment) {
        return new Assignment()
            .staffId(assignment.getStaff() == null || assignment.getStaff().getStaffId() == null
                ? null
                : assignment.getStaff().getStaffId().longValue())
            .timeSlot(assignment.getTimeSlot() == null ? null : assignment.getTimeSlot().toString())
            .taskId(assignment.getTask() == null || assignment.getTask().getTaskId() == null
                ? null
                : assignment.getTask().getTaskId().longValue());
    }

    /**
     * We will save the schedule and assignments for the given date. If a schedule already exists for that date,
     * it will be replaced with the new one. We won't do the policy check, so that users can save a schedule
     * that violates the policies, and then fix it later.
     *
     * @param date date of the Schedule to be saved
     * @param param data to be saved
     * @return saved Schedule
     */
    public ScheduleParam saveByDate(LocalDate date, ScheduleParam param) {
        // Step 1: validation
        validateRequest(date, param);

        // Step 2: map API request into persistence entities
        Schedule schedule = toScheduleEntity(date, param);
        List<ScheduleAssignment> assignments = toScheduleAssignments(param, schedule);

        // Step 3: save and return
        return saveAndReturnParam(date, schedule, assignments);
    }

    public ScheduleParam autoSchedule(LocalDate date, ScheduleParam param) {
        // Step 1: validation
        validateRequest(date, param);

        // Step 2: map API request into persistence entities
        Schedule schedule = toScheduleEntity(date, param);
        List<ScheduleAssignment> assignments = toScheduleAssignments(param, schedule);
        validateStaffTimeSlot(assignments);

        // Step 3: policy check 1,2,4,5,6. Will do 3 during autoSchedule. 7 isn't mandatory.
        preAutoPolicyCheck(assignments, param.getPolicies());

        // Step 4: (autoSchedule) replace Optional with Desk, Check-in, Picking, Roaming or Shelving tasks when possible
        List<ScheduleAssignment> result = autoService.autoAssignTasks(assignments, param.getPolicies());

        // Step 5: replace param assignments with the result from autoSchedule
        param.setAssignments(result.stream().map(this::toAssignmentParam).toList());
        return param;
    }

    private ScheduleParam saveAndReturnParam(LocalDate date, Schedule schedule, List<ScheduleAssignment> assignments) {
        // Step 1: replace existing schedule content for this date
        scheduleAssignmentService.deleteByDate(date);
        scheduleRepository.findByDate(date).ifPresent(scheduleRepository::delete);

        // Step 2: save schedule first, then attach and save assignments
        Schedule savedSchedule = scheduleRepository.save(schedule);
        assignments.forEach(assignment -> assignment.setSchedule(savedSchedule));
        scheduleAssignmentService.saveToDate(date, assignments);

        // Step 3: return saved view model
        return getScheduleByDate(date);
    }

    public void validateStaffTimeSlot(List<ScheduleAssignment> scheduleAssignments) {
        if (scheduleAssignments == null || scheduleAssignments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Schedule assignments cannot be null or empty");
        }

        // 1. Define the exact required hourly timeSlots from 09:00 to 18:00 (for an 18:00 finish)
        Set<LocalTime> requiredSlots = new HashSet<>();
        for (LocalTime lt = WORK_START; lt.isBefore(WORK_END); lt = lt.plusHours(1)) {
            requiredSlots.add(lt);
        }
        requiredSlots.add(WORK_END); // Include the 18:00 slot


        // 2. Group the assignments by each individual Staff member
        Map<Staff, List<ScheduleAssignment>> staffSchedules = scheduleAssignments.stream()
            .filter(a -> a.getStaff() != null && a.getTimeSlot() != null)
            .collect(Collectors.groupingBy(ScheduleAssignment::getStaff));

        // If there are no valid staff groupings, validation fails
        if (staffSchedules.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No valid staff groupings found");
        }

        // 3. Validate the timeSlots for every single staff member
        for (Map.Entry<Staff, List<ScheduleAssignment>> entry : staffSchedules.entrySet()) {
            Set<LocalTime> staffSlots = getSlots(entry);

            // Fail if the staff member is missing any of the required hourly slots
            if (staffSlots.size() != requiredSlots.size() || !staffSlots.containsAll(requiredSlots)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Schedule assignments are missing required time slots for staff member " + entry.getKey().getStaffName());
            }
        }
    }

    private static @NonNull Set<LocalTime> getSlots(Map.Entry<Staff, List<ScheduleAssignment>> entry) {
        List<ScheduleAssignment> assignments = entry.getValue();
        Set<LocalTime> staffSlots = new HashSet<>();

        for (ScheduleAssignment assignment : assignments) {
            LocalTime slot = assignment.getTimeSlot();

            // Fail immediately if the slot is outside the 09:00 - 18:00 boundary
            if (slot.isBefore(WORK_START) || slot.isAfter(WORK_END)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Schedule assignments contain invalid time slots for staff member " + entry.getKey().getStaffName());
            }

            // Fail immediately if a duplicate timeSlot is found for this staff member
            if (!staffSlots.add(slot)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Schedule assignments contain duplicate time slots for staff member " + entry.getKey().getStaffName());
            }
        }
        return staffSlots;
    }

    /**
     * Check if the assignments violate the policies 1,2,4,5,6.
     * Policy 3 will be checked after auto. Policy 7 is not mandatory.
     *
     * @param assignments scheduleAssignments to be checked.
     * @param policyIds           list of policy IDs to be checked.
     */
    private void preAutoPolicyCheck(List<ScheduleAssignment> assignments, List<Long> policyIds) {
        policyService.checkPolicy_1(assignments, policyIds);
        policyService.checkPolicy_2(assignments, policyIds);
        policyService.checkPolicy_3(assignments, policyIds);
        policyService.checkPolicy_4(assignments, policyIds);
        policyService.checkPolicy_5(assignments, policyIds);
        policyService.checkPolicy_6(assignments, policyIds);
    }

    private void validateRequest(LocalDate date, ScheduleParam param) {
        if (date == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date is required");
        }
        if (param == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Schedule request is required");
        }
        LocalDate requestDate;
        try {
            requestDate = LocalDate.parse(param.getDate());
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request date: " + param.getDate());
        }
        if (!date.equals(requestDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path date must match request date");
        }

        validatePolicies(param.getPolicies());
        validateStaffIds(param);
        validateAssignments(param.getAssignments());
    }

    private void validatePolicies(List<Long> policyIds) {
        List<Long> safePolicyIds = policyIds == null ? List.of() : policyIds;
        List<Integer> ids = safePolicyIds.stream().map(this::toIntId).toList();
        if (policyRepository.findAllById(ids).size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more policies do not exist");
        }
    }

    private void validateStaffIds(ScheduleParam param) {
        List<Integer> ids = Stream.of(
            toIntId(param.getRosterStaffId()),
            toIntId(param.getBankingStaffId()),
            toIntId(param.getBackupStaffId()),
            toIntId(param.getInspectionStaffId())
        ).distinct().toList();
        if (staffRepository.findAllById(ids).size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more role staff IDs do not exist");
        }
    }

    private void validateAssignments(List<Assignment> assignments) {
        List<Assignment> safeAssignments = assignments == null ? List.of() : assignments;
        List<Integer> staffIds = safeAssignments.stream()
            .map(Assignment::getStaffId)
            .map(this::toIntId)
            .distinct()
            .toList();
        List<Integer> taskIds = safeAssignments.stream()
            .map(Assignment::getTaskId)
            .map(this::toIntId)
            .distinct()
            .toList();

        if (!staffIds.isEmpty() && staffRepository.findAllById(staffIds).size() != staffIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more assignment staff IDs do not exist");
        }
        if (!taskIds.isEmpty() && taskRepository.findAllById(taskIds).size() != taskIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more assignment task IDs do not exist");
        }

        for (Assignment assignment : safeAssignments) {
            LocalTime parsedTime;
            try {
                parsedTime = LocalTime.parse(assignment.getTimeSlot());
            } catch (DateTimeParseException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid assignment timeSlot: " + assignment.getTimeSlot());
            }
            if (parsedTime.isBefore(WORK_START) || parsedTime.isAfter(WORK_END)) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Assignment timeSlot must be between 09:00 and 18:00 (inclusive): " + assignment.getTimeSlot()
                );
            }
        }
    }

    private Schedule toScheduleEntity(LocalDate date, ScheduleParam param) {
        Schedule schedule = new Schedule();
        schedule.setDate(date);
        schedule.setNotes(param.getNotes());
        schedule.setRosterStaff(staffRepository.findById(toIntId(param.getRosterStaffId()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid roster staff")));
        schedule.setBankingStaff(staffRepository.findById(toIntId(param.getBankingStaffId()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid banking staff")));
        schedule.setBankingBackupStaff(staffRepository.findById(toIntId(param.getBackupStaffId()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid backup staff")));
        schedule.setBuildingInspector(staffRepository.findById(toIntId(param.getInspectionStaffId()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid inspection staff")));
        schedule.setPolicies((param.getPolicies() == null ? List.<Long>of() : param.getPolicies()).stream()
            .map(String::valueOf)
            .collect(java.util.stream.Collectors.joining(",")));
        return schedule;
    }

    private List<ScheduleAssignment> toScheduleAssignments(ScheduleParam param, Schedule schedule) {
        List<Assignment> safeAssignments = param.getAssignments() == null ? List.of() : param.getAssignments();
        return safeAssignments.stream().map(assignmentParam -> {
            Staff staff = staffRepository.findById(toIntId(assignmentParam.getStaffId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid assignment staff"));
            Task task = taskRepository.findById(toIntId(assignmentParam.getTaskId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid assignment task"));

            ScheduleAssignment assignment = new ScheduleAssignment();
            assignment.setSchedule(schedule);
            assignment.setStaff(staff);
            assignment.setTask(task);
            assignment.setTimeSlot(LocalTime.parse(assignmentParam.getTimeSlot()));
            return assignment;
        }).toList();
    }

    private Integer toIntId(Long id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID cannot be null");
        }
        try {
            return Math.toIntExact(id);
        } catch (ArithmeticException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID out of range: " + id);
        }
    }
}

