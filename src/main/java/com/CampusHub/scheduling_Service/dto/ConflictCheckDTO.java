package com.CampusHub.scheduling_Service.dto;
import lombok.Data;

@Data
public class ConflictCheckDTO {
    private String room;
    private String startTime;
    private String endTime;
    private Integer day;
}
