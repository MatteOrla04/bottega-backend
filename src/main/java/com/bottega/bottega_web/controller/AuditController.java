package com.bottega.bottega_web.controller;

import com.bottega.bottega_web.model.AuditLog;
import com.bottega.bottega_web.repositary.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @Autowired
    private AuditLogRepository auditLogRepo;

    @GetMapping("/logs")
    public List<AuditLog> getTuttiILog(@RequestHeader(value = "idBottega", defaultValue = "1") Long idBottega) {
        // Ora il server usa zero memoria extra. Estrae solo gli ultimi 100 log utili!
        return auditLogRepo.findTop100ByOperatore_IdBottegaOrderByDataOraDesc(idBottega);
    }
}