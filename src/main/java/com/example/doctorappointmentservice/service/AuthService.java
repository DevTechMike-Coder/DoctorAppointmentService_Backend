package com.example.doctorappointmentservice.service;

import com.example.doctorappointmentservice.dto.AuthResponse;
import com.example.doctorappointmentservice.dto.LoginRequest;
import com.example.doctorappointmentservice.dto.RegisterRequest;
import com.example.doctorappointmentservice.dto.RegisterResponse;
import com.example.doctorappointmentservice.entity.Role;
import com.example.doctorappointmentservice.entity.User;
import com.example.doctorappointmentservice.exception.EmailNotVerifiedException;
import com.example.doctorappointmentservice.exception.InvalidCredentialsException;
import com.example.doctorappointmentservice.repository.UserRepository;
import com.example.doctorappointmentservice.security.JwtService;
import com.example.doctorappointmentservice.service.email.EmailDomainValidator;
import com.example.doctorappointmentservice.service.email.EmailSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles account registration and login: creates {@link User} records
 * with hashed passwords and issues JWTs via {@link JwtService}.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailDomainValidator emailDomainValidator;
    private final EmailVerificationService emailVerificationService;
    private final EmailSettings emailSettings;

    /**
     * Creates a new account, hashing the given password and validating the
     * requested role, then immediately issues a login token for it.
     *
     * @param request new-account details (name, email, password, role)
     * @return either a login token (verification off) or a prompt to verify the email (verification on)
     * @throws IllegalArgumentException if the email is already registered or the role is invalid
     */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        emailDomainValidator.assertDeliverable(email);

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        Role role;
        try {
            role = Role.valueOf(request.role().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role: " + request.role());
        }

        // Privilege-escalation guard: public signup may only create PATIENT or DOCTOR accounts.
        if (role == Role.ADMIN) {
            throw new IllegalArgumentException("Invalid role: " + request.role());
        }

        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(role)
                .build();

        user = userRepository.save(user);

        if (emailSettings.isVerificationRequired()) {
            emailVerificationService.issueAndSend(user);
            return new RegisterResponse(true,
                    "Account created. Check your inbox for a verification link to activate it.", null);
        }

        String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole().name());
        return new RegisterResponse(false, "Account created.",
                new AuthResponse(token, user.getId(), user.getFullName(), user.getRole().name()));
    }

    /**
     * Authenticates a user by email/password and issues a fresh JWT.
     *
     * @param request the login credentials
     * @return a token plus basic identity info for the account
     * @throws com.example.doctorappointmentservice.exception.InvalidCredentialsException if the email doesn't exist or the password doesn't match
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Checked after the password so only the real owner learns the account exists but is unverified.
        if (emailSettings.isVerificationRequired() && !user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Please verify your email address before signing in.");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole().name());

        return new AuthResponse(token, user.getId(), user.getFullName(), user.getRole().name());
    }

    /** Emails are compared case-insensitively; store them trimmed and lowercased. */
    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }
}