package com.example.zorvyn.auth.dto;

import java.time.Instant;

public class AuthToken {
    private String rawToken;
    private String tokenHash;
    private String sessionId;
    private String salt;
    private Instant expiresAt;

    public AuthToken(String rawToken, String tokenHash, String sessionId, String salt, Instant expiresAt) {
        this.rawToken = rawToken;
        this.tokenHash = tokenHash;
        this.sessionId = sessionId;
        this.salt = salt;
        this.expiresAt = expiresAt;
    }

    public String getRawToken() {
        return rawToken;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getSalt() {
        return salt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}


