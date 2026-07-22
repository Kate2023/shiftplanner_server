package com.example.shiftplanner_server.services;

import com.example.shiftplanner_server.model.Assignment;
import com.example.shiftplanner_server.model.ScheduleParam;
import com.example.shiftplanner_server.entities.Schedule;
import com.example.shiftplanner_server.entities.ScheduleAssignment;
import com.example.shiftplanner_server.entities.Staff;
import com.example.shiftplanner_server.entities.Task;
import com.example.shiftplanner_server.repositories.PolicyRepository;
import com.example.shiftplanner_server.repositories.ScheduleRepository;
import com.example.shiftplanner_server.repositories.StaffRepository;
import com.example.shiftplanner_server.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private static final LocalTime ASSIGNMENT_START_TIME = LocalTime.of(9, 0);
    private static final LocalTime ASSIGNMENT_END_TIME = LocalTime.of(18, 0);

    private final ScheduleRepository scheduleRepository;
    private final ScheduleAssignmentService scheduleAssignmentService;
    private final StaffRepository staffRepository;
    private final TaskRepository taskRepository;
    private final PolicyRepository policyRepository;

    public Optional<Schedule> getByDate(LocalDate date) {
        return scheduleRepository.findByDate(date);
    }

    public void generate(LocalDate date, Schedule schedule) {
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

    public ScheduleParam autoScheduleAndSave(LocalDate date, ScheduleParam request) {
        // TODO: run auto-scheduling flow and persist result.
        return null;
    }

    public ScheduleParam saveByDate(LocalDate date, ScheduleParam param) {
        validateRequest(date, param);

        // Step 2: map API request into persistence entities
        Schedule schedule = toScheduleEntity(date, param);
        List<ScheduleAssignment> assignments = toScheduleAssignments(param, schedule);

        // Step 3: replace existing schedule content for this date
        scheduleAssignmentService.deleteByDate(date);
        scheduleRepository.findByDate(date).ifPresent(scheduleRepository::delete);

        // Step 4: save schedule first, then attach and save assignments
        Schedule savedSchedule = scheduleRepository.save(schedule);
        assignments.forEach(assignment -> assignment.setSchedule(savedSchedule));
        scheduleAssignmentService.saveToDate(date, assignments);

        // Step 5: return saved view model
        return getScheduleByDate(date);
    }

    private void validateRequest(LocalDate date, ScheduleParam param) {
        if (date == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date is required");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date cannot be earlier than today");
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
        Set<Integer> ids = Set.of(
                toIntId(param.getRosterStaffId()),
                toIntId(param.getBankingStaffId()),
                toIntId(param.getBackupStaffId()),
                toIntId(param.getInspectionStaffId())
        );
        if (staffRepository.findAllById(ids).size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more role staff IDs do not exist");
        }
    }

    private void validateAssignments(List<Assignment> assignments) {
        List<Assignment> safeAssignments = assignments == null ? List.of() : assignments;
        Set<Integer> staffIds = safeAssignments.stream()
                .map(Assignment::getStaffId)
                .map(this::toIntId)
                .collect(java.util.stream.Collectors.toSet());
        Set<Integer> taskIds = safeAssignments.stream()
                .map(Assignment::getTaskId)
                .map(this::toIntId)
                .collect(java.util.stream.Collectors.toSet());

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
            if (parsedTime.isBefore(ASSIGNMENT_START_TIME) || parsedTime.isAfter(ASSIGNMENT_END_TIME)) {
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

