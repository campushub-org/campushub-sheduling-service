package com.CampusHub.scheduling_Service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "schedule_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private PlanStatus status; // DRAFT, ACTIVE, ARCHIVED, TEMPLATE

    private boolean isDefault;

    private String academicYear;

    private int semester;

    private String level; // L1, L2, L3, M1, M2

    private String createdBy;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleEvent> events;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum PlanStatus {
        DRAFT, ACTIVE, ARCHIVED, TEMPLATE
    }
}
