package com.example.shiftplanner_server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "token", schema = "sp")
@Getter
@Setter
@NoArgsConstructor
public class Token {

    @Id
    @Column(name = "token", nullable = false, length = 255)
    private String token;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;
}

