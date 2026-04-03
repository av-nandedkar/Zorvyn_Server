package com.example.zorvyn.user.dto;

import com.example.zorvyn.common.model.RoleType;
import javax.validation.constraints.NotNull;

public class UpdateUserRoleRequest {

    @NotNull
    private RoleType role;

    public UpdateUserRoleRequest() {
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }
}


