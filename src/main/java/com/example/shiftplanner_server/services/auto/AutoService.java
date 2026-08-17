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
import java.util.*;

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
        AutoData data = new AutoData(assignments, policyIds, taskService.getDeskTask(), taskService.getCheckinTask());
        tryFirstStages_0_to_3(data);
        tryStage_4(data);
        return assignments;
    }

    // 3.3 Implementation: Stage 0 to 3
    void tryFirstStages_0_to_3(AutoData data) {
        // 3.3.1 Flag to indicate whether we are trying to find the best possible solution

        // 3.3.2 high level Implementation of the Stages 0 to 3
        while (data.stage < data.stages.size()) {
            if (tryStage(data)) {
                // Pass current stage successfully, we can move on to the next stage
                data.stage++;
            } else {
                // Fail, we need to revert the last successful stage and try again
                data.getCurrentStage().reset();
                data.stage--; // move back to the previousStage
                if (data.stage < 0) {
                    // Cannot find a feasible solution, throw an exception
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ERROR_FORMAT_3);
                }
            }
        }
    }

    // 3.3.3 Implementation of tryStage()
    boolean tryStage(AutoData data) {
        Stage stage = data.getCurrentStage();
        while (stage.time.isBefore(WORK_END)) {
            // check if current timeslot has enough the target task(s) already
            long count = data.assignments.stream()
                .filter(a -> a.getTimeSlot().equals(stage.time)
                    && (stage.task.equals(a.getTask()) ||
                    (stage.task.getTaskAlias() != null && stage.task.getTaskAlias().equals(a.getTask()))))
                .count();
            if (count >= stage.numberOfTasks) {
                // Current timeSlot has enough target task(s), move on to the next timeSlot
                stage.time = stage.time.plusHours(1);
                stage.staff = null; // reset the staff member for the next timeSlot
                continue;
            }

            // Find the next suitable staff member for the current timeSlot and task
            Change change = nextChange(data, stage);
            if (change != null) {
                // save and apply the change to the assignments
                data.applyChange(change);
                stage.changes.add(change);
                stage.time = stage.time.plusHours(1);
                stage.staff = null;
                continue;
            }
            // If we reach here, it means we couldn't find a suitable staff member for the current timeSlot and task.
            if (stage.isMandatory) {
                // Mandatory. We must revert or return false if we cannot revert.
                if (stage.changes.isEmpty()) {
                    // Cannot revert
                    return false;
                } else {
                    // Revert the last change
                    Change lastChange = stage.changes.pollLast();
                    data.revertChange(lastChange);
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
        Task optional = taskService.getOptionalTask();
        List<Score> scores = new ArrayList<>();
        for (Staff staff : data.getShuffledStaffs()) {
            if (optional.equals(data.getTask(staff, stage.time))
                && passPolicyCheck(data, stage.time, staff, stage.task)) {
                // Calculate the score for the staff member based on the number of tasks assigned
                scores.add(new Score(staff, calculateScore(data, staff, stage.task)));
            }
        }

        scores.sort(Comparator.comparingLong(Score::value)); // Sort scores in ascending order

        for (int i = 0; i < scores.size(); i++) {
            if (stage.staff == null) {
                // The first time we try this timeSlot
                return new Change(scores.getFirst().staff(), stage.time, stage.task, optional);
            } else if (scores.get(i).staff().equals(stage.staff)) {
                // We have already tried this staff member, so we need to find the next one in the sorted list
                if (i + 1 < scores.size()) {
                    return new Change(scores.get(i + 1).staff(), stage.time, stage.task, optional);
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
        for (LocalTime t : data.getAllTimeSlots()) {
            for (Staff staff : data.getShuffledStaffs()) {
                if (data.getTask(staff, t).equals(optional)) {
                    // Try to assign a task to the Optional time slot
                    Task newTask = findSuitableTask(data, staff, t);
                    if (newTask != null) {
                        // Assign the new task to the Optional time slot
                        Change change = new Change(staff, t, newTask, taskService.getOptionalTask());
                        data.applyChange(change);
                    }
                }
            }
        }
    }

    // 3.4.2 Implementation of findSuitableTask()
    private Task findSuitableTask(AutoData data, Staff staff, LocalTime time) {
        Task optional = taskService.getOptionalTask();
        // Check the previous and next timeSlots to ensure no consecutive tasks are assigned
        Task previousTask = data.getTask(staff, time.minusHours(1));
        Task nextTask = data.getTask(staff, time.plusHours(1));
        List<Task> possibleTasks = new ArrayList<>(List.of(taskService.getPickingTask(),
            taskService.getRoamingTask(),
            taskService.getShelvingTask()));
        Map<Task, Long> taskCounts = new HashMap<>();
        for (Task task : possibleTasks) {
            if (optional.equals(data.getTask(staff, time))
                && passPolicyCheck(data, time, staff, task)) {
                taskCounts.put(task, data.countByTask(task));
            }
        }
        if (taskCounts.isEmpty()) {
            return null;
        }

        //sort score from small to big
        List<Task> sortedTasks = taskCounts.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .toList();

        for (Task task : sortedTasks) {
            if (!task.equals(previousTask) && !task.equals(nextTask)) {
                return task;
            }
        }
        return null;
    }

    // 3.5 Implementation of Policy Check (4 & 6 only)
    private boolean passPolicyCheck(AutoData data, LocalTime time, Staff staff, Task task) {
        return tryPolicy4(data, time, staff, task)
            && tryPolicy6(data, staff);
    }

    // 3.5.1 Implementation of tryPolicy4()
    private boolean tryPolicy4(AutoData data, LocalTime time, Staff staff, Task task) {
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
            return (nextTask != null)
                && (!nextTask.equals(task))
                && (task.getTaskAlias() == null || !task.getTaskAlias().equals(nextTask)); // Consecutive tasks violation
        }
        return true; // No consecutive tasks violation
    }

    // 3.5.2 Implementation of tryPolicy6()
    private boolean tryPolicy6(AutoData data, Staff staff) {
        if (!data.policyIds.contains(6L)) {
            return true;// Policy 6 is not enabled, so we can skip the check
        }

        // Policy 6. Staff members working an eight-hour shift must be allocated at least one Optional (unassigned) time slot during the day
        return isPartTime(data, staff) || hasTwoOptionalTimeSlot(data, staff);
    }

    // 3.5.3 Implementation of isPartTime()
    private boolean isPartTime(AutoData data, Staff staff) {
        Task offsite = taskService.getOffsiteTask();
        return data.assignments.stream()
            .anyMatch(a -> a.getTask().equals(offsite)
                && a.getStaff().equals(staff)
                && a.getTimeSlot().isBefore(WORK_END)); // Check if the staff member has an Off-site task before the last hour of work
    }

    //3.5.4 Implementation of hasTwoOptionalTimeSlot()
    private boolean hasTwoOptionalTimeSlot(AutoData data, Staff staff) {
        Task optional = taskService.getOptionalTask();
        return data.assignments.stream()
            .filter(sa -> sa.getStaff().equals(staff) && sa.getTask().equals(optional))
            .count() > 1;
    }
}
