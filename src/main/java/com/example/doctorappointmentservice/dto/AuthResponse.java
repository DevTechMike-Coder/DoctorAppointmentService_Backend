package com.example.doctorappointmentservice.dto;

/**
 * Response returned after a successful login or registration: the signed
 * JWT to use on subsequent requests plus basic identity info for the client.
 */
public record AuthResponse(
        String token,
        Long userId,
        String fullName,
        String role
) {}