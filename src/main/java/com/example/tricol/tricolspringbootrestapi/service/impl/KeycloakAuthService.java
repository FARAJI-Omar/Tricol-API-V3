package com.example.tricol.tricolspringbootrestapi.service.impl;

import com.example.tricol.tricolspringbootrestapi.dto.request.LoginRequest;
import com.example.tricol.tricolspringbootrestapi.dto.response.KeycloakAuthResponse;
import com.example.tricol.tricolspringbootrestapi.dto.response.KeycloakTokenResponse;
import com.example.tricol.tricolspringbootrestapi.model.UserApp;
import com.example.tricol.tricolspringbootrestapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
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
    private final UserRepository userRepository;

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
            log.error("Keycloak authentication failed with status: {}", response.getStatusCode());
            throw new BadCredentialsException("Invalid username or password");
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            log.error("Keycloak authentication failed for user: {}. HTTP Status: {}, Error Response: {}",
                    request.getUsername(), e.getStatusCode(), errorBody);

            throw new BadCredentialsException("Invalid username or password");
        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Keycloak authentication failed for user: {}. Error: {}", request.getUsername(), e.getMessage(), e);
            throw new BadCredentialsException("Invalid username or password");
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
            log.error("Token refresh failed with status: {}", response.getStatusCode());
            throw new BadCredentialsException("Invalid or expired refresh token");
        } catch (HttpClientErrorException e) {
            log.error("Token refresh failed. HTTP Status: {}, Error: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BadCredentialsException("Invalid or expired refresh token");
        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage(), e);
            throw new BadCredentialsException("Invalid or expired refresh token");
        }
    }

    private KeycloakAuthResponse mapToAuthResponse(KeycloakTokenResponse tokenResponse, String username) {
        String email = null;
        String role = null;
        Set<String> permissions = new HashSet<>();

        // Decode JWT to extract email, roles and permissions
        if (tokenResponse.getAccessToken() != null) {
            try {
                String[] parts = tokenResponse.getAccessToken().split("\\.");
                if (parts.length > 1) {
                    String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));

                    // Extract email
                    if (payload.contains("\"email\"")) {
                        int emailStart = payload.indexOf("\"email\":\"") + 9;
                        int emailEnd = payload.indexOf("\"", emailStart);
                        if (emailEnd > emailStart) {
                            email = payload.substring(emailStart, emailEnd);
                        }
                    }

                    // Extract realm roles from JWT
                    if (payload.contains("\"realm_access\"")) {
                        int realmAccessStart = payload.indexOf("\"realm_access\"");
                        int rolesStart = payload.indexOf("\"roles\"", realmAccessStart);
                        if (rolesStart > 0) {
                            int rolesArrayStart = payload.indexOf("[", rolesStart);
                            int rolesArrayEnd = payload.indexOf("]", rolesArrayStart);
                            if (rolesArrayEnd > rolesArrayStart) {
                                String rolesArray = payload.substring(rolesArrayStart + 1, rolesArrayEnd);
                                String[] rolesList = rolesArray.split(",");
                                for (String r : rolesList) {
                                    String roleName = r.trim().replace("\"", "");
                                    // Skip default Keycloak roles
                                    if (!roleName.isEmpty() &&
                                        !roleName.equals("offline_access") &&
                                        !roleName.equals("uma_authorization") &&
                                        !roleName.equals("default-roles-tricol")) {
                                        permissions.add("ROLE_" + roleName);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to extract data from token: {}", e.getMessage());
            }
        }

        // Try to get user from database to get role and permissions from MySQL
        if (username != null) {
            try {
                Optional<UserApp> userOpt = userRepository.findByUsernameWithPermissions(username);
                if (userOpt.isPresent()) {
                    UserApp user = userOpt.get();

                    // Get role from database
                    if (user.getRole() != null) {
                        role = user.getRole().getName().name();

                        // Add role permissions from database
                        if (user.getRole().getPermissions() != null) {
                            user.getRole().getPermissions().forEach(permission ->
                                permissions.add(permission.getName().name())
                            );
                        }
                    }

                    // Add user-specific permissions from database
                    if (user.getUserPermissions() != null) {
                        user.getUserPermissions().stream()
                            .filter(up -> up.isActive())
                            .forEach(up -> permissions.add(up.getPermission().getName().name()));
                    }

                    log.debug("Found user in database: role={}, permissions count={}", role, permissions.size());
                } else {
                    log.debug("User not found in database, using JWT roles only");
                }
            } catch (Exception e) {
                log.warn("Failed to fetch user from database: {}", e.getMessage());
            }
        }
        
        return KeycloakAuthResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .tokenType(tokenResponse.getTokenType())
                .expiresIn(tokenResponse.getExpiresIn())
                .username(username)
                .email(email)
                .role(role)
                .permissions(permissions)
                .build();
    }
}
