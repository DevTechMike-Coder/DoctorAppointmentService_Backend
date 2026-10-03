package com.example.doctorappointmentservice.controller;

import com.example.doctorappointmentservice.dto.DoctorDto;
import com.example.doctorappointmentservice.entity.ProfilePhoto;
import com.example.doctorappointmentservice.security.CustomUserDetails;
import com.example.doctorappointmentservice.service.DoctorPhotoService;
import com.example.doctorappointmentservice.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
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
    private final DoctorPhotoService doctorPhotoService;

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

    /**
     * {@code POST /api/v1/doctors/profile/photo} — uploads/replaces the authenticated doctor's own
     * photo (multipart field {@code file}, max 2 MB, JPEG or PNG). Re-encoded server-side. Doctor-only.
     */
    @PostMapping(path = "/profile/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorDto> uploadPhoto(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(doctorPhotoService.upload(principal.getId(), file));
    }

    /** {@code DELETE /api/v1/doctors/profile/photo} — removes the authenticated doctor's photo. Doctor-only. */
    @DeleteMapping("/profile/photo")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorDto> deletePhoto(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(doctorPhotoService.remove(principal.getId()));
    }

    /**
     * {@code GET /api/v1/doctors/{id}/photo} — serves a doctor's photo. Public (an {@code <img>} tag
     * can't send a bearer token). URLs carry a {@code ?v=} version, so responses are cached as immutable.
     */
    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long id) {
        ProfilePhoto photo = doctorPhotoService.getByDoctorId(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.getContentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .header("Content-Security-Policy", "default-src 'none'; sandbox")
                .header("X-Content-Type-Options", "nosniff")
                .body(photo.getData());
    }
}