package com.example.zorvyn.auth.dto;

import com.example.zorvyn.common.model.RoleType;
import com.example.zorvyn.common.model.UserStatus;

public class CurrentUserResponse {
    private Long id;
    private String name;
    private String email;
    private RoleType role;
    private UserStatus status;

    public CurrentUserResponse(Long id, String name, String email, RoleType role, UserStatus status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
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
}
