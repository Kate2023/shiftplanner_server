package com.example.shiftplanner_server.entities;

import jakarta.persistence.*;
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

    public Token(String token, AppUser appUser) {
        this.token = token;
        this.user = appUser;
        this.createdOn = LocalDateTime.now();
    }
}

