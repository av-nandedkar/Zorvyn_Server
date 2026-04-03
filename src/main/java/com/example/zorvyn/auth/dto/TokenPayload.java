package com.example.zorvyn.auth.dto;

import com.example.zorvyn.common.model.RoleType;

public class TokenPayload {
    private String sessionId;
    private Long userId;
    private RoleType role;
    private long expiresAtEpoch;
    private String salt;

    public TokenPayload(String sessionId, Long userId, RoleType role, long expiresAtEpoch, String salt) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.role = role;
        this.expiresAtEpoch = expiresAtEpoch;
        this.salt = salt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public RoleType getRole() {
        return role;
    }

    public long getExpiresAtEpoch() {
        return expiresAtEpoch;
    }

    public String getSalt() {
        return salt;
    }
}

