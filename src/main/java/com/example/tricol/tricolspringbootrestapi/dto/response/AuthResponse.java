package com.example.tricol.tricolspringbootrestapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private Long id;
    private String accessToken;
    private String refreshToken;
    private String username;
    private String role;
    private Set<String> permissions;
}
