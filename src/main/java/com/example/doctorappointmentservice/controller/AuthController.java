package com.example.doctorappointmentservice.controller;

import com.example.doctorappointmentservice.dto.AuthResponse;
import com.example.doctorappointmentservice.dto.LoginRequest;
import com.example.doctorappointmentservice.dto.RegisterRequest;
import com.example.doctorappointmentservice.service.AuthService;
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

    /** {@code POST /api/v1/auth/register} — creates a new account and returns a login token. */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** {@code POST /api/v1/auth/login} — authenticates credentials and returns a login token. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}