package com.example.shiftplanner_server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "schedules", schema = "sp")
@Getter
@Setter
@NoArgsConstructor
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Integer scheduleId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "notes")
    private String notes;

    @ManyToOne
    @JoinColumn(name = "roster_staff")
    private Staff rosterStaff;

    @ManyToOne
    @JoinColumn(name = "banking_staff")
    private Staff bankingStaff;

    @ManyToOne
    @JoinColumn(name = "banking_backup_staff")
    private Staff bankingBackupStaff;

    @ManyToOne
    @JoinColumn(name = "building_inspector")
    private Staff buildingInspector;

    @Column(name = "policies", nullable = false)
    private String policies = "";

}
