package com.example.tricol.tricolspringbootrestapi.security;

import com.example.tricol.tricolspringbootrestapi.model.UserApp;
import com.example.tricol.tricolspringbootrestapi.repository.KeycloakUserMappingRepository;
import com.example.tricol.tricolspringbootrestapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakJwtConverter implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

    private final UserRepository userRepository;
    private final KeycloakUserMappingRepository keycloakMappingRepository;
    private final AuthorityService authorityService;

    @Override
    @Transactional(readOnly = true)
    public UsernamePasswordAuthenticationToken convert(Jwt jwt) {
        String keycloakUserId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        
        log.debug("Converting Keycloak JWT for user: {} (Keycloak ID: {})", username, keycloakUserId);

        UserApp user = keycloakMappingRepository.findByKeycloakUserId(keycloakUserId)
                .map(mapping -> userRepository.findById(mapping.getAppUserId()).orElse(null))
                .orElseGet(() -> userRepository.findByUsername(username).orElse(null));

        if (user == null) {
            log.warn("User not found for Keycloak ID: {} or username: {}. Using Keycloak roles only.", keycloakUserId, username);
            // Extract roles from Keycloak JWT and create temporary authentication
            Set<GrantedAuthority> authorities = extractAuthoritiesFromJwt(jwt);
            log.debug("Extracted {} authorities from Keycloak JWT", authorities.size());
            
            // Create a minimal user object for authentication
            UserApp tempUser = UserApp.builder()
                    .username(username)
                    .email(email)
                    .enabled(true)
                    .build();
            
            CustomUserDetails userDetails = new CustomUserDetails(tempUser, authorities);
            return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        }

        log.debug("User found in database: {}", user.getUsername());
        Set<GrantedAuthority> authorities = authorityService.buildAuthorities(user);
        log.debug("Built {} authorities for user from database", authorities.size());
        CustomUserDetails userDetails = new CustomUserDetails(user, authorities);

        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }
    
    private Set<GrantedAuthority> extractAuthoritiesFromJwt(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        // Extract realm roles
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            authorities.addAll(roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toSet()));
        }
        
        // Extract resource/client roles
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            resourceAccess.values().forEach(resource -> {
                if (resource instanceof Map) {
                    Map<String, Object> resourceMap = (Map<String, Object>) resource;
                    if (resourceMap.containsKey("roles")) {
                        List<String> roles = (List<String>) resourceMap.get("roles");
                        authorities.addAll(roles.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toSet()));
                    }
                }
            });
        }
        
        log.debug("Extracted authorities from Keycloak JWT: {}", authorities);
        return authorities;
    }
}
