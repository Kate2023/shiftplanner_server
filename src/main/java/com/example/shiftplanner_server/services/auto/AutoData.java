package com.example.shiftplanner_server.services.auto;

import com.example.shiftplanner_server.entities.ScheduleAssignment;
import com.example.shiftplanner_server.entities.Staff;
import com.example.shiftplanner_server.entities.Task;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
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
        this.staffs = getAllStaff(assignments);

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
        if (policyIds.contains(5L)) {
            this.stages.add(new Stage(5L, checkInTask, 2, false, new ArrayDeque<>(), WORK_START, null)); // Policy 5: at most 2 check-in
        }

        this.stage = 0;
    }

    public List<Staff> getAllStaff(List<ScheduleAssignment> assignments) {
        return assignments.stream()
            .map(ScheduleAssignment::getStaff)
            .distinct()
            .collect(Collectors.toList());
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
}
