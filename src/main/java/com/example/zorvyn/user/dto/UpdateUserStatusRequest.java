package com.example.zorvyn.user.dto;

import com.example.zorvyn.common.model.UserStatus;
import javax.validation.constraints.NotNull;

public class UpdateUserStatusRequest {

    @NotNull
    private UserStatus status;

    public UpdateUserStatusRequest() {
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}


