package com.example.shiftplanner_server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "schedules", schema = "sp")
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

    public Schedule() {
    }

    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Staff getRosterStaff() {
        return rosterStaff;
    }

    public void setRosterStaff(Staff rosterStaff) {
        this.rosterStaff = rosterStaff;
    }

    public Staff getBankingStaff() {
        return bankingStaff;
    }

    public void setBankingStaff(Staff bankingStaff) {
        this.bankingStaff = bankingStaff;
    }

    public Staff getBankingBackupStaff() {
        return bankingBackupStaff;
    }

    public void setBankingBackupStaff(Staff bankingBackupStaff) {
        this.bankingBackupStaff = bankingBackupStaff;
    }

    public Staff getBuildingInspector() {
        return buildingInspector;
    }

    public void setBuildingInspector(Staff buildingInspector) {
        this.buildingInspector = buildingInspector;
    }
}

