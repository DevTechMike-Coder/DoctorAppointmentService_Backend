package com.example.doctorappointmentservice.controller;

import com.example.doctorappointmentservice.dto.AppointmentDto;
import com.example.doctorappointmentservice.dto.BookAppointmentRequest;
import com.example.doctorappointmentservice.dto.MeetingJoinResponse;
import com.example.doctorappointmentservice.dto.UpdateAppointmentStatusRequest;
import com.example.doctorappointmentservice.security.CustomUserDetails;
import com.example.doctorappointmentservice.service.AppointmentService;
import com.example.doctorappointmentservice.service.meeting.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for booking and managing appointments. Access to individual
 * appointments is restricted to admins or the patient/doctor who owns them,
 * enforced via {@code @PreAuthorize} and {@code AppointmentSecurity}.
 */
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final MeetingService meetingService;

    /**
     * {@code POST /api/v1/appointments} — books an appointment for the
     * currently authenticated patient. Patient-only.
     */
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentDto> bookAppointment(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody BookAppointmentRequest request
    ) {
        AppointmentDto booked = appointmentService.bookAppointment(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(booked);
    }

    /**
     * {@code GET /api/v1/appointments/{id}} — fetches one appointment.
     * Allowed for admins, or the patient/doctor who owns the appointment.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @appointmentSecurity.isOwner(#id, authentication)")
    public ResponseEntity<AppointmentDto> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    /**
     * {@code GET /api/v1/appointments/me} — lists the current patient's own
     * appointments. Patient-only.
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentDto>> getMyAppointments(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForPatient(principal.getId()));
    }

    /**
     * {@code GET /api/v1/appointments/doctor/{doctorId}} — lists all
     * appointments for a given doctor. Doctor/Admin only.
     */
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DOCTOR') and @availabilitySecurity.isOwnerOfDoctorProfile(#doctorId, authentication))")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsForDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForDoctor(doctorId));
    }

    /**
     * {@code PATCH /api/v1/appointments/{id}/status} — changes an
     * appointment's status (e.g. confirm/cancel/complete). Doctor/Admin only.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DOCTOR') and @appointmentSecurity.isDoctorOfAppointment(#id, authentication))")
    public ResponseEntity<AppointmentDto> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentStatusRequest request
    ) {
        AppointmentDto updated = appointmentService.updateStatus(id, request.status());
        return ResponseEntity.ok(updated);
    }

    /**
     * {@code DELETE /api/v1/appointments/{id}} — cancels an appointment and
     * frees its slot. Allowed for admins, or the patient/doctor who owns it.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @appointmentSecurity.isOwner(#id, authentication)")
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@code POST /api/v1/appointments/{id}/meeting} — returns a join URL for the video call.
     * Ownership, status and time-window checks happen in {@link MeetingService}.
     */
    @PostMapping("/{id}/meeting")
    public ResponseEntity<MeetingJoinResponse> joinMeeting(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return ResponseEntity.ok(meetingService.join(id, principal.getId()));
    }
}
