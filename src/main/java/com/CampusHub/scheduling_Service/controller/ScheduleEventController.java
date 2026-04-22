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
            @RequestParam(required = false) Long roomId) {
        return scheduleEventService.getFilteredEvents(teacherId, roomId);
    }

    @PostMapping("/events")
    public ResponseEntity<ScheduleEvent> createEvent(@RequestBody ScheduleEventDTO eventDTO) {
        ScheduleEvent event = scheduleEventService.convertToEntity(eventDTO);
        return ResponseEntity.ok(scheduleEventService.saveEvent(event));
    }

    @PutMapping("/events/{id}")
    public ScheduleEvent updateEvent(@PathVariable UUID id, @RequestBody ScheduleEvent event) {
        event.setId(id);
        return scheduleEventService.saveEvent(event);
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
