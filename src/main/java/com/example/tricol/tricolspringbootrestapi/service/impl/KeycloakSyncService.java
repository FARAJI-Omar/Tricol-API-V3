package com.example.tricol.tricolspringbootrestapi.service.impl;

import com.example.tricol.tricolspringbootrestapi.model.KeycloakUserMapping;
import com.example.tricol.tricolspringbootrestapi.model.Permission;
import com.example.tricol.tricolspringbootrestapi.model.RoleApp;
import com.example.tricol.tricolspringbootrestapi.model.UserApp;
import com.example.tricol.tricolspringbootrestapi.repository.KeycloakUserMappingRepository;
import com.example.tricol.tricolspringbootrestapi.security.AuthorityService;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "keycloak.enabled", havingValue = "true", matchIfMissing = false)
public class KeycloakSyncService {

    private final Keycloak keycloakAdminClient;
    private final KeycloakUserMappingRepository keycloakMappingRepository;
    private final AuthorityService authorityService;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    public void syncUserToKeycloak(UserApp user) {
        syncUserToKeycloak(user, null);
    }

    public void syncUserToKeycloak(UserApp user, String plainPassword) {
        try {
            Optional<KeycloakUserMapping> existingMapping = keycloakMappingRepository.findByAppUserId(user.getId());
            
            if (existingMapping.isPresent()) {
                log.info("User already synced to Keycloak: {}", user.getUsername());
                updateKeycloakUser(existingMapping.get().getKeycloakUserId(), user);
                return;
            }

            UserRepresentation keycloakUser = new UserRepresentation();
            keycloakUser.setUsername(user.getUsername());
            keycloakUser.setEmail(user.getEmail());
            keycloakUser.setEnabled(user.isEnabled());
            keycloakUser.setEmailVerified(true);
            
            // Split fullName into firstName and lastName
            if (user.getFullName() != null && !user.getFullName().isEmpty()) {
                String[] nameParts = user.getFullName().trim().split("\\s+", 2);
                keycloakUser.setFirstName(nameParts[0]);
                keycloakUser.setLastName(nameParts.length > 1 ? nameParts[1] : nameParts[0]);
            } else {
                keycloakUser.setFirstName(user.getUsername());
                keycloakUser.setLastName(user.getUsername());
            }

            Map<String, List<String>> attributes = new HashMap<>();
            attributes.put("app_user_id", List.of(user.getId().toString()));
            keycloakUser.setAttributes(attributes);

            Response response = keycloakAdminClient.realm(realm).users().create(keycloakUser);
            
            if (response.getStatus() == 201) {
                String keycloakUserId = extractUserIdFromResponse(response);
                
                // Set password if provided
                if (plainPassword != null && !plainPassword.isEmpty()) {
                    setKeycloakPassword(keycloakUserId, plainPassword);
                }
                
                KeycloakUserMapping mapping = KeycloakUserMapping.builder()
                        .appUserId(user.getId())
                        .keycloakUserId(keycloakUserId)
                        .syncStatus("SYNCED")
                        .lastSyncedAt(LocalDateTime.now())
                        .build();
                keycloakMappingRepository.save(mapping);

                if (user.getRole() != null) {
                    syncRoleToKeycloak(keycloakUserId, user.getRole());
                }
                syncPermissionsToKeycloak(keycloakUserId, user);
                
                log.info("User synced to Keycloak successfully: {}", user.getUsername());
            } else {
                log.error("Failed to create user in Keycloak. Status: {}", response.getStatus());
            }
            response.close();
        } catch (Exception e) {
            log.error("Error syncing user to Keycloak: {}", e.getMessage(), e);
        }
    }

    public void syncRoleToKeycloak(String keycloakUserId, RoleApp role) {
        try {
            List<RoleRepresentation> existingRoles = keycloakAdminClient.realm(realm)
                    .users().get(keycloakUserId)
                    .roles().realmLevel().listAll();

            existingRoles.forEach(existingRole -> {
                keycloakAdminClient.realm(realm)
                        .users().get(keycloakUserId)
                        .roles().realmLevel().remove(List.of(existingRole));
            });

            RoleRepresentation roleRep = keycloakAdminClient.realm(realm)
                    .roles().get(role.getName().name()).toRepresentation();

            keycloakAdminClient.realm(realm)
                    .users().get(keycloakUserId)
                    .roles().realmLevel().add(List.of(roleRep));

            log.info("Role synced to Keycloak: {}", role.getName());
        } catch (Exception e) {
            log.error("Error syncing role to Keycloak: {}", e.getMessage(), e);
        }
    }

    public void syncPermissionsToKeycloak(String keycloakUserId, UserApp user) {
        try {
            Set<GrantedAuthority> authorities = authorityService.buildAuthorities(user);

            String clientUuid = getClientUuid();
            if (clientUuid == null) {
                log.error("Client UUID not found for client: {}", clientId);
                return;
            }

            List<RoleRepresentation> existingClientRoles = keycloakAdminClient.realm(realm)
                    .users().get(keycloakUserId)
                    .roles().clientLevel(clientUuid).listAll();

            if (!existingClientRoles.isEmpty()) {
                keycloakAdminClient.realm(realm)
                        .users().get(keycloakUserId)
                        .roles().clientLevel(clientUuid).remove(existingClientRoles);
            }

            List<RoleRepresentation> clientRoles = authorities.stream()
                    .map(auth -> {
                        try {
                            return keycloakAdminClient.realm(realm)
                                    .clients().get(clientUuid)
                                    .roles().get(auth.getAuthority()).toRepresentation();
                        } catch (Exception e) {
                            log.warn("Client role not found in Keycloak: {}", auth.getAuthority());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!clientRoles.isEmpty()) {
                keycloakAdminClient.realm(realm)
                        .users().get(keycloakUserId)
                        .roles().clientLevel(clientUuid).add(clientRoles);
            }

            log.info("Permissions synced to Keycloak for user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Error syncing permissions to Keycloak: {}", e.getMessage(), e);
        }
    }

    public void removePermissionFromKeycloak(String keycloakUserId, Permission permission) {
        try {
            String clientUuid = getClientUuid();
            if (clientUuid == null) return;

            RoleRepresentation roleRep = keycloakAdminClient.realm(realm)
                    .clients().get(clientUuid)
                    .roles().get(permission.getName().name()).toRepresentation();

            keycloakAdminClient.realm(realm)
                    .users().get(keycloakUserId)
                    .roles().clientLevel(clientUuid).remove(List.of(roleRep));

            log.info("Permission removed from Keycloak: {}", permission.getName());
        } catch (Exception e) {
            log.error("Error removing permission from Keycloak: {}", e.getMessage(), e);
        }
    }

    private void updateKeycloakUser(String keycloakUserId, UserApp user) {
        try {
            UserRepresentation keycloakUser = keycloakAdminClient.realm(realm)
                    .users().get(keycloakUserId).toRepresentation();

            keycloakUser.setEnabled(user.isEnabled());
            keycloakUser.setEmail(user.getEmail());
            
            // Update firstName and lastName from fullName
            if (user.getFullName() != null && !user.getFullName().isEmpty()) {
                String[] nameParts = user.getFullName().trim().split("\\s+", 2);
                keycloakUser.setFirstName(nameParts[0]);
                keycloakUser.setLastName(nameParts.length > 1 ? nameParts[1] : nameParts[0]);
            }

            keycloakAdminClient.realm(realm).users().get(keycloakUserId).update(keycloakUser);

            if (user.getRole() != null) {
                syncRoleToKeycloak(keycloakUserId, user.getRole());
            }
            syncPermissionsToKeycloak(keycloakUserId, user);

            KeycloakUserMapping mapping = keycloakMappingRepository.findByKeycloakUserId(keycloakUserId)
                    .orElseThrow();
            mapping.setLastSyncedAt(LocalDateTime.now());
            keycloakMappingRepository.save(mapping);

            log.info("Keycloak user updated: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Error updating Keycloak user: {}", e.getMessage(), e);
        }
    }

    private String extractUserIdFromResponse(Response response) {
        String location = response.getHeaderString("Location");
        return location.substring(location.lastIndexOf('/') + 1);
    }

    private String getClientUuid() {
        try {
            return keycloakAdminClient.realm(realm)
                    .clients().findByClientId(clientId)
                    .get(0).getId();
        } catch (Exception e) {
            log.error("Error getting client UUID: {}", e.getMessage());
            return null;
        }
    }

    private void setKeycloakPassword(String keycloakUserId, String plainPassword) {
        try {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(plainPassword);
            credential.setTemporary(false);

            keycloakAdminClient.realm(realm)
                    .users().get(keycloakUserId)
                    .resetPassword(credential);

            log.info("Password set for Keycloak user: {}", keycloakUserId);
        } catch (Exception e) {
            log.error("Error setting Keycloak password: {}", e.getMessage(), e);
        }
    }
}
