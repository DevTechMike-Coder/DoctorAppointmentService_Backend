package com.example.doctorappointmentservice.service.email;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sends transactional email through Brevo's HTTPS API (port 443). SMTP is avoided
 * on purpose: Railway blocks outbound SMTP on most plans. Sending is async so a
 * slow provider never blocks registration. With no provider configured the
 * verification link is logged instead (local development only).
 */
@Service
public class EmailSender {

    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);
    private static final URI BREVO_SEND_URL = URI.create("https://api.brevo.com/v3/smtp/email");

    private final EmailSettings settings;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ExecutorService executor = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "email-sender");
        t.setDaemon(true);
        return t;
    });

    public EmailSender(EmailSettings settings) {
        this.settings = settings;
    }

    public void sendVerificationEmail(String toEmail, String toName, String verifyLink) {
        if (!settings.isProviderConfigured()) {
            log.warn("[DEV] Email provider not configured. Verification link for {}: {}", toEmail, verifyLink);
            return;
        }

        String html = "<div style=\"font-family:Arial,sans-serif;max-width:480px;margin:0 auto;color:#10161c\">"
                + "<h2 style=\"margin:0 0 12px\">Verify your email</h2>"
                + "<p>Hi " + escapeHtml(toName) + ", confirm this email address to activate your MedBook account.</p>"
                + "<p><a href=\"" + escapeHtml(verifyLink) + "\" style=\"display:inline-block;background:#0f6e5c;"
                + "color:#fff;text-decoration:none;padding:12px 22px;border-radius:10px;font-weight:600\">"
                + "Verify email</a></p>"
                + "<p style=\"font-size:13px;color:#555\">This link expires in 24 hours. If you didn't create an "
                + "account, you can ignore this email.</p></div>";

        String json = "{\"sender\":{\"name\":" + jsonString(settings.getFromName())
                + ",\"email\":" + jsonString(settings.getFromAddress()) + "}"
                + ",\"to\":[{\"email\":" + jsonString(toEmail) + ",\"name\":" + jsonString(toName) + "}]"
                + ",\"subject\":" + jsonString("Verify your MedBook email")
                + ",\"htmlContent\":" + jsonString(html) + "}";

        executor.submit(() -> post(json, toEmail));
    }

    private void post(String json, String toEmail) {
        try {
            HttpRequest request = HttpRequest.newBuilder(BREVO_SEND_URL)
                    .timeout(Duration.ofSeconds(10))
                    .header("api-key", settings.getApiKey())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                String body = response.body() == null ? "" : response.body();
                log.error("Email provider rejected verification email to {} ({}): {}", toEmail,
                        response.statusCode(), body.length() > 300 ? body.substring(0, 300) : body);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            log.error("Failed to send verification email to {}", toEmail, ex);
        }
    }

    @PreDestroy
    void shutdown() {
        executor.shutdown();
    }

    /** Minimal JSON string encoder (avoids depending on a specific Jackson major version). */
    private static String jsonString(String value) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : (value == null ? "" : value).toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.append('"').toString();
    }

    private static String escapeHtml(String value) {
        return (value == null ? "" : value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
