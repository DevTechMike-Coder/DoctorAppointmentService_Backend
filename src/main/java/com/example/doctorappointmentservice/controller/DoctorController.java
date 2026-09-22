package com.example.doctorappointmentservice.controller;

import com.example.doctorappointmentservice.dto.DoctorDto;
import com.example.doctorappointmentservice.security.CustomUserDetails;
import com.example.doctorappointmentservice.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for browsing doctor profiles and letting a doctor manage
 * their own profile. Browsing is public; updating a profile is doctor-only.
 */
@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    /** {@code GET /api/v1/doctors} — lists all doctors. Public. */
    @GetMapping
    public ResponseEntity<List<DoctorDto>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    /** {@code GET /api/v1/doctors/{id}} — fetches one doctor's profile. Public. */
    @GetMapping("/{id}")
    public ResponseEntity<DoctorDto> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    /** {@code GET /api/v1/doctors/search?specialty=...} — searches doctors by specialization. Public. */
    @GetMapping("/search")
    public ResponseEntity<List<DoctorDto>> searchBySpecialty(@RequestParam String specialty) {
        return ResponseEntity.ok(doctorService.searchBySpecialty(specialty));
    }

    /**
     * {@code PUT /api/v1/doctors/profile} — creates or updates the
     * authenticated doctor's own profile. Doctor-only.
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorDto> createOrUpdateProfile(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody DoctorDto request
    ) {
        DoctorDto updated = doctorService.createOrUpdateProfile(principal.getId(), request);
        return ResponseEntity.ok(updated);
    }
}