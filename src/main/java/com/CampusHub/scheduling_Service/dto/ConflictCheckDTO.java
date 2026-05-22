package com.CampusHub.scheduling_Service.dto;
import lombok.Data;

@Data
public class ConflictCheckDTO {
    private String room;        // ID de la salle
    private String startTime;
    private String endTime;
    private Integer day;
    private Long teacherId;     // ← nouveau
    private String title;       // ← nouveau (pour message d'erreur explicite)
}