package com.CampusHub.scheduling_Service.service;

import com.CampusHub.scheduling_Service.dto.SlotReservationRequestDTO;
import com.CampusHub.scheduling_Service.dto.SlotReservationResponseDTO;
import com.CampusHub.scheduling_Service.dto.SlotReservationResponseDTO.FreeSlotDTO;
import com.CampusHub.scheduling_Service.dto.SlotReservationResponseDTO.SlotReservationDTO;
import com.CampusHub.scheduling_Service.entity.ScheduleEvent;
import com.CampusHub.scheduling_Service.entity.SlotReservation;
import com.CampusHub.scheduling_Service.repository.ScheduleEventRepository;
import com.CampusHub.scheduling_Service.repository.SlotReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlotReservationService {

    private final SlotReservationRepository reservationRepository;
    private final ScheduleEventRepository scheduleEventRepository;

    // Créneaux horaires standards de l'établissement
    private static final String[][] STANDARD_SLOTS = {
        {"08:00", "10:00"},
        {"10:15", "12:15"},
        {"12:30", "14:30"},
        {"14:45", "16:45"},
        {"17:00", "19:00"}
    };
private static final String[] DAY_LABELS = {
    "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"
};

    public SlotReservationResponseDTO processReservation(SlotReservationRequestDTO req) {

        LocalTime startTime = LocalTime.parse(req.getStartTime());
        LocalTime endTime   = LocalTime.parse(req.getEndTime());

        // ================================================================
        // NIVEAU 1 : Vérification contre l'emploi du temps officiel (ScheduleEvent)
        // On vérifie salle ET niveau (via subjectCode lié au niveau)
        // ================================================================
        List<ScheduleEvent> allEvents = scheduleEventRepository.findAll();
        for (ScheduleEvent ev : allEvents) {

            boolean memeJour = ev.getDayOfWeek() == req.getDayOfWeek();
            boolean chevauchement = startTime.isBefore(ev.getEndTime())
                                 && endTime.isAfter(ev.getStartTime());

            if (!memeJour || !chevauchement) continue;

            // Conflit de salle dans le planning officiel
            if (ev.getRoomId() != null && ev.getRoomId().equals(req.getRoomId())) {
                log.warn("Conflit planning officiel (salle) détecté pour roomId={}", req.getRoomId());
                return buildConflictResponse(req,
                    "Ce créneau est déjà occupé dans l'emploi du temps officiel (salle " 
                    + req.getRoomName() + " occupée).");
            }

            // Conflit de niveau dans le planning officiel (même subjectCode = même groupe/niveau)
            if (ev.getSubjectCode() != null && req.getSubjectCode() != null
                    && ev.getSubjectCode().equals(req.getSubjectCode())) {
                log.warn("Conflit planning officiel (niveau) détecté pour subjectCode={}", req.getSubjectCode());
                return buildConflictResponse(req,
                    "Ce créneau est déjà occupé dans l'emploi du temps officiel pour ce niveau.");
            }
        }

        // ================================================================
        // NIVEAU 2 : Vérification contre les réservations déjà soumises
        // ================================================================
        List<SlotReservation> roomConflicts = reservationRepository.findRoomConflicts(
            req.getRoomId(), req.getDayOfWeek(),
            req.getWeekNumber(), req.getYear(),
            startTime, endTime
        );
        if (!roomConflicts.isEmpty()) {
            SlotReservation conflicting = roomConflicts.get(0);
            return buildConflictResponse(req,
                "La salle " + req.getRoomName() + " est déjà réservée par "
                + conflicting.getTeacherName() + " sur ce créneau.");
        }

        List<SlotReservation> niveauConflicts = reservationRepository.findNiveauConflicts(
            req.getNiveau(), req.getDayOfWeek(),
            req.getWeekNumber(), req.getYear(),
            startTime, endTime
        );
        if (!niveauConflicts.isEmpty()) {
            SlotReservation conflicting = niveauConflicts.get(0);
            return buildConflictResponse(req,
                "Le niveau " + req.getNiveau() + " a déjà un cours réservé sur ce créneau"
                + " par " + conflicting.getTeacherName() + ".");
        }

        // ================================================================
        // Aucun conflit → on enregistre la réservation
        // ================================================================
        SlotReservation reservation = new SlotReservation();
        reservation.setTeacherId(req.getTeacherId());
        reservation.setTeacherName(req.getTeacherName());
        reservation.setTitle(req.getTitle());
        reservation.setSubjectCode(req.getSubjectCode());
        reservation.setType(req.getType());
        reservation.setRoomId(req.getRoomId());
        reservation.setRoomName(req.getRoomName());
        reservation.setDayOfWeek(req.getDayOfWeek());
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setWeekNumber(req.getWeekNumber());
        reservation.setYear(req.getYear());
        reservation.setNiveau(req.getNiveau());
        reservation.setAcademicYear(req.getAcademicYear());
        reservation.setSemester(req.getSemester());
        reservation.setStatus(SlotReservation.ReservationStatus.APPROVED);

        SlotReservation saved = reservationRepository.save(reservation);

        SlotReservationDTO dto = toDTO(saved);
        return new SlotReservationResponseDTO(true,
            "Créneau réservé avec succès !", dto, null);
    }

    // Récupérer toutes les réservations d'un teacher
    public List<SlotReservation> getMyReservations(Long teacherId) {
        return reservationRepository.findByTeacherId(teacherId);
    }

    // ================================================================
    // Calcul des créneaux libres de la semaine pour un niveau donné
    // Combine planning officiel + réservations existantes
    // ================================================================
    private List<FreeSlotDTO> computeFreeSlots(SlotReservationRequestDTO req) {
        List<FreeSlotDTO> freeSlots = new ArrayList<>();
        List<ScheduleEvent> allEvents = scheduleEventRepository.findAll();
        List<SlotReservation> existingReservations = reservationRepository
            .findByWeekNumberAndYearAndNiveau(req.getWeekNumber(), req.getYear(), req.getNiveau());

        for (int day = 0; day <= 6; day++) {
            for (String[] slot : STANDARD_SLOTS) {
                LocalTime slotStart = LocalTime.parse(slot[0]);
                LocalTime slotEnd   = LocalTime.parse(slot[1]);
                final int currentDay = day;

                // Vérifie contre le planning officiel
                boolean occupiedInPlanning = allEvents.stream().anyMatch(ev ->
                    ev.getDayOfWeek() == currentDay
                    && slotStart.isBefore(ev.getEndTime())
                    && slotEnd.isAfter(ev.getStartTime())
                    && (
                        (ev.getRoomId() != null && ev.getRoomId().equals(req.getRoomId()))
                        || (ev.getSubjectCode() != null && ev.getSubjectCode().equals(req.getSubjectCode()))
                    )
                );

                // Vérifie contre les réservations existantes pour ce niveau
                boolean occupiedInReservations = existingReservations.stream().anyMatch(r ->
                    r.getDayOfWeek() == currentDay
                    && slotStart.isBefore(r.getEndTime())
                    && slotEnd.isAfter(r.getStartTime())
                    && r.getStatus() != SlotReservation.ReservationStatus.REJECTED
                );

                if (!occupiedInPlanning && !occupiedInReservations) {
                    freeSlots.add(new FreeSlotDTO(
                        currentDay,
                        DAY_LABELS[currentDay],
                        slot[0],
                        slot[1]
                    ));
                }
            }
        }
        return freeSlots;
    }

    private SlotReservationResponseDTO buildConflictResponse(
            SlotReservationRequestDTO req, String message) {
        List<FreeSlotDTO> freeSlots = computeFreeSlots(req);
        String fullMessage = message + " Voici les créneaux libres de la semaine "
            + req.getWeekNumber() + " pour le niveau " + req.getNiveau() + " :";
        return new SlotReservationResponseDTO(false, fullMessage, null, freeSlots);
    }

    private SlotReservationDTO toDTO(SlotReservation r) {
        return new SlotReservationDTO(
            r.getId(), r.getTitle(), r.getSubjectCode(), r.getType(),
            r.getRoomId(), r.getRoomName(), r.getDayOfWeek(),
            r.getStartTime().toString(), r.getEndTime().toString(),
            r.getWeekNumber(), r.getYear(), r.getNiveau(), r.getStatus().name()
        );
    }
public void cancelReservation(Long id, Long teacherId) {
    SlotReservation reservation = reservationRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Réservation introuvable"));
    if (!reservation.getTeacherId().equals(teacherId)) {
        throw new RuntimeException("Action non autorisée");
    }
    reservationRepository.delete(reservation);
}
}
