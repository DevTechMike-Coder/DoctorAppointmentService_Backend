package com.example.doctorappointmentservice.service.meeting;

import com.example.doctorappointmentservice.dto.MeetingJoinResponse;
import com.example.doctorappointmentservice.entity.Appointment;
import com.example.doctorappointmentservice.entity.AppointmentStatus;
import com.example.doctorappointmentservice.exception.MeetingUnavailableException;
import com.example.doctorappointmentservice.exception.ResourceNotFoundException;
import com.example.doctorappointmentservice.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Issues join URLs for an appointment's video call. Only the appointment's own patient or
 * doctor can join, only while it is CONFIRMED, and only inside the join window around the slot.
 */
@Service
@RequiredArgsConstructor
public class MeetingService {

    private final AppointmentRepository appointmentRepository;
    private final DailyVideoClient daily;
    private final MeetingSettings settings;

    @Transactional
    public MeetingJoinResponse join(Long appointmentId, Long userId) {
        Appointment appt = appointmentRepository.findByIdForUpdate(appointmentId)
                .orElseThrow(() -> notFound(appointmentId));

        var doctorUser = appt.getSlot().getDoctorProfile().getUser();
        boolean isDoctor = doctorUser.getId().equals(userId);
        boolean isPatient = appt.getPatient().getId().equals(userId);
        // 404 (not 403) for strangers so appointment ids can't be probed.
        if (!isDoctor && !isPatient) throw notFound(appointmentId);

        if (appt.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new MeetingUnavailableException("The call opens once the doctor confirms the appointment.");
        }

        Instant start = appt.getSlot().getStartTime().atZone(settings.getZone()).toInstant();
        Instant end = appt.getSlot().getEndTime().atZone(settings.getZone()).toInstant();
        Instant now = Instant.now();
        if (now.isBefore(start.minus(settings.getJoinEarly()))) {
            throw new MeetingUnavailableException("The call opens "
                    + settings.getJoinEarly().toMinutes() + " minutes before the appointment.");
        }
        Instant closes = end.plus(settings.getJoinLate());
        if (now.isAfter(closes)) {
            throw new MeetingUnavailableException("This appointment's call window has closed.");
        }

        if (appt.getMeetingRoomName() == null) {
            String name = "appt-" + UUID.randomUUID().toString().replace("-", "");
            DailyVideoClient.Room room = daily.createRoom(name, closes.getEpochSecond());
            appt.setMeetingRoomName(room.name());
            appt.setMeetingRoomUrl(room.url());
            appointmentRepository.save(appt);
        }

        String display = isDoctor ? "Dr. " + stripDr(doctorUser.getFullName()) : appt.getPatient().getFullName();
        String token = daily.createToken(appt.getMeetingRoomName(), display, isDoctor, closes.getEpochSecond());
        return new MeetingJoinResponse(appt.getMeetingRoomUrl() + "?t=" + token);
    }

    private static String stripDr(String name) {
        return name.regionMatches(true, 0, "Dr. ", 0, 4) ? name.substring(4) : name;
    }

    private static ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException("Appointment not found with id: " + id);
    }
}
