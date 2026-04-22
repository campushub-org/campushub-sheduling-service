package com.CampusHub.scheduling_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEventDTO {
    private String id;
    private String title;
    private String type; // lecture, td, tp, exam, meeting
    private String professor; // Nom complet
    private String room;      // Nom de la salle
    private String startTime; // Format "HH:mm"
    private String endTime;   // Format "HH:mm"
    private int day;          // 0 = Lundi, etc.
    private String description;
    
    // IDs originaux pour permettre la modification
    private Long teacherId;
    private Long groupId;
    private Long roomId;
    private String subjectCode;
    private String seriesId;
    }
