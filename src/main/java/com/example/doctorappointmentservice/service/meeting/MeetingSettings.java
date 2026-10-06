package com.example.doctorappointmentservice.service.meeting;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZoneId;

/** Video-call configuration, bound from {@code app.meeting.*} (see application.yml). */
@Component
public class MeetingSettings {

    @Value("${app.meeting.daily-api-key:}")
    private String apiKey;

    @Getter
    @Value("${app.meeting.daily-base-url:https://api.daily.co/v1}")
    private String baseUrl;

    @Value("${app.meeting.zone:Africa/Lagos}")
    private String zone;

    @Value("${app.meeting.join-early-minutes:10}")
    private long joinEarlyMinutes;

    @Value("${app.meeting.join-late-minutes:15}")
    private long joinLateMinutes;

    public String getApiKey() { return apiKey == null ? "" : apiKey.trim(); }

    public ZoneId getZone() { return ZoneId.of(zone); }
    public Duration getJoinEarly() { return Duration.ofMinutes(joinEarlyMinutes); }
    public Duration getJoinLate() { return Duration.ofMinutes(joinLateMinutes); }
    public boolean isConfigured() { return !getApiKey().isEmpty(); }
}
