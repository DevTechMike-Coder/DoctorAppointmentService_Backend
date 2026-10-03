package com.example.doctorappointmentservice.service.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Email-related configuration, bound from {@code app.email.*} / {@code app.frontend-url}
 * (see application.yml for the env vars that feed these).
 */
@Component
public class EmailSettings {

    /** When true, accounts must verify their email before they can sign in. */
    @Value("${app.email.verification-required:false}")
    private boolean verificationRequired;

    /** Brevo API key. Blank = no provider configured (links are logged instead of emailed). */
    @Value("${app.email.api-key:}")
    private String apiKey;

    /** Verified sender address registered with the email provider. */
    @Value("${app.email.from-address:}")
    private String fromAddress;

    @Value("${app.email.from-name:MedBook}")
    private String fromName;

    /** Public frontend origin used to build the verification link. */
    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public boolean isVerificationRequired() {
        return verificationRequired;
    }

    public String getApiKey() {
        return apiKey == null ? "" : apiKey.trim();
    }

    public String getFromAddress() {
        return fromAddress == null ? "" : fromAddress.trim();
    }

    public String getFromName() {
        return fromName;
    }

    public String getFrontendUrl() {
        String url = frontendUrl == null ? "" : frontendUrl.trim();
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public boolean isProviderConfigured() {
        return !getApiKey().isEmpty() && !getFromAddress().isEmpty();
    }
}
