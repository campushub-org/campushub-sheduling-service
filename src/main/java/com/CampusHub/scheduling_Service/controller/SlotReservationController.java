package com.CampusHub.scheduling_Service.controller;

import com.CampusHub.scheduling_Service.dto.SlotReservationRequestDTO;
import com.CampusHub.scheduling_Service.dto.SlotReservationResponseDTO;
import com.CampusHub.scheduling_Service.entity.SlotReservation;
import com.CampusHub.scheduling_Service.service.SlotReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/scheduling/reservations")
@RequiredArgsConstructor
public class SlotReservationController {

    private final SlotReservationService reservationService;

    @PostMapping
    public ResponseEntity<SlotReservationResponseDTO> requestReservation(
            @RequestBody SlotReservationRequestDTO request) {
        return ResponseEntity.ok(reservationService.processReservation(request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<SlotReservation>> getMyReservations(
            @RequestParam Long teacherId) {
        return ResponseEntity.ok(reservationService.getMyReservations(teacherId));
    }
    @DeleteMapping("/{id}")
public ResponseEntity<Void> cancelReservation(
        @PathVariable Long id,
        @RequestParam Long teacherId) {
    reservationService.cancelReservation(id, teacherId);
    return ResponseEntity.noContent().build();
}
}
