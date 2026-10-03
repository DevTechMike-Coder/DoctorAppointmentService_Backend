package com.example.doctorappointmentservice.dto;

/**
 * Response for {@code POST /api/v1/auth/register}. When email verification is
 * required, {@code auth} is null and the client must tell the user to check
 * their inbox; otherwise {@code auth} carries the login token as before.
 */
public record RegisterResponse(
        boolean verificationRequired,
        String message,
        AuthResponse auth
) {}
