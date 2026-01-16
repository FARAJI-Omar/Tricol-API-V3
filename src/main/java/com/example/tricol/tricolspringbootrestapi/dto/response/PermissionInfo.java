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
public class PermissionInfo {
    private Long id;
    private String name;
    private boolean active;
    private LocalDateTime grantedAt;
}
