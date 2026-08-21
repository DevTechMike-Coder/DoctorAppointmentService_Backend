package com.example.doctorappointmentservice.controller;

import com.example.doctorappointmentservice.dto.AppointmentDto;
import com.example.doctorappointmentservice.dto.BookAppointmentRequest;
import com.example.doctorappointmentservice.dto.UpdateAppointmentStatusRequest;
import com.example.doctorappointmentservice.security.CustomUserDetails;
import com.example.doctorappointmentservice.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentDto> bookAppointment(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody BookAppointmentRequest request
    ) {
        AppointmentDto booked = appointmentService.bookAppointment(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(booked);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @appointmentSecurity.isOwner(#id, authentication)")
    public ResponseEntity<AppointmentDto> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentDto>> getMyAppointments(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForPatient(principal.getId()));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsForDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForDoctor(doctorId));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<AppointmentDto> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentStatusRequest request
    ) {
        AppointmentDto updated = appointmentService.updateStatus(id, request.status());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @appointmentSecurity.isOwner(#id, authentication)")
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.noContent().build();
    }
}