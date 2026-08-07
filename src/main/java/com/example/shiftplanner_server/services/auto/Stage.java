package com.example.shiftplanner_server.services.auto;

import com.example.shiftplanner_server.entities.Staff;
import com.example.shiftplanner_server.entities.Task;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.Deque;

import static com.example.shiftplanner_server.services.ServiceConstant.WORK_START;

@Getter
@Setter
@AllArgsConstructor
public class Stage {
    Long policyId;          // policyId associated with this stage
    Task task;              // task to be assigned in this stage
    int numberOfTasks;      // number of tasks to be assigned in this stage
    boolean isMandatory;    // whether the numberOfTasks is mandatory
    Deque<Change> changes;  // stack to keep track of changes made during the stage
    LocalTime time;   // current timeSlot being processed
    Staff staff;     // current staff member being assigned

    public void reset() {
        changes.clear();
        time = WORK_START;
        staff = null;
    }
}