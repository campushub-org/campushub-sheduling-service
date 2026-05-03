package com.CampusHub.scheduling_Service.repository;

import com.CampusHub.scheduling_Service.entity.SlotReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface SlotReservationRepository extends JpaRepository<SlotReservation, Long> {

    // Toutes les réservations d'un teacher
    List<SlotReservation> findByTeacherId(Long teacherId);

    // Réservations pour une semaine et un niveau donnés (pour calculer les créneaux libres)
    List<SlotReservation> findByWeekNumberAndYearAndNiveau(
        int weekNumber, int year, String niveau
    );

    // Réservations pour une semaine et une salle donnée
    List<SlotReservation> findByWeekNumberAndYearAndRoomId(
        int weekNumber, int year, Long roomId
    );

    // Requête centrale : conflit par SALLE (même salle, même jour, même semaine, chevauchement horaire)
    @Query("""
        SELECT r FROM SlotReservation r
        WHERE r.roomId = :roomId
          AND r.dayOfWeek = :dayOfWeek
          AND r.weekNumber = :weekNumber
          AND r.year = :year
          AND r.status <> 'REJECTED'
          AND r.startTime < :endTime
          AND r.endTime > :startTime
    """)
    List<SlotReservation> findRoomConflicts(
        @Param("roomId") Long roomId,
        @Param("dayOfWeek") int dayOfWeek,
        @Param("weekNumber") int weekNumber,
        @Param("year") int year,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );

    // Requête centrale : conflit par NIVEAU (même niveau, même jour, même semaine, chevauchement)
    @Query("""
        SELECT r FROM SlotReservation r
        WHERE r.niveau = :niveau
          AND r.dayOfWeek = :dayOfWeek
          AND r.weekNumber = :weekNumber
          AND r.year = :year
          AND r.status <> 'REJECTED'
          AND r.startTime < :endTime
          AND r.endTime > :startTime
    """)
    List<SlotReservation> findNiveauConflicts(
        @Param("niveau") String niveau,
        @Param("dayOfWeek") int dayOfWeek,
        @Param("weekNumber") int weekNumber,
        @Param("year") int year,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );
}