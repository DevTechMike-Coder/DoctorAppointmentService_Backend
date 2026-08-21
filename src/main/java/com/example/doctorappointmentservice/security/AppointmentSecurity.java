package com.example.doctorappointmentservice.security;

import com.example.doctorappointmentservice.entity.Appointment;
import com.example.doctorappointmentservice.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("appointmentSecurity")
@RequiredArgsConstructor
public class AppointmentSecurity {

    private final AppointmentRepository appointmentRepository;

    public boolean isOwner(Long appointmentId, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            return false;
        }

        Long currentUserId = principal.getId();

        return appointmentRepository.findById(appointmentId)
                .map(appt -> isPatientOwner(appt, currentUserId) || isDoctorOwner(appt, currentUserId))
                .orElse(false);
    }

    private boolean isPatientOwner(Appointment appt, Long userId) {
        return appt.getPatient().getId().equals(userId);
    }

    private boolean isDoctorOwner(Appointment appt, Long userId) {
        return appt.getSlot().getDoctorProfile().getUser().getId().equals(userId);
    }
}