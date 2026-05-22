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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j

public class ScheduleEventService {

    private final ScheduleEventRepository scheduleEventRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final UserClient userClient;
    private final SalleClient salleClient;

    public ScheduleEventService(ScheduleEventRepository scheduleEventRepository, TeacherAssignmentRepository teacherAssignmentRepository, UserClient userClient, SalleClient salleClient) {
        this.scheduleEventRepository = scheduleEventRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.userClient = userClient;
        this.salleClient = salleClient;
    }

    public List<ScheduleEventDTO> getFilteredEvents(Long teacherId, Long roomId) {
        List<ScheduleEvent> events = scheduleEventRepository.findAll();
        
        return events.stream()
                .filter(e -> {
                    boolean match = true;
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

public ScheduleEvent convertToEntity(ScheduleEventDTO dto) {
    ScheduleEvent e = new ScheduleEvent();
    if (dto.getId() != null && !dto.getId().startsWith("new-")) {
        try {
            e.setId(UUID.fromString(dto.getId()));
        } catch (IllegalArgumentException ex) { }
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

    // Résoudre l'assignation depuis le teacher_id (ID user)
    if (dto.getTeacherId() != null) {
        teacherAssignmentRepository
            .findFirstByTeacherId(dto.getTeacherId())
            .ifPresentOrElse(
                assignment -> e.setAssignmentId(assignment.getId()),
                () -> e.setAssignmentId(null)
            );
    }
    return e;
}

    public List<ScheduleEvent> saveAll(List<ScheduleEvent> events) {
        return scheduleEventRepository.saveAll(events);
    }
    @org.springframework.transaction.annotation.Transactional
    public List<ScheduleEvent> replaceAll(List<ScheduleEvent> newEvents) {
    scheduleEventRepository.deleteAll();
        return scheduleEventRepository.saveAll(newEvents);
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
    public List<String> validateBatch(List<ScheduleEventDTO> dtos) {
    List<ScheduleEvent> existingEvents = scheduleEventRepository.findAll();
    List<String> conflicts = new ArrayList<>();

    for (int i = 0; i < dtos.size(); i++) {
        ScheduleEventDTO dto = dtos.get(i);
        java.time.LocalTime newStart = java.time.LocalTime.parse(dto.getStartTime());
        java.time.LocalTime newEnd = java.time.LocalTime.parse(dto.getEndTime());

        for (ScheduleEvent existing : existingEvents) {
            boolean sameDay = existing.getDayOfWeek() == dto.getDay();
            if (!sameDay) continue;

            boolean timeOverlap = newStart.isBefore(existing.getEndTime()) &&
                                  newEnd.isAfter(existing.getStartTime());
            if (!timeOverlap) continue;

            // Conflit de salle
            if (dto.getRoomId() != null && dto.getRoomId().equals(existing.getRoomId())) {
                conflicts.add(String.format(
                    "Conflit de salle — \"%s\" : la salle ID %d est déjà occupée le %s de %s à %s.",
                    dto.getTitle(), dto.getRoomId(), dayLabel(dto.getDay()),
                    dto.getStartTime(), dto.getEndTime()
                ));
            }

            // Conflit d'enseignant
            if (dto.getTeacherId() != null && existing.getAssignmentId() != null) {
                teacherAssignmentRepository.findFirstByTeacherId(dto.getTeacherId())
                    .ifPresent(assignment -> {
                        if (assignment.getId().equals(existing.getAssignmentId())) {
                            conflicts.add(String.format(
                                "Conflit d'enseignant — \"%s\" : l'enseignant est déjà assigné à un autre cours le %s de %s à %s.",
                                dto.getTitle(), dayLabel(dto.getDay()),
                                dto.getStartTime(), dto.getEndTime()
                            ));
                        }
                    });
            }
        }

        // Conflit entre les nouveaux créneaux eux-mêmes
        for (int j = i + 1; j < dtos.size(); j++) {
            ScheduleEventDTO other = dtos.get(j);
            if (other.getDay() != dto.getDay()) continue;

            java.time.LocalTime otherStart = java.time.LocalTime.parse(other.getStartTime());
            java.time.LocalTime otherEnd = java.time.LocalTime.parse(other.getEndTime());
            boolean overlap = newStart.isBefore(otherEnd) && newEnd.isAfter(otherStart);
            if (!overlap) continue;

            if (dto.getRoomId() != null && dto.getRoomId().equals(other.getRoomId())) {
                conflicts.add(String.format(
                    "Conflit interne — \"%s\" et \"%s\" partagent la même salle ID %d le %s de %s à %s.",
                    dto.getTitle(), other.getTitle(), dto.getRoomId(),
                    dayLabel(dto.getDay()), dto.getStartTime(), dto.getEndTime()
                ));
            }

            if (dto.getTeacherId() != null && dto.getTeacherId().equals(other.getTeacherId())) {
                conflicts.add(String.format(
                    "Conflit interne — \"%s\" et \"%s\" assignent le même enseignant le %s de %s à %s.",
                    dto.getTitle(), other.getTitle(),
                    dayLabel(dto.getDay()), dto.getStartTime(), dto.getEndTime()
                ));
            }
        }
    }
    return conflicts;
}

private String dayLabel(int day) {
    String[] labels = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
    return (day >= 0 && day < labels.length) ? labels[day] : "Jour " + day;
}
}
