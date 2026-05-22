package com.CampusHub.scheduling_Service.service;

import com.CampusHub.scheduling_Service.entity.SchedulePlan;
import com.CampusHub.scheduling_Service.repository.SchedulePlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchedulePlanService {

    private final SchedulePlanRepository planRepository;
    private final ScheduleEventService eventService;

    public List<SchedulePlan> getAllPlans() {
        return planRepository.findAll();
    }
    
    @Transactional
    public SchedulePlan importPlan(com.CampusHub.scheduling_Service.dto.PlanImportExportDTO dto) {
        SchedulePlan plan = new SchedulePlan();
        plan.setName(dto.getName());
        plan.setAcademicYear(dto.getAcademicYear());
        plan.setSemester(dto.getSemester());
        plan.setLevel(dto.getLevel());
        plan.setStatus(SchedulePlan.PlanStatus.DRAFT);
        
        final SchedulePlan savedPlan = planRepository.save(plan);
        
        if (dto.getEvents() != null) {
            List<com.CampusHub.scheduling_Service.entity.ScheduleEvent> events = dto.getEvents().stream()
                .map(eventDto -> {
                    com.CampusHub.scheduling_Service.entity.ScheduleEvent event = eventService.convertToEntity(eventDto);
                    event.setPlan(savedPlan);
                    return event;
                })
                .collect(java.util.stream.Collectors.toList());
            eventService.saveAll(events);
        }
        
        return savedPlan;
    }

    public com.CampusHub.scheduling_Service.dto.PlanImportExportDTO exportPlan(UUID id) {
        SchedulePlan plan = getPlanById(id);
        com.CampusHub.scheduling_Service.dto.PlanImportExportDTO dto = new com.CampusHub.scheduling_Service.dto.PlanImportExportDTO();
        dto.setName(plan.getName());
        dto.setAcademicYear(plan.getAcademicYear());
        dto.setSemester(plan.getSemester());
        dto.setLevel(plan.getLevel());
        
        // Note: conversion back to DTO is needed here if events are lazily loaded
        // This is simplified for the example
        return dto;
    }

    public SchedulePlan getPlanById(UUID id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan non trouvé"));
    }

    @Transactional
    public SchedulePlan createPlan(SchedulePlan plan) {
        if (plan.isDefault()) {
            resetCurrentDefaults(plan.getLevel());
        }
        return planRepository.save(plan);
    }

    @Transactional
    public SchedulePlan activatePlan(UUID id) {
        SchedulePlan plan = getPlanById(id);
        
        // On archive les anciens plans actifs du même niveau/semestre
        List<SchedulePlan> activePlans = planRepository.findByStatus(SchedulePlan.PlanStatus.ACTIVE);
        activePlans.stream()
                .filter(p -> p.getLevel().equals(plan.getLevel()) && p.getSemester() == plan.getSemester())
                .forEach(p -> p.setStatus(SchedulePlan.PlanStatus.ARCHIVED));
        
        plan.setStatus(SchedulePlan.PlanStatus.ACTIVE);
        return planRepository.save(plan);
    }

    private void resetCurrentDefaults(String level) {
        planRepository.findByLevelAndStatusAndIsDefaultTrue(level, SchedulePlan.PlanStatus.ACTIVE)
                .ifPresent(p -> {
                    p.setDefault(false);
                    planRepository.save(p);
                });
    }

    @Transactional
    public void deletePlan(UUID id) {
        planRepository.deleteById(id);
    }
}
