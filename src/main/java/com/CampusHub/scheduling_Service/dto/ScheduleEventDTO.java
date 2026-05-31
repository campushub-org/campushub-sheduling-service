package com.CampusHub.scheduling_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEventDTO {
    private String id;
    private String title;
    private String type; // lecture, td, tp, exam, meeting
    private String professor; // Noms complets joints (ex: "Achille, Naomi")
    private String room;      // Nom de la salle
    private String startTime; // Format "HH:mm"
    private String endTime;   // Format "HH:mm"
    private int day;          // 0 = Lundi, etc.
    private String description;
    private String groupName;
    
    // IDs originaux pour permettre la modification
    private List<Long> teacherIds;
    private String subjectCode;

    private Long roomId;
    private String seriesId;
    private String planId; // UUID as String
}
