package com.example.doctorappointmentservice.controller;

import com.example.doctorappointmentservice.dto.PracticeLocationDto;
import com.example.doctorappointmentservice.dto.PracticeLocationRequest;
import com.example.doctorappointmentservice.security.CustomUserDetails;
import com.example.doctorappointmentservice.service.PracticeLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Workplace locations for doctors. Reading a doctor's locations is public; managing them is
 * doctor-only and always scoped to the authenticated doctor (no doctor id is accepted from the client).
 * Route-level access is already open for {@code /api/v1/doctors/**} in SecurityConfig, so the
 * write endpoints rely on {@code @PreAuthorize}.
 */
@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorLocationController {

    private final PracticeLocationService locationService;

    /** {@code GET /api/v1/doctors/{id}/locations} — a doctor's workplaces (id = doctor profile id). Public. */
    @GetMapping("/{id}/locations")
    public ResponseEntity<List<PracticeLocationDto>> getDoctorLocations(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.listForDoctor(id));
    }

    /** {@code GET /api/v1/doctors/profile/locations} — the authenticated doctor's own locations. Doctor-only. */
    @GetMapping("/profile/locations")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<PracticeLocationDto>> getMyLocations(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(locationService.listMine(principal.getId()));
    }

    /** {@code POST /api/v1/doctors/profile/locations} — adds a workplace. Doctor-only. */
    @PostMapping("/profile/locations")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PracticeLocationDto> addLocation(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody PracticeLocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.create(principal.getId(), request));
    }

    /** {@code PUT /api/v1/doctors/profile/locations/{locationId}} — updates one of the doctor's workplaces. Doctor-only. */
    @PutMapping("/profile/locations/{locationId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PracticeLocationDto> updateLocation(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long locationId,
            @Valid @RequestBody PracticeLocationRequest request) {
        return ResponseEntity.ok(locationService.update(principal.getId(), locationId, request));
    }

    /** {@code DELETE /api/v1/doctors/profile/locations/{locationId}} — removes one of the doctor's workplaces. Doctor-only. */
    @DeleteMapping("/profile/locations/{locationId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteLocation(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long locationId) {
        locationService.delete(principal.getId(), locationId);
        return ResponseEntity.noContent().build();
    }
}
