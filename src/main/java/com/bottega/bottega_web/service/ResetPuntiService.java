package com.bottega.bottega_web.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.bottega.bottega_web.BeneficiarioDao;

@Service
public class ResetPuntiService {

    private final BeneficiarioDao beneficiarioDao;

    public ResetPuntiService(BeneficiarioDao beneficiarioDao) {
        this.beneficiarioDao = beneficiarioDao;
    }

    @Scheduled(cron = "0 0 0 * * MON")
    public void ricaricaPuntiAutomatica() {
        System.out.println("[LUNEDÌ ORE 00:00] Avvio procedura di ricarica settimanale GLOBALE...");
        // Modificato: il robot non ha una bottega, quindi lancia il comando per tutti
        beneficiarioDao.resetPuntiSettimanaleGlobale();
        System.out.println("Procedura completata! Tutte le tessere di tutte le botteghe sono pronte.");
    }
}