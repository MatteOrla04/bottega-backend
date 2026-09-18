package com.bottega.bottega_web.repositary;

import com.bottega.bottega_web.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // Spring Boot scriverà tutte le query SQL al posto nostro!
}