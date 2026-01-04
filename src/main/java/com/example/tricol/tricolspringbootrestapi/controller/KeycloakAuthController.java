package com.example.tricol.tricolspringbootrestapi.controller;

import com.example.tricol.tricolspringbootrestapi.dto.request.LoginRequest;
import com.example.tricol.tricolspringbootrestapi.dto.request.RefreshTokenRequest;
import com.example.tricol.tricolspringbootrestapi.dto.response.KeycloakAuthResponse;
import com.example.tricol.tricolspringbootrestapi.service.impl.KeycloakAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/keycloak/auth")
@RequiredArgsConstructor
@Tag(name = "Keycloak Authentication", description = "Keycloak OAuth2 authentication endpoints")
@ConditionalOnProperty(name = "keycloak.enabled", havingValue = "true", matchIfMissing = false)
public class KeycloakAuthController {

    private final KeycloakAuthService keycloakAuthService;

    @PostMapping("/login")
    @Operation(summary = "Login with Keycloak", description = "Authenticate user via Keycloak and get JWT tokens")
    public ResponseEntity<KeycloakAuthResponse> loginWithKeycloak(@Valid @RequestBody LoginRequest request) {
        KeycloakAuthResponse response = keycloakAuthService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Keycloak token", description = "Refresh access token using refresh token")
    public ResponseEntity<KeycloakAuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        KeycloakAuthResponse response = keycloakAuthService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
}
