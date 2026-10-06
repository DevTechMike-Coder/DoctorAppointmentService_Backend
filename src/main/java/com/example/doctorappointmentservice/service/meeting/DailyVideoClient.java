package com.example.doctorappointmentservice.service.meeting;

import com.example.doctorappointmentservice.exception.MeetingUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Thin wrapper over Daily.co's REST API. Rooms are private (a signed meeting token is
 * required to enter), capped at two participants, and expire on their own.
 */
@Component
public class DailyVideoClient {

    private static final Logger log = LoggerFactory.getLogger(DailyVideoClient.class);

    public record Room(String name, String url) {}

    private final MeetingSettings settings;
    private final RestClient http;

    public DailyVideoClient(MeetingSettings settings) {
        this.settings = settings;
        this.http = RestClient.builder()
                .baseUrl(settings.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + settings.getApiKey())
                .build();
    }

    public Room createRoom(String name, long expEpochSeconds) {
        requireConfigured();
        try {
            Map<?, ?> res = http.post().uri("/rooms")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "name", name,
                            "privacy", "private",
                            "properties", Map.of(
                                    "exp", expEpochSeconds,
                                    "max_participants", 2,
                                    "enable_chat", true,
                                    "eject_at_room_exp", true)))
                    .retrieve()
                    .body(Map.class);
            return new Room(name, (String) res.get("url"));
        } catch (RestClientException | NullPointerException | ClassCastException e) {
            log.error("Daily createRoom failed", e);
            throw new MeetingUnavailableException("Video service is unavailable. Try again shortly.", true);
        }
    }

    /** Mints a single-room token. Only the doctor gets owner rights (can mute/eject). */
    public String createToken(String roomName, String userName, boolean owner, long expEpochSeconds) {
        requireConfigured();
        try {
            Map<?, ?> res = http.post().uri("/meeting-tokens")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("properties", Map.of(
                            "room_name", roomName,
                            "user_name", userName,
                            "is_owner", owner,
                            "exp", expEpochSeconds)))
                    .retrieve()
                    .body(Map.class);
            return (String) res.get("token");
        } catch (RestClientException | NullPointerException | ClassCastException e) {
            log.error("Daily createToken failed", e);
            throw new MeetingUnavailableException("Video service is unavailable. Try again shortly.", true);
        }
    }

    private void requireConfigured() {
        if (!settings.isConfigured()) {
            log.error("DAILY_API_KEY is not set; video calls are disabled");
            throw new MeetingUnavailableException("Video calls aren't enabled yet.", true);
        }
    }
}
