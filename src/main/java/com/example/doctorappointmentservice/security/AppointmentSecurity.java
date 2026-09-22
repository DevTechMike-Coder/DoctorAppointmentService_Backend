package com.example.doctorappointmentservice.security;

import com.example.doctorappointmentservice.entity.Appointment;
import com.example.doctorappointmentservice.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Authorization helper used from {@code @PreAuthorize} SpEL expressions to
 * decide whether the current user is allowed to view/modify a given
 * appointment (either the patient who booked it or the doctor it belongs to).
 */
@Component("appointmentSecurity")
@RequiredArgsConstructor
public class AppointmentSecurity {

    private final AppointmentRepository appointmentRepository;

    /**
     * Checks whether the authenticated user is either the patient or the
     * doctor associated with the given appointment.
     *
     * @param appointmentId the appointment being accessed
     * @param authentication the current request's authentication, expected
     *                       to carry a {@link CustomUserDetails} principal
     * @return true if the user owns the appointment (as patient or doctor); false otherwise, including when the appointment doesn't exist
     */
    public boolean isOwner(Long appointmentId, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            return false;
        }

        Long currentUserId = principal.getId();

        return appointmentRepository.findById(appointmentId)
                .map(appt -> isPatientOwner(appt, currentUserId) || isDoctorOwner(appt, currentUserId))
                .orElse(false);
    }

    /** True if the given user is the patient who booked this appointment. */
    private boolean isPatientOwner(Appointment appt, Long userId) {
        return appt.getPatient().getId().equals(userId);
    }

    /** True if the given user is the doctor whose slot this appointment occupies. */
    private boolean isDoctorOwner(Appointment appt, Long userId) {
        return appt.getSlot().getDoctorProfile().getUser().getId().equals(userId);
    }
}