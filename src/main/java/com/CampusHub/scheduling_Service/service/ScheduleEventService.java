package com.CampusHub.scheduling_Service.service;

import com.CampusHub.scheduling_Service.entity.ScheduleEvent;
import com.CampusHub.scheduling_Service.repository.ScheduleEventRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ScheduleEventService {

    private final ScheduleEventRepository scheduleEventRepository;

    public ScheduleEventService(ScheduleEventRepository scheduleEventRepository) {
        this.scheduleEventRepository = scheduleEventRepository;
    }

    public List<ScheduleEvent> getAllEvents() {
        return scheduleEventRepository.findAll();
    }

    public List<ScheduleEvent> getFilteredEvents(Long groupId, Long teacherId, Long roomId) {
        List<ScheduleEvent> events = scheduleEventRepository.findAll();
        
        return events.stream().filter(e -> {
            boolean match = true;
            if (groupId != null) match = match && groupId.equals(e.getGroupId());
            if (roomId != null) match = match && roomId.equals(e.getRoomId());
            // Pour le prof, on passera par l'assignmentId plus tard si nécessaire
            return match;
        }).collect(Collectors.toList());
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

    /**
     * Algorithme de détection de conflits
     * Vérifie si l'événement E chevauche un autre événement existant pour la même salle, 
     * le même groupe ou le même professeur.
     */
    public boolean hasConflicts(ScheduleEvent e) {
        List<ScheduleEvent> existingEvents = scheduleEventRepository.findAll();
        
        for (ScheduleEvent existing : existingEvents) {
            // On ignore l'événement lui-même si c'est une mise à jour
            if (existing.getId() != null && existing.getId().equals(e.getId())) continue;

            // Vérification du créneau horaire (Overlap)
            boolean timeOverlap = e.getDayOfWeek() == existing.getDayOfWeek() &&
                                 e.getStartTime().isBefore(existing.getEndTime()) &&
                                 e.getEndTime().isAfter(existing.getStartTime());

            if (timeOverlap) {
                // Conflit de salle
                if (e.getRoomId().equals(existing.getRoomId())) return true;
                
                // Conflit de groupe
                if (e.getGroupId() != null && e.getGroupId().equals(existing.getGroupId())) return true;
                
                // Conflit de professeur (si assignmentId est le même)
                if (e.getAssignmentId() != null && e.getAssignmentId().equals(existing.getAssignmentId())) return true;
            }
        }
        return false;
    }
}
