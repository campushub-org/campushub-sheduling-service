package com.CampusHub.scheduling_Service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "schedule_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = true)
    private String subjectCode;

    private String seriesId; // Nullable for unique events, shared for series

    private Long assignmentId; // FK vers TeacherAssignment

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private String type; // LECTURE, TD, TP, EXAM

    @Column(nullable = false)
    private int dayOfWeek; // 0=Lundi, 4=Vendredi

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;
    
    private String academicYear;
    private int semester;
}
