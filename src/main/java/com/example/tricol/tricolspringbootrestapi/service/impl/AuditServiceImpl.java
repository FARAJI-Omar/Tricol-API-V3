package com.example.tricol.tricolspringbootrestapi.service.impl;

import com.example.tricol.tricolspringbootrestapi.model.AuditLog;
import com.example.tricol.tricolspringbootrestapi.repository.AuditLogRepository;
import com.example.tricol.tricolspringbootrestapi.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void logAction(String username, String action, String entityId, String entityType) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(username);
        auditLog.setAction(action);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setEntityId(entityId);
        auditLog.setEntityType(entityType);
        
        auditLogRepository.save(auditLog);
    }
}
