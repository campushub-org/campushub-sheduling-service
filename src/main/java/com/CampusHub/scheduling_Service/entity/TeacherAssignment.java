package com.CampusHub.scheduling_Service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "teacher_assignment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subjectCode;

    @Column(nullable = false)
    private Long teacherId;

    @Column(nullable = false)
    private String role; // COURSE_LECTURER, ASSISTANT_LECTURER

    private String teacherName; // Pour dénormaliser un peu et éviter trop d'appels Feign
}
