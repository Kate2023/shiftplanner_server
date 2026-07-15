package com.example.shiftplanner_server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tasks", schema = "sp")
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

    @Column(name = "is_auto", nullable = false)
    private boolean auto;

}

