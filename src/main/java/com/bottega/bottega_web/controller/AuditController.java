package com.bottega.bottega_web.controller;

import com.bottega.bottega_web.model.AuditLog;
import com.bottega.bottega_web.repositary.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @Autowired
    private AuditLogRepository auditLogRepo;

    // L'endpoint che spedisce tutti i log al frontend
    @GetMapping("/logs")
    public List<AuditLog> getTuttiILog() {
        // Peschiamo tutti i log dal database (dal più vecchio al più nuovo)
        return auditLogRepo.findAll();
    }
}