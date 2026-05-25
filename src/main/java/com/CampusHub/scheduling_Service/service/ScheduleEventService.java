package com.CampusHub.scheduling_Service.service;

import com.CampusHub.scheduling_Service.client.SalleClient;
import com.CampusHub.scheduling_Service.client.SalleDTO;
import com.CampusHub.scheduling_Service.client.UserClient;
import com.CampusHub.scheduling_Service.client.UserDTO;
import com.CampusHub.scheduling_Service.dto.ScheduleEventDTO;
import com.CampusHub.scheduling_Service.dto.ConflictCheckDTO;
import com.CampusHub.scheduling_Service.entity.ScheduleEvent;
import com.CampusHub.scheduling_Service.repository.ScheduleEventRepository;
import com.CampusHub.scheduling_Service.repository.TeacherAssignmentRepository;
import com.CampusHub.scheduling_Service.repository.SchedulePlanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ScheduleEventService {

    private final ScheduleEventRepository scheduleEventRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final SchedulePlanRepository schedulePlanRepository;
    private final UserClient userClient;
    private final SalleClient salleClient;

    public ScheduleEventService(ScheduleEventRepository scheduleEventRepository, 
                               TeacherAssignmentRepository teacherAssignmentRepository, 
                               SchedulePlanRepository schedulePlanRepository,
                               UserClient userClient, 
                               SalleClient salleClient) {
        this.scheduleEventRepository = scheduleEventRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.schedulePlanRepository = schedulePlanRepository;
        this.userClient = userClient;
        this.salleClient = salleClient;
    }

    public List<ScheduleEventDTO> getFilteredEvents(Long teacherId, Long roomId, UUID planId) {
        List<ScheduleEvent> events;
        
        if (planId != null) {
            events = scheduleEventRepository.findAll().stream()
                    .filter(e -> e.getPlan() != null && e.getPlan().getId().equals(planId))
                    .collect(Collectors.toList());
        } else {
            // Par défaut, on prend tous les événements (ou on pourrait filtrer par le plan actif)
            events = scheduleEventRepository.findAll();
        }
        
        return events.stream()
                .filter(e -> {
                    boolean match = true;
                    if (roomId != null) match = match && roomId.equals(e.getRoomId());
                    if (teacherId != null) match = match && teacherId.equals(e.getAssignmentId());
                    return match;
                })
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduleEventDTO createEvent(ScheduleEventDTO dto) {
        ScheduleEvent event = convertToEntity(dto);
        ScheduleEvent saved = scheduleEventRepository.save(event);
        return convertToDTO(saved);
    }

    @Transactional
    public ScheduleEventDTO updateEvent(UUID id, ScheduleEventDTO dto) {
        ScheduleEvent event = convertToEntity(dto);
        event.setId(id);
        ScheduleEvent saved = scheduleEventRepository.save(event);
        return convertToDTO(saved);
    }

    public ScheduleEvent saveEvent(ScheduleEvent event) {
        return scheduleEventRepository.save(event);
    }

    public ScheduleEvent convertToEntity(ScheduleEventDTO dto) {
        ScheduleEvent e = new ScheduleEvent();
        if (dto.getId() != null && !dto.getId().startsWith("new-")) {
            try {
                e.setId(UUID.fromString(dto.getId()));
            } catch (IllegalArgumentException ex) {
                // Ignore temporary ID, let JPA generate a new one
            }
        }
        e.setTitle(dto.getTitle() != null ? dto.getTitle() : "Sans titre");
        e.setSeriesId(dto.getSeriesId());
        e.setSubjectCode(dto.getSubjectCode());
        e.setType(dto.getType() != null ? dto.getType() : "lecture");
        e.setDayOfWeek(dto.getDay());
        e.setStartTime(dto.getStartTime() != null ? java.time.LocalTime.parse(dto.getStartTime()) : java.time.LocalTime.of(8, 0));
        e.setEndTime(dto.getEndTime() != null ? java.time.LocalTime.parse(dto.getEndTime()) : java.time.LocalTime.of(10, 0));
        e.setRoomId(dto.getRoomId());
        e.setDescription(dto.getDescription());
        e.setGroupName(dto.getGroupName());
        e.setAssignmentId(dto.getTeacherId()); // Autorise le null explicitement
        if (dto.getPlanId() != null) {
            schedulePlanRepository.findById(UUID.fromString(dto.getPlanId())).ifPresent(e::setPlan);
        }
        return e;
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
        dto.setRoomId(e.getRoomId());
        dto.setSubjectCode(e.getSubjectCode());
        dto.setSeriesId(e.getSeriesId());
        dto.setDescription(e.getDescription());
        dto.setGroupName(e.getGroupName());
        if (e.getPlan() != null) {
            dto.setPlanId(e.getPlan().getId().toString());
        }

        // Résolution des noms via Feign et Repository
        try {
            if (e.getAssignmentId() != null) {
                teacherAssignmentRepository.findById(e.getAssignmentId()).ifPresent(assignment -> {
                    dto.setTeacherId(assignment.getId()); // On renvoie l'ID de l'assignation
                    dto.setProfessor(assignment.getTeacherName() != null ? assignment.getTeacherName() : "Enseignant #" + assignment.getTeacherId());
                });
            } else {
                dto.setProfessor("Non assigné");
            }

            if (e.getRoomId() != null) {
                SalleDTO salle = salleClient.getSalleById(e.getRoomId());
                dto.setRoom(salle != null ? salle.getNom() : "Salle Inconnue");
            }
        } catch (Exception ex) {
            log.error("Erreur de résolution : {}", ex.getMessage());
            dto.setProfessor("Erreur Service");
            dto.setRoom("Erreur Service");
        }

        return dto;
    }

    public boolean checkConflicts(ConflictCheckDTO dto) {
        List<ScheduleEvent> existingEvents = scheduleEventRepository.findAll();
        for (ScheduleEvent existing : existingEvents) {
            // Comparaison simple par salle, jour et chevauchement d'heures
            if (existing.getRoomId() != null && existing.getRoomId().toString().equals(dto.getRoom()) && 
                existing.getDayOfWeek() == dto.getDay()) {

                java.time.LocalTime start = java.time.LocalTime.parse(dto.getStartTime());
                java.time.LocalTime end = java.time.LocalTime.parse(dto.getEndTime());

                boolean timeOverlap = start.isBefore(existing.getEndTime()) &&
                                     end.isAfter(existing.getStartTime());
                if (timeOverlap) return true;
            }
        }
        return false;
    }
}
