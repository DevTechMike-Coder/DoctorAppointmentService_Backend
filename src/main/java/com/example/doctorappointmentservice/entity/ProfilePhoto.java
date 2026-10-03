package com.example.doctorappointmentservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Processed profile image for a user, keyed by the owning user's id. Kept in a
 * separate table from {@link DoctorProfile} so list queries never load image bytes.
 * {@code data} is a bytea column (no {@code @Lob}, which would map to an oid).
 */
@Entity
@Table(name = "profile_photos")
@Getter
@Setter
@NoArgsConstructor
public class ProfilePhoto {

    @Id
    private Long userId;

    @Column(nullable = false, length = 50)
    private String contentType;

    @Column(nullable = false)
    private byte[] data;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public ProfilePhoto(Long userId) {
        this.userId = userId;
    }
}
