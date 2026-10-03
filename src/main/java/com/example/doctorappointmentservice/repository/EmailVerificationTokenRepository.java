package com.example.doctorappointmentservice.repository;

import com.example.doctorappointmentservice.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link EmailVerificationToken} entities.
 */
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    Optional<EmailVerificationToken> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    /** Invalidates all outstanding tokens for a user (called before issuing a fresh one). */
    @Modifying
    @Query("delete from EmailVerificationToken t where t.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
