package com.bottega.bottega_web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bottega.bottega_web.ProdottoDao;
import com.bottega.bottega_web.model.Prodotto;
import com.bottega.bottega_web.repositary.OperatoreRepository;
import com.bottega.bottega_web.service.AuditService;

import java.util.List;

@RestController
@RequestMapping("/api/magazzino")
public class MagazzinoController {

    private final ProdottoDao prodottoDao;

    @Autowired
    private OperatoreRepository operatoreRepo;

    @Autowired
    private AuditService auditService;

    public MagazzinoController(ProdottoDao prodottoDao) {
        this.prodottoDao = prodottoDao;
    }

    @GetMapping("/prodotti")
    public List<Prodotto> getInventario(@RequestHeader(value = "idBottega", defaultValue = "1") Long idBottega) {
        System.out.println("Richiesta dal web: Caricamento inventario magazzino (Bottega " + idBottega + ")");
        // PASSAGGIO CHIAVE: Filtra per bottega
        return prodottoDao.trovaTutti(idBottega); 
    }

    @PostMapping("/prodotto")
    public ResponseEntity<String> aggiungiProdotto(
            @RequestBody Prodotto nuovoProdotto,
            @RequestHeader(value = "Operatore", required = false) String usernameOperatore,
            @RequestHeader(value = "idBottega", defaultValue = "1") Long idBottega) { 
        
        try {
            // PASSAGGIO CHIAVE: Assegniamo l'idBottega al nuovo prodotto prima di salvarlo
            nuovoProdotto.setIdBottega(idBottega);
            prodottoDao.inserisciProdotto(nuovoProdotto);
            
            if (usernameOperatore != null) {
                operatoreRepo.findByUsername(usernameOperatore).ifPresent(operatore -> {
                    String dettagli = "Aggiunto nuovo prodotto: " + nuovoProdotto.getDescrizione() + " (Codice: " + nuovoProdotto.getCodiceBarre() + ")";
                    auditService.registraAzione(operatore, "INSERIMENTO_MAGAZZINO", dettagli);
                });
            }
            
            return ResponseEntity.ok("Prodotto salvato con successo nella tua Bottega!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Errore nel salvataggio sul database.");
        }
    }

    @PutMapping("/prodotto/scorta")
    public ResponseEntity<String> caricaScorta(
            @RequestParam String barre, 
            @RequestParam int qta,
            @RequestHeader(value = "Operatore", required = false) String usernameOperatore,
            @RequestHeader(value = "idBottega", defaultValue = "1") Long idBottega) { 
        
        try {
            // PASSAGGIO CHIAVE: Cerca e aggiorna solo se il prodotto è della tua bottega
            Prodotto p = prodottoDao.cercaPerCodiceBarre(barre, idBottega);
            if (p != null) {
                int nuovaScorta = p.getScortaMagazzino() + qta;
                prodottoDao.aggiornaScorta(barre, nuovaScorta, idBottega); 
                
                if (usernameOperatore != null) {
                    operatoreRepo.findByUsername(usernameOperatore).ifPresent(operatore -> {
                        String dettagli = "Aggiunta scorta: +" + qta + " per il prodotto " + p.getDescrizione() + " (Codice: " + barre + "). Nuova scorta totale: " + nuovaScorta;
                        auditService.registraAzione(operatore, "CARICO_MERCE", dettagli);
                    });
                }

                return ResponseEntity.ok("Scorta aggiornata a " + nuovaScorta);
            }
            return ResponseEntity.badRequest().body("Prodotto non trovato nel tuo magazzino!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Errore di aggiornamento.");
        }
    }
}