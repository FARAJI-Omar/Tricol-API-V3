package com.example.tricol.tricolspringbootrestapi.service;

public interface AuditService {
    void logAction(String username, String action, String entityId, String entityType);
    void logPermissionChange(Long userId, String username, String permissionName, boolean granted, Long adminId);
}
