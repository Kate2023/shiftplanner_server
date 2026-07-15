package com.example.shiftplanner_server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(
        name = "schedule_assignments",
        schema = "sp",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_assignment_staff_slot", columnNames = {"schedule_id", "staff_id", "time_slot"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ScheduleAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Integer assignmentId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne(optional = false)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(name = "time_slot", nullable = false)
    private LocalTime timeSlot;

    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

}

