package com.example.shiftplanner_server.services.auto;

import com.example.shiftplanner_server.entities.ScheduleAssignment;
import com.example.shiftplanner_server.entities.Staff;
import com.example.shiftplanner_server.entities.Task;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.List;

import static com.example.shiftplanner_server.services.ServiceConstant.ASSIGNMENT_START;

@Getter
@Setter
public class AutoData {

    List<ScheduleAssignment> assignments; // make a copy of the original assignments to work on.
    List<LocalTime> timeslots;   // all timeslots to be processed
    List<Staff> staffs;         // all staffs to be processed
    List<Staff> fullTimeStaffs; // all full-time staffs who need to have at least 1 optional time slot during the day
    List<Stage> stages;        // list to keep track of stages being processed
    int stage;           // index of the current stage being processed

    AutoData(List<ScheduleAssignment> assignments, Task deskTask, Task checkInTask) {
        this.assignments = assignments;
        this.timeslots = getAllTimeslots();
        this.staffs = getAllStaff(assignments);
        this.fullTimeStaffs = getALlFullTimeStaff(assignments);
        this.stages = List.of(
            new Stage(deskTask, 1, true, new ArrayDeque<>(), ASSIGNMENT_START, null), // Policy 3: at least 1 desk per hour
            new Stage(deskTask, 2, false, new ArrayDeque<>(), ASSIGNMENT_START, null), // Policy 5: at most 2 desk per hour
            new Stage(checkInTask, 1, false, new ArrayDeque<>(), ASSIGNMENT_START, null), // Policy 7: at least 1 check-in per hour
            new Stage(checkInTask, 2, false, new ArrayDeque<>(), ASSIGNMENT_START, null) // Policy 5: at most 2 check-in per hour
        );
        this.stage = 0;
    }

    public List<LocalTime> getAllTimeslots() {
        return List.of();
    }

    public List<Staff> getAllStaff(List<ScheduleAssignment> assignments) {
        return List.of();
    }

    public List<Staff> getALlFullTimeStaff(List<ScheduleAssignment> assignments) {
        return List.of();
    }

    public Stage getCurrentStage() {
        return stages.get(stage);
    }

    public void resetCurrentStage() {
        getCurrentStage().reset();
    }

    public void applyChange(Change change) {
//        assignments.apply(change);
    }

    public void revertLastChange() {
        Stage currentStage = getCurrentStage();
        if (!currentStage.changes.isEmpty()) {
            Change lastChange = currentStage.changes.peekLast();
//            assignments.revert(lastChange);
            currentStage.currentTime = lastChange.timeslot(); // Go back to the last assigned timeslot to reassign
            currentStage.currentStaff = lastChange.staff(); // Go back to the last assigned staff member to reassign
            currentStage.changes.removeLast(); // Remove the last change from the stack
        }
    }

    public Task getTask(Staff staff, LocalTime timeslot) {
        return null;
    }

}
