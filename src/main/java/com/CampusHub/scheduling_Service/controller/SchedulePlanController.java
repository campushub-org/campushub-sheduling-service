package com.CampusHub.scheduling_Service.controller;

import com.CampusHub.scheduling_Service.entity.SchedulePlan;
import com.CampusHub.scheduling_Service.service.SchedulePlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/scheduling/plans")
@RequiredArgsConstructor
public class SchedulePlanController {

    private final SchedulePlanService planService;

    @GetMapping
    public List<SchedulePlan> getAllPlans() {
        return planService.getAllPlans();
    }

    @GetMapping("/{id}")
    public SchedulePlan getPlanById(@PathVariable UUID id) {
        return planService.getPlanById(id);
    }

    @PostMapping
    public SchedulePlan createPlan(@RequestBody SchedulePlan plan) {
        return planService.createPlan(plan);
    }

    @PostMapping("/{id}/activate")
    public SchedulePlan activatePlan(@PathVariable UUID id) {
        return planService.activatePlan(id);
    }

    @PostMapping("/import")
    public SchedulePlan importPlan(@RequestBody com.CampusHub.scheduling_Service.dto.PlanImportExportDTO dto) {
        return planService.importPlan(dto);
    }

    @GetMapping("/{id}/export")
    public com.CampusHub.scheduling_Service.dto.PlanImportExportDTO exportPlan(@PathVariable UUID id) {
        return planService.exportPlan(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable UUID id) {
        planService.deletePlan(id);
        return ResponseEntity.ok().build();
    }
}
