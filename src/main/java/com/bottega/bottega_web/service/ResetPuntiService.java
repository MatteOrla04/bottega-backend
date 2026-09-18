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

    //  "Esegui ogni Lunedì (MON) alle ore 00:00"
    // (I campi sono: Secondo Minuto Ora Giorno Mese GiornoDellaSettimana)
    @Scheduled(cron = "0 0 0 * * MON")
    public void ricaricaPuntiAutomatica() {
        System.out.println("[LUNEDÌ ORE 00:00] Avvio procedura di ricarica settimanale...");
        beneficiarioDao.resetPuntiSettimanale();
        System.out.println("Procedura completata! Tutte le tessere sono pronte per la nuova settimana.");
    }
}