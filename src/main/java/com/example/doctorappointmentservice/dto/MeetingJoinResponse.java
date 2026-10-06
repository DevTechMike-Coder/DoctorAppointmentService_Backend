package com.example.doctorappointmentservice.dto;

/** Per-user join URL for an appointment's video call (contains a short-lived signed token). */
public record MeetingJoinResponse(String joinUrl) {}
