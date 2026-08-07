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

    // 1. The policies priority in the below order:
    // Policy 4: no consecutive Desk, Check-in, Picking, Roaming or Shelving tasks (must be enforced during the assignment process)
    // Policy 6: at least 1 optional time slot for 8-hour shifts (must be enforced during the assignment process)
    // Policy 3: at least 1 desk per hour (if not possible, throw an exception)
    // Policy 5: at most 2 desk per hour (only if possible)
    // Policy 7: at least 1 check-in per hour(only if possible)
    // Policy 5: at most 2 check-in per hour(only if possible)

    // 2.  Implementation Stages
    // Note: Policies 4 and 6 will be checked all the time during the following stages
    // Stage 0: do Policy 3
    // Stage 1: do Policy 5 (2 desk per hour)
    // Stage 2: do Policy 7
    // Stage 3: do Policy 5 (2 check-in per hour)
    // Stage 4: change all Optional time slots to actual tasks (if possible)
    public List<ScheduleAssignment> autoAssignTasks(List<ScheduleAssignment> assignments, List<Long> policyIds) {
        AutoData autoData = new AutoData(assignments, policyIds, taskService.getDeskTask(), taskService.getCheckinTask());
        tryFirstStages_0_to_3(autoData);
        tryStage_4(autoData);
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
                data.getCurrentStage().reset();
                data.stage--; // move back to the previousStage
                if (data.stage < 0) {
                    // No more stages to revert. Which means there is no possible solution
                    if (tryBest) {
                        // Cannot find the best possible solution. Try to find a feasible solution
                        tryBest = false;
                        data.stage = 0; // reset to the first stage
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
        while (stage.time.isBefore(WORK_END)) {
            // Find the next suitable staff member for the current timeSlot and task
            Change change = nextChange(data, stage);
            if (change != null) {
                // save and apply the change to the assignments
                data.applyChange(change);
                stage.changes.add(change);
                continue;
            }
            // If we reach here, it means we couldn't find a suitable staff member for the current timeSlot and task.
            if (stage.isMandatory || tryBest) {
                // Mandatory. We must revert or return false if we cannot revert.
                if (stage.changes.isEmpty()) {
                    // Cannot revert
                    return false;
                } else {
                    // Revert the last change
                    Change lastChange = stage.changes.pollLast();
                    data.applyChange(lastChange);
                    stage.time = lastChange.timeSlot(); // Go back to the last assigned timeSlot to reassign
                    stage.staff = lastChange.staff(); // Go back to the last assigned staff member to reassign
                    continue;
                }
            }
            // Not mandatory. We can skip this timeSlot and continue to the next one.
            stage.time = stage.time.plusHours(1);
        }
        return true; // Indicate successful assignment for the current stage
    }

    // 3.3.4 Implementation of nextChange()
    private Change nextChange(AutoData data, Stage stage) {

        List<Score> scores = new ArrayList<>();
        for (Staff staff : data.getStaffs()) {
            if (passPolicyCheck(data, stage, staff)) {
                // Calculate the score for the staff member based on the number of tasks assigned
                scores.add(new Score(staff, calculateScore(data, staff, stage.task)));
            }
        }

        scores.sort(Comparator.comparingLong(Score::value).thenComparing(s -> s.staff().getStaffName()));

        for (int i = 0; i < scores.size(); i++) {
            // Find the current staff member in the sorted list
            if (scores.get(i).staff().equals(stage.staff)) {
                // Check if there is an element after it
                if (i + 1 < scores.size()) {
                    return new Change(scores.get(i + 1).staff(), stage.time, stage.task); // Return the next staff member in the sorted list
                }
                return null; // No next staff member available
            }
        }
        return null; // No next staff member available
    }

    // 3.3.5 Implementation of calculateScore()
    private long calculateScore(AutoData data, Staff staff, Task task) {
        // At the moment, we use the number of the tasks that has been assigned to the staff.
        // In the future, we can modify this one to make it seems more random.
        return data.assignments.stream()
            .filter(a -> a.getStaff().equals(staff) && a.getTask().equals(task))
            .count();
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
        // Iterate through all the timeSlots and staff members to find Optional time slots
        for (Staff staff : data.getStaffs()) {
            for (LocalTime t = WORK_START; t.isBefore(WORK_END); t = t.plusHours(1)) {
                if (data.getTask(staff, t) == optional) {
                    // Try to assign a task to the Optional time slot
                    Task newTask = findSuitableTask(data, staff, t);
                    if (newTask != null) {
                        // Assign the new task to the Optional time slot
                        Change change = new Change(staff, t, newTask);
                        data.applyChange(change);
                    }
                }
            }
        }
    }

    // 3.4.2 Implementation of findSuitableTask()
    private Task findSuitableTask(AutoData data, Staff staff, LocalTime time) {
        // Check the previous and next timeSlots to ensure no consecutive tasks are assigned
        Task previousTask = data.getTask(staff, time.minusHours(1));
        Task nextTask = data.getTask(staff, time.plusHours(1));
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
    private boolean passPolicyCheck(AutoData data, Stage stage, Staff staff) {
        return passPolicy4(data, stage, staff)
            && passPolicy6(data, staff);
    }

    // 3.5.1 Implementation of passPolicy4()
    private boolean passPolicy4(AutoData data, Stage stage, Staff staff) {
        if (!data.policyIds.contains(4L)) {
            return true; // Policy 4 is not enabled, so we can skip the check
        }

        // Policy 4. A staff member must not be assigned to two consecutive Desk, Check-in, Picking, Roaming or Shelving tasks.
        // lunch/check-in is an alias to check-in,
        // lunch/roaming is an alias to roaming.
        // That is, there should be
        // no lunch/check-in after a check-in task,
        // and no lunch/roaming after a roaming task.
        // And vice versa.
        LocalTime time = stage.time;
        Task task = stage.task;
        if (time.isAfter(WORK_START)) {
            Task previousTask = data.getTask(staff, time.minusHours(1));
            if (previousTask != null) {
                if (task.equals(previousTask) || task.equals(previousTask.getTaskAlias())) {
                    return false; // Consecutive tasks violation
                }
            }
        }

        if (time.isBefore(WORK_END)) {
            Task nextTask = data.getTask(staff, time.plusHours(1));
            if (nextTask != null) {
                return !nextTask.equals(task) && !nextTask.getTaskAlias().equals(task); // Consecutive tasks violation
            }
        }
        return true; // No consecutive tasks violation
    }

    // 3.5.2 Implementation of passPolicy6()
    private boolean passPolicy6(AutoData data, Staff staff) {
        if (!data.policyIds.contains(6L)) {
            return true;// Policy 6 is not enabled, so we can skip the check
        }

        // Policy 6. Staff members working an eight-hour shift must be allocated at least one Optional (unassigned) time slot during the day
        return isPartTime(data, staff) || hasTwoOptionalTimeSlot(data, staff);
    }

    // 3.5.3 Implementation of isPartTime()
    private boolean isPartTime(AutoData data, Staff staff) {
        Task block = taskService.getBlockTask();
        return data.assignments.stream()
            .anyMatch(a -> a.getTask().equals(block) && a.getStaff().equals(staff));
    }

    //3.5.4 Implementation of hasTwoOptionalTimeSlot()
    private boolean hasTwoOptionalTimeSlot(AutoData data, Staff staff) {
        Task optional = taskService.getOptionalTask();
        return data.assignments.stream()
            .filter(sa -> sa.getStaff().equals(staff) && sa.getTask().equals(optional))
            .count() > 1;
    }
}
