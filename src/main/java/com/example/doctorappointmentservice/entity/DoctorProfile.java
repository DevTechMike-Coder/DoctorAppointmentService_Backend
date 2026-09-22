package com.example.doctorappointmentservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Doctor-specific profile data attached one-to-one to a {@link User} whose
 * role is DOCTOR. Holds the information patients see when browsing or
 * searching for doctors (specialization, bio, consultation fee, etc.).
 */
@Entity
@Table(name = "doctor_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String specialization;

    private String qualifications;

    private String bio;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal consultationFee;
}
