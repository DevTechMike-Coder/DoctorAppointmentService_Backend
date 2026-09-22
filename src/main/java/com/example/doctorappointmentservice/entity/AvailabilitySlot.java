package com.example.doctorappointmentservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * A single bookable time window offered by a doctor. Slots are created by
 * doctors/admins and booked by patients via {@code AppointmentService}.
 * The {@code version} field enables optimistic locking so two patients
 * cannot race to book the same slot.
 */
@Entity
@Table(name = "availability_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilitySlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorProfile doctorProfile;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    @Builder.Default
    private boolean isBooked = false;

    @Version
    private Long version;
}