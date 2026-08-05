package com.example.shiftplanner_server.services.auto;

import com.example.shiftplanner_server.entities.ScheduleAssignment;
import com.example.shiftplanner_server.entities.Staff;
import com.example.shiftplanner_server.entities.Task;
import com.example.shiftplanner_server.services.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.example.shiftplanner_server.services.ServiceConstant.*;

@Service
@RequiredArgsConstructor
public class AutoService {
    private final TaskService taskService;

    List<ScheduleAssignment> autoAssignTasks(List<ScheduleAssignment> assignments) {
        AutoData autoDate = new AutoData(assignments, taskService.getDeskTask(), taskService.getCheckinTask());
        tryFirstStages_0_to_3(autoDate);
        tryStage_4(autoDate);
        return assignments;
    }


    // 3.3 Implementation: Stage 0 to 3
    void tryFirstStages_0_to_3(AutoData data) {
        // 3.3.1 Flag to indicate whether we are trying to find the best possible solution
        boolean tryBest = true;

        // 3.3.2 high level Implementation of the Stages 0 to 3
        while (data.stage < data.stages.size()) {
            if (tryStage(data, tryBest)) {
                // Pass current stage successfully, we can move on to the next stage
                data.stage++;
            } else {
                // Fail, we need to revert the last successful stage and try again
                data.resetCurrentStage();
                data.stage--; // move back to the previousStage
                if (data.stage < 0) {
                    // No more stages to revert. Which means there is no possible solution
                    if (tryBest) {
                        // Cannot find the best possible solution. Try to find a feasible solution
                        tryBest = false;
                        data.stage = 0; // reset to the first stage
                        continue; // try again
                    } else {
                        // Cannot find a feasible solution, throw an exception
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ERROR_FORMAT_3);
                    }
                }
            }
        }
    }


    // 3.3.3 Implementation of tryStage()
    boolean tryStage(AutoData data, boolean tryBest) {
        Stage stage = data.getCurrentStage();
        while (stage.currentTime.isBefore(ASSIGNMENT_END)) {
            // Find a suitable staff member for the current timeslot and task
            Change change = nextChange(data, stage.currentTime, stage.task, stage.numberOfTasks, stage.currentStaff);
            if (change != null) {
                // save and apply the change to the assignments
                data.applyChange(change);
                continue;
            }
            // If we reach here, it means we couldn't find a suitable staff member for the current timeslot and task.
            if (stage.isMandatory || tryBest) {
                // Mandatory. We must revert or return false if we cannot revert.
                if (stage.changes.isEmpty()) {
                    // Cannot revert
                    return false;
                } else {
                    // Revert the last change and try the next Staff member for the current timeslot and task.
                    data.revertLastChange();
                    continue;
                }
            }
            // Not mandatory. We can skip this timeslot and continue to the next one.
            stage.currentTime = stage.currentTime.plusHours(1);
        }
        return true; // Indicate successful assignment for the current stage
    }


    // 3.3.4 Implementation of nextChange()
    private Change nextChange(AutoData data, LocalTime timeslot, Task task, int numberOfTasks, Staff currentStaff) {
        List<Score> scores = new ArrayList<>();
        for (Staff staff : data.getStaffs()) {
            if (passPolicyCheck(data, timeslot, task, numberOfTasks, currentStaff)) {
                // Calculate the score for the staff member based on the number of tasks assigned
                int scoreValue = calculateScore(data, staff, timeslot, task);
                scores.add(new Score(staff, scoreValue));
            }
        }

        scores.sort(Comparator.comparingInt(Score::value).thenComparing(s -> s.staff().getStaffName()));

        for (int i = 0; i < scores.size(); i++) {
            // Find the current name in the sorted list
            if (scores.get(i).staff().getStaffName().equals(currentStaff.getStaffName())) {
                // Check if there is an element after it
                if (i + 1 < scores.size()) {
                    return new Change(scores.get(i + 1).staff(), timeslot, task); // Return the next staff member in the sorted list
                }
                return null; // No next staff member available
            }
        }
        return null; // No next staff member available
    }


    // 3.3.5 Implementation of calculateScore()
    private int calculateScore(AutoData data, Staff staff, LocalTime timeslot, Task task) {
        // Implement the logic to calculate the score for a staff member based on the number of tasks assigned and other relevant factors.
        // Return the calculated score as an integer value.
        return 0; // Placeholder implementation
    }


    /**
     * Stage 4: change all Optional time slots to actual tasks (if possible)
     * This stage will be executed after all the previous stages have been successfully executed.
     * Replace Optional with only Picking, Roaming or Shelving, since we have done Desk and Check-in in the previous stages.
     * This stage will respect the policies 4 and 6.
     * At the end of the stage, some staffs will have an Optional, according to policy 6.
     */
    void tryStage_4(AutoData data) {
        Task optional = taskService.getOptionalTask();
        // Iterate through all the timeslots and staff members to find Optional time slots
        for (Staff staff : data.getStaffs()) {
            for (LocalTime timeslot : data.getAllTimeslots()) {
                if (data.getTask(staff, timeslot) == optional) {
                    // Try to assign a task to the Optional time slot
                    Task newTask = findSuitableTask(data, staff, timeslot);
                    if (newTask != null) {
                        // Assign the new task to the Optional time slot
                        Change change = new Change(staff, timeslot, newTask);
                        data.applyChange(change);
                    }
                }
            }
        }
    }

    // 3.4.2 Implementation of findSuitableTask()
    private Task findSuitableTask(AutoData data, Staff staff, LocalTime timeslot) {
        // Check the previous and next timeslots to ensure no consecutive tasks are assigned
        Task previousTask = data.getTask(staff, timeslot.minusHours(1));
        Task nextTask = data.getTask(staff, timeslot.plusHours(1));
        List<Task> possibleTasks = List.of(taskService.getPickingTask(),
            taskService.getRoamingTask(),
            taskService.getShelvingTask());
        for (Task task : possibleTasks) {
            if (task != previousTask && task != nextTask) {
                return task;
            }
        }
        return null;
    }

    // 3.5 Implementation of Policy Check (4 & 6 only)
    private boolean passPolicyCheck(AutoData data, LocalTime timeslot, Task task, int numberOfTasks, Staff currentStaff) {
        return passPolicy4(data, timeslot, task, currentStaff) && passPolicy6(data, currentStaff);
    }


    // 3.5.1 Implementation of passPolicy4()
    private boolean passPolicy4(AutoData data, LocalTime timeslot, Task task, Staff currentStaff) {
        // Policy 4. A staff member must not be assigned to two consecutive Desk, Check-in, Picking, Roaming or Shelving tasks.
        // lunch/check-in is an alias to check-in,
        // lunch/roaming is an alias to roaming.
        // That is, there should be
        // no lunch/check-in after a check-in task,
        // and no lunch/roaming after a roaming task.
        // And vice versa.
        if (timeslot.isAfter(ASSIGNMENT_START)) {
            Task previousTask = findTaskByStaffAndTimeslot(data, currentStaff, timeslot.minusHours(1));
            if (previousTask != null) {
                if (task.equals(previousTask) || task.equals(previousTask.getTaskAlias())) {
                    return false; // Consecutive tasks violation
                }
            }
        }

        if (timeslot.isBefore(ASSIGNMENT_END)) {
            Task nextTask = findTaskByStaffAndTimeslot(data, currentStaff, timeslot.plusHours(1));
            if (nextTask != null) {
                return !nextTask.equals(task) && !nextTask.getTaskAlias().equals(task); // Consecutive tasks violation
            }
        }
        return true; // No consecutive tasks violation
    }

    // 3.5.2 Implementation of passPolicy6()
    private boolean passPolicy6(AutoData data, Staff currentStaff) {
        // Policy 6. Staff members working an eight-hour shift must be allocated at least one Optional (unassigned) time slot during the day
        return (!data.getFullTimeStaffs().contains(currentStaff) ||
            hasTwoOptionalTimeSlot(data, currentStaff));
    }

    // 3.5.1 Implementation of findTaskByStaffAndTimeslot()
    private Task findTaskByStaffAndTimeslot(AutoData data, Staff staff, LocalTime timeslot) {
        // Implement the logic to find the assignment of a specific staff member at a specific timeslot.
        // Return the assignment if found, null otherwise.
        return null;
    }

    //3.5.2 Implementation of hasTwoOptionalTimeSlot()
    private boolean hasTwoOptionalTimeSlot(AutoData data, Staff currentStaff) {
        // Implement the logic to check if the staff member has at least one Optional time slot during the day.
        // Return true if the staff member has at least one Optional time slot, false otherwise.
        return false;
    }

}
