package com.CampusHub.scheduling_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

// Ce que le backend retourne après vérification
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotReservationResponseDTO {

    private boolean success;           // true = réservé, false = conflit détecté
    private String message;            // Message explicatif

    // Si success = true : la réservation créée
    private SlotReservationDTO reservation;

    // Si success = false : liste des créneaux libres de la même semaine pour ce niveau
    private List<FreeSlotDTO> freeSlots;

    // --- Classes internes pour alléger les imports ---

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotReservationDTO {
        private Long id;
        private String title;
        private String subjectCode;
        private String type;
        private Long roomId;
        private String roomName;
        private int dayOfWeek;
        private String startTime;
        private String endTime;
        private int weekNumber;
        private int year;
        private String niveau;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FreeSlotDTO {
        private int dayOfWeek;       // 0=Lundi...4=Vendredi
        private String dayLabel;     // "Lundi", "Mardi"...
        private String startTime;    // "08:00"
        private String endTime;      // "10:00"
        // Le créneau est libre = ni dans ScheduleEvent ni dans SlotReservation
        // pour ce niveau et cette semaine
    }
}