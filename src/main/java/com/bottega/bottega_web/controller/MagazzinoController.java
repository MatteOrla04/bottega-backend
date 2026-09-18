package com.bottega.bottega_web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bottega.bottega_web.ProdottoDao;
import com.bottega.bottega_web.model.Prodotto;
import com.bottega.bottega_web.model.Operatore;
import com.bottega.bottega_web.repositary.OperatoreRepository;
import com.bottega.bottega_web.service.AuditService;

import java.util.List;

@RestController
@RequestMapping("/api/magazzino")
public class MagazzinoController {

    private final ProdottoDao prodottoDao;

    // Aggiungiamo i nostri nuovi strumenti per l'Audit
    @Autowired
    private OperatoreRepository operatoreRepo;

    @Autowired
    private AuditService auditService;

    public MagazzinoController(ProdottoDao prodottoDao) {
        this.prodottoDao = prodottoDao;
    }

    // PORTA 1: Invia tutta la lista dei prodotti al caricamento della pagina
    @GetMapping("/prodotti")
    public List<Prodotto> getInventario() {
        System.out.println("Richiesta dal web: Caricamento inventario magazzino");
        return prodottoDao.trovaTutti(); 
    }

    // PORTA 2: Riceve i dati dal pop-up e crea un nuovo prodotto
    @PostMapping("/prodotto")
    public ResponseEntity<String> aggiungiProdotto(
            @RequestBody Prodotto nuovoProdotto,
            @RequestHeader(value = "Operatore", required = false) String usernameOperatore) { // Cattura la firma!
        
        System.out.println("Nuovo prodotto in arrivo: " + nuovoProdotto.getDescrizione());
        try {
            // 1. Salva il prodotto
            prodottoDao.inserisciProdotto(nuovoProdotto);
            
            // 2. Registra l'azione nel Log (se l'operatore è presente)
            if (usernameOperatore != null) {
                operatoreRepo.findByUsername(usernameOperatore).ifPresent(operatore -> {
                    String dettagli = "Aggiunto nuovo prodotto: " + nuovoProdotto.getDescrizione() + " (Codice: " + nuovoProdotto.getCodiceBarre() + ")";
                    auditService.registraAzione(operatore, "INSERIMENTO_MAGAZZINO", dettagli);
                });
            }
            
            return ResponseEntity.ok("Prodotto salvato con successo!");
        } catch (Exception e) {
            System.out.println("Errore salvataggio: " + e.getMessage());
            return ResponseEntity.badRequest().body("Errore nel salvataggio sul database.");
        }
    }

    // PORTA 3: Carico merci (somma i nuovi arrivi alla scorta attuale)
    @PutMapping("/prodotto/scorta")
    public ResponseEntity<String> caricaScorta(
            @RequestParam String barre, 
            @RequestParam int qta,
            @RequestHeader(value = "Operatore", required = false) String usernameOperatore) { // Cattura la firma!
        
        System.out.println("Carico merci per prodotto: " + barre + " | Quantità: +" + qta);
        try {
            Prodotto p = prodottoDao.cercaPerCodiceBarre(barre);
            if (p != null) {
                // 1. Aggiorna la scorta
                int nuovaScorta = p.getScortaMagazzino() + qta;
                prodottoDao.aggiornaScorta(barre, nuovaScorta); 
                
                // 2. Registra l'azione nel Log
                if (usernameOperatore != null) {
                    operatoreRepo.findByUsername(usernameOperatore).ifPresent(operatore -> {
                        String dettagli = "Aggiunta scorta: +" + qta + " per il prodotto " + p.getDescrizione() + " (Codice: " + barre + "). Nuova scorta totale: " + nuovaScorta;
                        auditService.registraAzione(operatore, "CARICO_MERCE", dettagli);
                    });
                }

                return ResponseEntity.ok("Scorta aggiornata a " + nuovaScorta);
            }
            return ResponseEntity.badRequest().body("Prodotto non trovato!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Errore di aggiornamento.");
        }
    }
}