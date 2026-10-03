package com.example.doctorappointmentservice.service;

import com.example.doctorappointmentservice.entity.EmailVerificationToken;
import com.example.doctorappointmentservice.entity.User;
import com.example.doctorappointmentservice.repository.EmailVerificationTokenRepository;
import com.example.doctorappointmentservice.repository.UserRepository;
import com.example.doctorappointmentservice.service.email.EmailSender;
import com.example.doctorappointmentservice.service.email.EmailSettings;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Issues, emails and redeems single-use email-verification tokens. Tokens are
 * 256-bit random values; only their SHA-256 hash is stored, they expire after
 * 24 hours, and issuing a new one invalidates any earlier ones.
 */
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final Duration TOKEN_TTL = Duration.ofHours(24);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final String INVALID_LINK = "This verification link is invalid, expired, or already used.";

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailSender emailSender;
    private final EmailSettings settings;
    private final SecureRandom random = new SecureRandom();

    @PostConstruct
    void warnOnMisconfiguration() {
        if (settings.isVerificationRequired() && !settings.isProviderConfigured()) {
            log.warn("app.email.verification-required is true but BREVO_API_KEY / MAIL_FROM_ADDRESS are not set: "
                    + "verification links will only be logged and users cannot complete verification.");
        }
    }

    /** Creates a fresh token for the user (invalidating older ones) and emails the verification link. */
    @Transactional
    public void issueAndSend(User user) {
        tokenRepository.deleteAllByUserId(user.getId());

        byte[] raw = new byte[32];
        random.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        tokenRepository.save(new EmailVerificationToken(
                user, sha256Hex(token), LocalDateTime.now().plus(TOKEN_TTL)));

        String link = settings.getFrontendUrl() + "/verify-email?token=" + token;
        emailSender.sendVerificationEmail(user.getEmail(), user.getFullName(), link);
    }

    /**
     * Re-sends the verification email if the account exists and is unverified.
     * Silent otherwise (and when inside the cooldown), so the endpoint can't be
     * used to discover which emails are registered.
     */
    @Transactional
    public void resend(String normalizedEmail) {
        userRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(user -> !user.isEmailVerified())
                .ifPresent(user -> {
                    Optional<EmailVerificationToken> latest =
                            tokenRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId());
                    boolean coolingDown = latest.isPresent()
                            && latest.get().getCreatedAt().isAfter(LocalDateTime.now().minus(RESEND_COOLDOWN));
                    if (!coolingDown) {
                        issueAndSend(user);
                    }
                });
    }

    /**
     * Redeems a token and marks the account's email as verified.
     *
     * @throws IllegalArgumentException if the token is unknown, expired or already used
     */
    @Transactional
    public void verify(String token) {
        EmailVerificationToken stored = tokenRepository.findByTokenHash(sha256Hex(token.trim()))
                .orElseThrow(() -> new IllegalArgumentException(INVALID_LINK));

        LocalDateTime now = LocalDateTime.now();
        if (stored.getUsedAt() != null || stored.getExpiresAt().isBefore(now)) {
            throw new IllegalArgumentException(INVALID_LINK);
        }

        User user = stored.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        stored.setUsedAt(now);
        tokenRepository.save(stored);
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
