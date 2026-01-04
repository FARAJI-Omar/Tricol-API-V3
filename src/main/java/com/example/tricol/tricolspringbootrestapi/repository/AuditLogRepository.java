package com.example.tricol.tricolspringbootrestapi.repository;

import com.example.tricol.tricolspringbootrestapi.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
