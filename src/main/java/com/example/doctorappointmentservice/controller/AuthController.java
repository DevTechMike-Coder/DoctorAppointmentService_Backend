package com.example.doctorappointmentservice.controller;

import com.example.doctorappointmentservice.dto.AuthResponse;
import com.example.doctorappointmentservice.dto.LoginRequest;
import com.example.doctorappointmentservice.dto.RegisterRequest;
import com.example.doctorappointmentservice.dto.RegisterResponse;
import com.example.doctorappointmentservice.dto.ResendVerificationRequest;
import com.example.doctorappointmentservice.dto.VerifyEmailRequest;
import com.example.doctorappointmentservice.service.AuthService;
import com.example.doctorappointmentservice.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public REST endpoints for account registration and login. No
 * authentication required; both endpoints issue a JWT on success.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    /** {@code POST /api/v1/auth/register} — creates a new account and returns a login token. */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** {@code POST /api/v1/auth/login} — authenticates credentials and returns a login token. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /** {@code POST /api/v1/auth/verify-email} — redeems the token from the emailed link. POST (not GET) so link scanners can't burn the token. */
    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        emailVerificationService.verify(request.token());
        return ResponseEntity.noContent().build();
    }

    /** {@code POST /api/v1/auth/resend-verification} — always 202, whether or not the email is registered. */
    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        emailVerificationService.resend(AuthService.normalizeEmail(request.email()));
        return ResponseEntity.accepted().build();
    }
}