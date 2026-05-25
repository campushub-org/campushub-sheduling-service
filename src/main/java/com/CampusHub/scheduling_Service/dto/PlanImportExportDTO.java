package com.CampusHub.scheduling_Service.dto;

import com.CampusHub.scheduling_Service.entity.SchedulePlan;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class PlanImportExportDTO {
    private String name;
    private String academicYear;
    private int semester;
    private String level;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<ScheduleEventDTO> events;
}
