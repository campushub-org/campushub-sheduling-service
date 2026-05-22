package com.CampusHub.scheduling_Service.dto;

import com.CampusHub.scheduling_Service.entity.SchedulePlan;
import lombok.Data;
import java.util.List;

@Data
public class PlanImportExportDTO {
    private String name;
    private String academicYear;
    private int semester;
    private String level;
    private List<ScheduleEventDTO> events;
}
