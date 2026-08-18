package com.example.shiftplanner_server.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Integer taskId;

    @Column(name = "task_name", nullable = false, unique = true, length = 255)
    private String taskName;

    @Column(name = "colour", nullable = false, length = 7)
    private String colour;

    @Column(name = "is_lunch", nullable = false)
    private boolean lunch;

    @ManyToOne()
    @JoinColumn(name = "task_alias")
    private Task taskAlias;

    @Column(name = "is_auto", nullable = false)
    private boolean auto;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return taskId.equals(task.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }
}

