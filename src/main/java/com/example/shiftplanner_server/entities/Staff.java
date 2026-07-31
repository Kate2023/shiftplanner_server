package com.example.shiftplanner_server.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Staff staff = (Staff) o;
        return staffId.equals(staff.staffId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(staffId);
    }
}

