package com.example.tricol.tricolspringbootrestapi.service.impl;

import com.example.tricol.tricolspringbootrestapi.dto.request.LoginRequest;
import com.example.tricol.tricolspringbootrestapi.dto.response.KeycloakAuthResponse;
import com.example.tricol.tricolspringbootrestapi.dto.response.KeycloakTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "keycloak.enabled", havingValue = "true", matchIfMissing = false)
public class KeycloakAuthService {

    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public KeycloakAuthResponse authenticate(LoginRequest request) {
        String tokenUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("username", request.getUsername());
        params.add("password", request.getPassword());
        params.add("scope", "openid email profile");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<KeycloakTokenResponse> response = restTemplate.postForEntity(
                    tokenUrl, entity, KeycloakTokenResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return mapToAuthResponse(response.getBody(), request.getUsername());
            }
            throw new RuntimeException("Failed to authenticate with Keycloak");
        } catch (Exception e) {
            log.error("Keycloak authentication failed: {}", e.getMessage());
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    public KeycloakAuthResponse refreshToken(String refreshToken) {
        String tokenUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("refresh_token", refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<KeycloakTokenResponse> response = restTemplate.postForEntity(
                    tokenUrl, entity, KeycloakTokenResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return mapToAuthResponse(response.getBody(), null);
            }
            throw new RuntimeException("Failed to refresh token");
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new RuntimeException("Token refresh failed: " + e.getMessage());
        }
    }

    private KeycloakAuthResponse mapToAuthResponse(KeycloakTokenResponse tokenResponse, String username) {
        String email = null;
        
        // Decode JWT to extract email
        if (tokenResponse.getAccessToken() != null) {
            try {
                String[] parts = tokenResponse.getAccessToken().split("\\.");
                if (parts.length > 1) {
                    String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                    // Simple JSON parsing to extract email
                    if (payload.contains("\"email\"")) {
                        int emailStart = payload.indexOf("\"email\":\"") + 9;
                        int emailEnd = payload.indexOf("\"", emailStart);
                        if (emailEnd > emailStart) {
                            email = payload.substring(emailStart, emailEnd);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to extract email from token: {}", e.getMessage());
            }
        }
        
        return KeycloakAuthResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .tokenType(tokenResponse.getTokenType())
                .expiresIn(tokenResponse.getExpiresIn())
                .username(username)
                .email(email)
                .build();
    }
}
