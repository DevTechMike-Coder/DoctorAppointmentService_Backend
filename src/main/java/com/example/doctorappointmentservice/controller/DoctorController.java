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

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<List<DoctorDto>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorDto> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<DoctorDto>> searchBySpecialty(@RequestParam String specialty) {
        return ResponseEntity.ok(doctorService.searchBySpecialty(specialty));
    }

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