package com.example.doctorappointmentservice.security;

import com.example.doctorappointmentservice.entity.AvailabilitySlot;
import com.example.doctorappointmentservice.entity.DoctorProfile;
import com.example.doctorappointmentservice.repository.AvailabilitySlotRepository;
import com.example.doctorappointmentservice.repository.DoctorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("availabilitySecurity")
@RequiredArgsConstructor
public class AvailabilitySecurity {

    private final DoctorProfileRepository doctorProfileRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;

    /**
     * True if the authenticated user is the doctor identified by doctorId
     * (i.e. their DoctorProfile.id matches the path variable).
     */
    public boolean isOwnerOfDoctorProfile(Long doctorId, Authentication authentication) {
        Long currentUserId = extractUserId(authentication);
        if (currentUserId == null) {
            return false;
        }

        return doctorProfileRepository.findById(doctorId)
                .map(DoctorProfile::getUser)
                .map(user -> user.getId().equals(currentUserId))
                .orElse(false);
    }

    /**
     * True if the authenticated user owns the DoctorProfile that the given slot belongs to.
     */
    public boolean isOwnerOfSlot(Long slotId, Authentication authentication) {
        Long currentUserId = extractUserId(authentication);
        if (currentUserId == null) {
            return false;
        }

        return availabilitySlotRepository.findById(slotId)
                .map(AvailabilitySlot::getDoctorProfile)
                .map(DoctorProfile::getUser)
                .map(user -> user.getId().equals(currentUserId))
                .orElse(false);
    }

    private Long extractUserId(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            return null;
        }
        return principal.getId();
    }
}