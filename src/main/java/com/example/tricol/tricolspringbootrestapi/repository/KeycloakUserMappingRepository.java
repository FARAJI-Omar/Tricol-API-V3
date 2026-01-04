package com.example.tricol.tricolspringbootrestapi.repository;

import com.example.tricol.tricolspringbootrestapi.model.KeycloakUserMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KeycloakUserMappingRepository extends JpaRepository<KeycloakUserMapping, Long> {
    
    Optional<KeycloakUserMapping> findByAppUserId(Long appUserId);
    
    Optional<KeycloakUserMapping> findByKeycloakUserId(String keycloakUserId);
    
    boolean existsByAppUserId(Long appUserId);
}
