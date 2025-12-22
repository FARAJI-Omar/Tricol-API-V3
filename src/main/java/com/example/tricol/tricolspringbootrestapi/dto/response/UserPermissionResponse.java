package com.example.tricol.tricolspringbootrestapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPermissionResponse {
    private Long id;
    private Long userId;
    private String username;
    private Long permissionId;
    private String permissionName;
    private boolean active;
    private Long grantedBy;
    private LocalDateTime grantedAt;
    private LocalDateTime revokedAt;
}

