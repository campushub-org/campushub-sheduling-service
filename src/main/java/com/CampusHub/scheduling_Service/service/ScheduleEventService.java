package com.CampusHub.scheduling_Service.service;

import com.CampusHub.scheduling_Service.client.SalleClient;
import com.CampusHub.scheduling_Service.client.SalleDTO;
import com.CampusHub.scheduling_Service.client.UserClient;
import com.CampusHub.scheduling_Service.client.UserDTO;
import com.CampusHub.scheduling_Service.dto.ScheduleEventDTO;
import com.CampusHub.scheduling_Service.entity.ScheduleEvent;
import com.CampusHub.scheduling_Service.repository.ScheduleEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ScheduleEventService {

    private final ScheduleEventRepository scheduleEventRepository;
    private final UserClient userClient;
    private final SalleClient salleClient;

    public ScheduleEventService(ScheduleEventRepository scheduleEventRepository,UserClient userClient,SalleClient salleClient) {
        this.scheduleEventRepository = scheduleEventRepository;
        this.userClient = userClient;
        this.salleClient = salleClient;
    }

    public List<ScheduleEventDTO> getFilteredEvents(Long groupId, Long teacherId, Long roomId) {
        List<ScheduleEvent> events = scheduleEventRepository.findAll();
        
        return events.stream()
                .filter(e -> {
                    boolean match = true;
                    if (groupId != null) match = match && groupId.equals(e.getGroupId());
                    if (roomId != null) match = match && roomId.equals(e.getRoomId());
                    // ... filter by teacherId if needed ...
                    return match;
                })
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ScheduleEvent saveEvent(ScheduleEvent event) {
        return scheduleEventRepository.save(event);
    }

    public List<ScheduleEvent> saveAll(List<ScheduleEvent> events) {
        return scheduleEventRepository.saveAll(events);
    }

    public void deleteEvent(UUID id) {
        scheduleEventRepository.deleteById(id);
    }

    private ScheduleEventDTO convertToDTO(ScheduleEvent e) {
        ScheduleEventDTO dto = new ScheduleEventDTO();
        dto.setId(e.getId().toString());
        dto.setTitle(e.getTitle());
        dto.setType(e.getType().toLowerCase());
        dto.setDay(e.getDayOfWeek());
        dto.setStartTime(e.getStartTime().toString());
        dto.setEndTime(e.getEndTime().toString());
        dto.setGroupId(e.getGroupId());
        dto.setRoomId(e.getRoomId());
        dto.setSubjectCode(e.getSubjectCode());

        // Résolution des noms via Feign
        try {
            if (e.getAssignmentId() != null) {
                // Pour simplifier ici, on pourrait aussi stocker le teacherId directement dans ScheduleEvent
                // Si TeacherAssignment est utilisé, on récupère d'abord l'assignment
                dto.setProfessor("Enseignant #" + e.getAssignmentId()); 
            } else {
                dto.setProfessor("Non assigné");
            }

            if (e.getRoomId() != null) {
                SalleDTO salle = salleClient.getSalleById(e.getRoomId());
                dto.setRoom(salle != null ? salle.getNom() : "Salle Inconnue");
            }
        } catch (Exception ex) {
            log.error("Erreur de résolution Feign : {}", ex.getMessage());
            dto.setProfessor("Erreur Service");
            dto.setRoom("Erreur Service");
        }

        return dto;
    }

    public boolean hasConflicts(ScheduleEvent e) {
        List<ScheduleEvent> existingEvents = scheduleEventRepository.findAll();
        for (ScheduleEvent existing : existingEvents) {
            if (existing.getId() != null && existing.getId().equals(e.getId())) continue;
            boolean timeOverlap = e.getDayOfWeek() == existing.getDayOfWeek() &&
                                 e.getStartTime().isBefore(existing.getEndTime()) &&
                                 e.getEndTime().isAfter(existing.getStartTime());
            if (timeOverlap) {
                if (e.getRoomId().equals(existing.getRoomId())) return true;
                if (e.getGroupId() != null && e.getGroupId().equals(existing.getGroupId())) return true;
            }
        }
        return false;
    }
}
