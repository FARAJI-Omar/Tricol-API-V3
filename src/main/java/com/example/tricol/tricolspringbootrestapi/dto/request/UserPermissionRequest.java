package com.example.tricol.tricolspringbootrestapi.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserPermissionRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Permission ID is required")
    private Long permissionId;
}

