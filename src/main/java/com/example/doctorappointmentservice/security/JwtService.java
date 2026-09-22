package com.example.doctorappointmentservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * Issues and validates HMAC-signed JWTs used as bearer tokens for API
 * authentication. Tokens embed the user's email (subject), id, and role.
 */
@Component
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    /** Derives the HMAC signing key from the configured {@code app.jwt.secret}. */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a signed JWT for a freshly authenticated user.
     *
     * @param email the user's email, stored as the token subject
     * @param userId the user's database id, stored as a custom claim
     * @param role the user's role name, stored as a custom claim
     * @return a compact, signed JWT string with the configured expiration
     */
    public String generateToken(String email, Long userId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /** Reads the token's subject (the user's email). */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Reads the "userId" custom claim, or null if absent. */
    public Long extractUserId(String token) {
        Number userId = extractAllClaims(token).get("userId", Number.class);
        return userId != null ? userId.longValue() : null;
    }

    /** Reads the "role" custom claim. */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Verifies the token's signature is valid, its subject matches the
     * given user details, and it has not expired.
     *
     * @throws io.jsonwebtoken.JwtException if the token is malformed or has an invalid signature
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /** True if the token's expiration timestamp is in the past. */
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /** Parses and verifies the token, then applies the given claim extractor. */
    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    /** Parses the token, verifying its signature, and returns all claims. */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}