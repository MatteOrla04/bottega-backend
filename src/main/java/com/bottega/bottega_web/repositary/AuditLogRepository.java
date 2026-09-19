package com.bottega.bottega_web.repositary;

import com.bottega.bottega_web.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    // MAGIA SPRING DATA: Limita i risultati agli ultimi 100, ordinati per data recente, filtrati per bottega
    List<AuditLog> findTop100ByOperatore_IdBottegaOrderByDataOraDesc(Long idBottega);
}