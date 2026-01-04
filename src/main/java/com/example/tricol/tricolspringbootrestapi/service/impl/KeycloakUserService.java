package com.example.tricol.tricolspringbootrestapi.service.impl;

import com.example.tricol.tricolspringbootrestapi.model.UserApp;
import com.example.tricol.tricolspringbootrestapi.repository.KeycloakUserMappingRepository;
import com.example.tricol.tricolspringbootrestapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "keycloak.enabled", havingValue = "true", matchIfMissing = false)
public class KeycloakUserService {

    private final Keycloak keycloakAdminClient;
    private final KeycloakUserMappingRepository keycloakMappingRepository;
    private final UserRepository userRepository;

    @Value("${keycloak.realm}")
    private String realm;

    public String findKeycloakUserId(Long appUserId) {
        return keycloakMappingRepository.findByAppUserId(appUserId)
                .map(mapping -> mapping.getKeycloakUserId())
                .orElse(null);
    }

    public UserApp findAppUserByKeycloakId(String keycloakUserId) {
        return keycloakMappingRepository.findByKeycloakUserId(keycloakUserId)
                .flatMap(mapping -> userRepository.findById(mapping.getAppUserId()))
                .orElse(null);
    }

    public String findKeycloakUserIdByUsername(String username) {
        try {
            List<UserRepresentation> users = keycloakAdminClient.realm(realm)
                    .users()
                    .search(username, true);

            if (!users.isEmpty()) {
                return users.get(0).getId();
            }
        } catch (Exception e) {
            log.error("Error finding Keycloak user by username: {}", e.getMessage());
        }
        return null;
    }

    public UserRepresentation getKeycloakUser(String keycloakUserId) {
        try {
            return keycloakAdminClient.realm(realm)
                    .users()
                    .get(keycloakUserId)
                    .toRepresentation();
        } catch (Exception e) {
            log.error("Error getting Keycloak user: {}", e.getMessage());
            return null;
        }
    }
}
