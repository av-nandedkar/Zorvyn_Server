package com.example.zorvyn.user.dto;

import com.example.zorvyn.common.model.RoleType;
import com.example.zorvyn.common.model.UserStatus;
import java.time.Instant;

public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private RoleType role;
    private UserStatus status;
    private Instant createdAt;

    public UserResponse(Long id, String name, String email, RoleType role, UserStatus status, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public RoleType getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}


