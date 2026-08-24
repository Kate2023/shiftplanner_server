package com.example.shiftplanner_server.services.auto;

import com.example.shiftplanner_server.entities.ScheduleAssignment;
import com.example.shiftplanner_server.entities.Staff;
import com.example.shiftplanner_server.entities.Task;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.shiftplanner_server.services.ServiceConstant.WORK_START;

@Getter
@Setter
public class AutoData {

    List<ScheduleAssignment> assignments; // make a copy of the original assignments to work on.
    List<Long> policyIds;
    List<Staff> staffs;         // all staffs to be processed
    List<Stage> stages;        // list to keep track of stages being processed
    int stage;           // index of the current stage being processed

    AutoData(List<ScheduleAssignment> assignments, List<Long> policyIds, Task deskTask, Task checkInTask) {
        this.assignments = assignments;
        this.policyIds = policyIds;
        this.staffs = getAllStaff();

        this.stages = new ArrayList<>();
        if (policyIds.contains(3L)) {
            this.stages.add(new Stage(3L, deskTask, 1, true, new ArrayDeque<>(), WORK_START, null)); // Policy 3: at least 1 desk
        }
        if (policyIds.contains(5L)) {
            this.stages.add(new Stage(5L, deskTask, 2, false, new ArrayDeque<>(), WORK_START, null)); // Policy 5: at most 2 desk
        }
        if (policyIds.contains(7L)) {
            this.stages.add(new Stage(7L, checkInTask, 1, false, new ArrayDeque<>(), WORK_START, null)); // Policy 7: at least 1 check-in per hour
        }
        // Disable this stage for now, as it is not needed for the current implementation
//        if (policyIds.contains(5L)) {
//            this.stages.add(new Stage(5L, checkInTask, 2, false, new ArrayDeque<>(), WORK_START, null)); // Policy 5: at most 2 check-in
//        }

        this.stage = 0;
    }

    public List<Staff> getAllStaff() {
        return assignments.stream()
            .map(ScheduleAssignment::getStaff)
            .distinct()
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Staff> getShuffledStaffs() {
        List<Staff> shuffled = new ArrayList<>(staffs);
        Collections.shuffle(shuffled);
        return shuffled;
    }

    public List<LocalTime> getAllTimeSlots() {
        return assignments.stream()
            .map(ScheduleAssignment::getTimeSlot)
            .distinct()
            .sorted()
            .toList();
    }

    public Stage getCurrentStage() {
        return stages.get(stage);
    }

    public void applyChange(Change change) {
        ScheduleAssignment assignment = findAssignment(change.staff(), change.timeSlot());
        if (assignment != null) {
            assignment.setTask(change.task());
        }
    }

    public void revertChange(Change change) {
        ScheduleAssignment assignment = findAssignment(change.staff(), change.timeSlot());
        if (assignment != null) {
            assignment.setTask(change.original());
        }
    }

    private ScheduleAssignment findAssignment(Staff staff, LocalTime timeSlot) {
        return assignments.stream()
            .filter(a -> a.getStaff().equals(staff) && a.getTimeSlot().equals(timeSlot))
            .findFirst()
            .orElse(null);
    }

    public Task getTask(Staff staff, LocalTime timeSlot) {
        ScheduleAssignment assignment = findAssignment(staff, timeSlot);
        return assignment != null ? assignment.getTask() : null;
    }

    public Long countByTask(Task task) {
        return assignments.stream().filter(a -> a.getTask().equals(task)).count();
    }

    public String getTaskString() {
        StringBuilder s = new StringBuilder();
        s.append(String.format("'%-10s':", "Time/Staff"));
        for (Staff staff : getAllStaff()) {
            s.append(String.format("'%-13s',", staff.getStaffName()));
        }
        s.append("\n");

        for (LocalTime time : getAllTimeSlots()) {
            s.append(String.format("'%-10s':", time));
            for (Staff staff : getAllStaff()) {
                Task task = getTask(staff, time);
                s.append(String.format("'%-13s',", task != null ? task.getTaskName() : "None"));
            }
            s.append("\n");
        }
        return s.toString();
    }
}
