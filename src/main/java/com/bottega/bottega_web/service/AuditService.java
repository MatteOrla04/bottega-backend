package com.bottega.bottega_web.service;

import com.bottega.bottega_web.model.AuditLog;
import com.bottega.bottega_web.model.Operatore;
import com.bottega.bottega_web.repositary.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepo;

    public void registraAzione(Operatore operatore, String azione, String dettagli) {
        AuditLog log = new AuditLog();
        log.setOperatore(operatore);
        log.setAzione(azione);
        log.setDettagli(dettagli);
        log.setDataOra(LocalDateTime.now()); // Salva l'ora esatta di adesso
        
        auditLogRepo.save(log);
    }
}