package com.example.zorvyn.auth.dto;

import java.time.Instant;

public class LoginResponse {
    private String token;
    private Instant expiresAt;
    private CurrentUserResponse user;

    public LoginResponse(String token, Instant expiresAt, CurrentUserResponse user) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public CurrentUserResponse getUser() {
        return user;
    }
}
