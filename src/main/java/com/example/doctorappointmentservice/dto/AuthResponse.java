package com.example.doctorappointmentservice.dto;

public record AuthResponse(
        String token,
        Long userId,
        String fullName,
        String role
) {}