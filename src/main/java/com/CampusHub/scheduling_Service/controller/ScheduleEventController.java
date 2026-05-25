package com.CampusHub.scheduling_Service.controller;

import com.CampusHub.scheduling_Service.dto.ScheduleEventDTO;
import com.CampusHub.scheduling_Service.dto.ConflictCheckDTO;
import com.CampusHub.scheduling_Service.entity.ScheduleEvent;
import com.CampusHub.scheduling_Service.service.ScheduleEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/scheduling")
public class ScheduleEventController {

    private final ScheduleEventService scheduleEventService;

    public ScheduleEventController(ScheduleEventService scheduleEventService) {
        this.scheduleEventService = scheduleEventService;
    }

    @GetMapping("/events")
    public List<ScheduleEventDTO> getEvents(
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) UUID planId) {
        return scheduleEventService.getFilteredEvents(teacherId, roomId, planId);
    }

    @PostMapping("/events")
    public ResponseEntity<ScheduleEventDTO> createEvent(@RequestBody ScheduleEventDTO eventDTO) {
        return ResponseEntity.ok(scheduleEventService.createEvent(eventDTO));
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<ScheduleEventDTO> updateEvent(@PathVariable UUID id, @RequestBody ScheduleEventDTO eventDTO) {
        return ResponseEntity.ok(scheduleEventService.updateEvent(id, eventDTO));
    }

    @DeleteMapping("/events/{id}")
    public void deleteEvent(@PathVariable UUID id) {
        scheduleEventService.deleteEvent(id);
    }

    @PostMapping("/batch-save")
    public ResponseEntity<List<ScheduleEvent>> batchSave(@RequestBody List<ScheduleEventDTO> eventDTOs) {
        List<ScheduleEvent> events = eventDTOs.stream()
                .map(scheduleEventService::convertToEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(scheduleEventService.saveAll(events));
    }

    @PostMapping("/check-conflicts")
    public ResponseEntity<Boolean> checkConflicts(@RequestBody ConflictCheckDTO dto) {
        // Logique simplifiée de vérification : on compare les champs du DTO avec les événements existants
        return ResponseEntity.ok(scheduleEventService.checkConflicts(dto));
    }
}
