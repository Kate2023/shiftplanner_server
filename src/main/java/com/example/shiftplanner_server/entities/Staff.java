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

import java.math.BigDecimal;

@Entity
@Table(name = "staff", schema = "sp")
@Getter
@Setter
@NoArgsConstructor
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Integer staffId;

    @Column(name = "staff_name", nullable = false, length = 255)
    private String staffName;

    @Column(name = "working_hours", nullable = false, precision = 5, scale = 2)
    private BigDecimal workingHours;

    @Column(name = "lunch_break", nullable = false, precision = 5, scale = 2)
    private BigDecimal lunchBreak;

    @Column(name = "is_active", nullable = false)
    private boolean active;

}

