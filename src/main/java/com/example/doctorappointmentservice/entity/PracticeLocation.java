package com.example.doctorappointmentservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A place where a doctor practises (clinic, hospital, private office). A doctor
 * may have several; at most one is flagged primary (enforced by a partial unique
 * index in the database).
 */
@Entity
@Table(name = "practice_locations")
@Getter
@Setter
@NoArgsConstructor
public class PracticeLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false, updatable = false)
    private DoctorProfile doctor;

    @Column(nullable = false, length = 150)
    private String facilityName;

    @Column(nullable = false, length = 200)
    private String addressLine1;

    @Column(length = 200)
    private String addressLine2;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String stateRegion;

    @Column(length = 20)
    private String postalCode;

    /** ISO 3166-1 alpha-2 code, upper-case. */
    @Column(nullable = false, length = 2)
    private String country;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "is_primary", nullable = false)
    private boolean primaryLocation;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
