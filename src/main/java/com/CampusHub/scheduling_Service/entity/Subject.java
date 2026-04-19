package com.CampusHub.scheduling_Service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subject")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subject {

    @Id
    private String code; // ex: INF111

    @Column(nullable = false)
    private String name;

    private int credits;
    private String category;
    private int niveau;
    private int semester;
    private String specialite;
}
