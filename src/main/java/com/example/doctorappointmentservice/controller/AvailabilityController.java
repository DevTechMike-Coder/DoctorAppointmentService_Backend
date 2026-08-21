package com.example.doctorappointmentservice.controller;

import com.example.doctorappointmentservice.dto.AvailabilityDto;
import com.example.doctorappointmentservice.dto.CreateSlotRequest;
import com.example.doctorappointmentservice.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors/{doctorId}/slots")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping
    public ResponseEntity<List<AvailabilityDto>> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(availabilityService.getAvailableSlots(doctorId, from, to));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DOCTOR') and @availabilitySecurity.isOwnerOfDoctorProfile(#doctorId, authentication))")
    public ResponseEntity<AvailabilityDto> createSlot(
            @PathVariable Long doctorId,
            @Valid @RequestBody CreateSlotRequest request
    ) {
        AvailabilityDto created = availabilityService.createSlot(doctorId, request.startTime(), request.endTime());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{slotId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DOCTOR') and @availabilitySecurity.isOwnerOfSlot(#slotId, authentication))")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long doctorId, @PathVariable Long slotId) {
        availabilityService.deleteSlot(slotId);
        return ResponseEntity.noContent().build();
    }
}