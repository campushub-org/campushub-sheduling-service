package com.CampusHub.scheduling_Service.dto;

import lombok.Data;

// Ce que le frontend envoie quand un teacher soumet une demande
@Data
public class SlotReservationRequestDTO {
    private Long teacherId;
    private String teacherName;
    private String title;
    private String subjectCode;
    private String type;        // LECTURE, TD, TP, EXAM
    private Long roomId;
    private String roomName;
    private int dayOfWeek;      // 0=Lundi, 4=Vendredi
    private String startTime;   // "HH:mm"
    private String endTime;     // "HH:mm"
    private int weekNumber;     // Numéro de semaine ISO
    private int year;
    private String niveau;      // Niveau du groupe (ex: "L1", "M2"...)
    private String academicYear;
    private int semester;
}