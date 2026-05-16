package com.CampusHub.scheduling_Service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "slot_reservation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Qui demande
    @Column(nullable = false)
    private Long teacherId;

    @Column(nullable = false)
    private String teacherName;

    // Infos du créneau proposé — mêmes champs que ScheduleEvent
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String subjectCode;

    @Column(nullable = false)
    private String type; // LECTURE, TD, TP, EXAM

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private String roomName;

    @Column(nullable = false)
    private int dayOfWeek; // 0=Lundi, 4=Vendredi

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    // Semaine concernée (numéro ISO de la semaine + année)
    @Column(nullable = false)
    private int weekNumber;

    @Column(nullable = false)
    private int year;

    // Niveau du groupe visé (ex: "L1", "L2", "M1" ou subjectCode du niveau)
    @Column(nullable = false)
    private String niveau;

    @Column(nullable = false)
    private String academicYear;

    @Column(nullable = false)
    private int semester;

    // Statut de la réservation
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.APPROVED;
    // APPROVED directement car le teacher soumet et c'est validé
    // si une validation pedagogue est voulue plus tard, changer en PENDING

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ReservationStatus {
        PENDING, APPROVED, REJECTED
    }

}
