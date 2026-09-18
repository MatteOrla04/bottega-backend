package com.bottega.bottega_web.controller;

import java.util.List; 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bottega.bottega_web.CassaService;
import com.bottega.bottega_web.BeneficiarioDao;
import com.bottega.bottega_web.ProdottoDao;
import com.bottega.bottega_web.TransazioneDao; // <-- 1. AGGIUNTO IMPORT STORICO
import com.bottega.bottega_web.model.Beneficiario;
import com.bottega.bottega_web.model.Prodotto;
import com.bottega.bottega_web.model.Transazione;
import com.bottega.bottega_web.dto.ScontrinoDTO;
import com.bottega.bottega_web.dto.ElementoScontrinoDTO;
import org.springframework.dao.DataIntegrityViolationException;

@RestController 
@RequestMapping("/api/cassa") 
public class CassaController {

    private final CassaService cassaService;
    private final BeneficiarioDao beneficiarioDao;
    private final ProdottoDao prodottoDao;
    private final TransazioneDao transazioneDao; // <-- 2. AGGIUNTA VARIABILE STORICO

    // <-- 3. AGGIUNTO TransazioneDao NEL COSTRUTTORE
    public CassaController(CassaService cassaService, BeneficiarioDao beneficiarioDao, ProdottoDao prodottoDao, TransazioneDao transazioneDao) {
        this.cassaService = cassaService;
        this.beneficiarioDao = beneficiarioDao;
        this.prodottoDao = prodottoDao;
        this.transazioneDao = transazioneDao;
    }

    // 1. LA PORTA DELLO SCANNER: Cerca un utente
    @GetMapping("/beneficiario")
    public Beneficiario getBeneficiario(@RequestParam String tessera) {
        System.out.println("Ricerca tessera dal web: " + tessera);
        return beneficiarioDao.cercaPerTessera(tessera);
    }

    // 2. LA PORTA DELLO SCANNER: Cerca un prodotto
    @GetMapping("/prodotto")
    public Prodotto getProdotto(@RequestParam String barre) {
        System.out.println("Ricerca prodotto dal web: " + barre);
        Prodotto p = prodottoDao.cercaPerCodiceBarre(barre);
        if (p == null || p.getScortaMagazzino() <= 0) {
            return null; 
        }
        return p;
    }

    // 3. LA NUOVA PORTA SCONTRINO: Riceve tutto il JSON
    @PostMapping("/acquisto")
    public ResponseEntity<String> registraAcquisto(@RequestBody ScontrinoDTO scontrino) {
        try {
            cassaService.processaScontrino(scontrino);
            return ResponseEntity.ok("Scontrino battuto ed elaborato con successo!");
        } catch (Exception e) {
            System.out.println("Transazione fallita: " + e.getMessage());
            return ResponseEntity.badRequest().body("Errore durante la transazione: " + e.getMessage());
        }
    }

    // 4. LA NUOVA PORTA ISCRIZIONI: Salva il nuovo utente (CON PROTEZIONE DUPLICATI)
    @PostMapping("/beneficiario")
    public ResponseEntity<String> registraNuovoUtente(@RequestBody Beneficiario nuovoUtente) {
        System.out.println("=== NUOVA ISCRIZIONE RICEVUTA ===");
        System.out.println("Tessera: " + nuovoUtente.getCodiceTessera());
        
        try {
            beneficiarioDao.inserisciBeneficiario(nuovoUtente);
            return ResponseEntity.ok("Utente salvato correttamente nel DB!");
            
        } catch (DataIntegrityViolationException e) {
            System.err.println("Errore Duplicato: " + e.getMessage());
            return ResponseEntity.badRequest().body("Attenzione: Esiste già un utente registrato con questa Tessera!");
            
        } catch (Exception e) {
            System.err.println("Errore DB durante l'iscrizione: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Errore interno durante il salvataggio: " + e.getMessage());
        }
    }

    // 5. RESTITUISCE TUTTI GLI ISCRITTI AL SITO WEB
    @GetMapping("/beneficiari")
    public List<Beneficiario> getTuttiIBeneficiari() {
        return beneficiarioDao.trovaTutti();
    }

    // 6. ELIMINA UN UTENTE
    @DeleteMapping("/beneficiario/{tessera}")
    public ResponseEntity<String> cancellaUtente(@PathVariable String tessera) {
        try {
            beneficiarioDao.eliminaBeneficiario(tessera);
            return ResponseEntity.ok("Utente eliminato con successo!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Errore durante l'eliminazione: " + e.getMessage());
        }
    }

    // 7. RICARICA PUNTI MANUALE (Dalla pagina Dettagli/Anagrafica)
    @PostMapping("/beneficiario/ricarica")
    public ResponseEntity<String> ricaricaPunti(@RequestParam String tessera, @RequestParam int puntiAggiuntivi) {
        try {
            Beneficiario b = beneficiarioDao.cercaPerTessera(tessera);
            if (b == null) {
                return ResponseEntity.badRequest().body("Errore: Tessera non trovata.");
            }
            
            int nuovoSaldo = b.getSaldoPunti() + puntiAggiuntivi;
            beneficiarioDao.aggiornaSaldoPunti(tessera, nuovoSaldo);
            
            return ResponseEntity.ok("Ricarica completata! Il nuovo saldo è di " + nuovoSaldo + " punti.");
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Errore durante la ricarica: " + e.getMessage());
        }
    }

    @DeleteMapping("/beneficiario/documento")
public org.springframework.http.ResponseEntity<String> eliminaDocumentoBeneficiario(@org.springframework.web.bind.annotation.RequestParam String tessera) {
    try {
        beneficiarioDao.eliminaDocumento(tessera);
        return org.springframework.http.ResponseEntity.ok("Documento d'identità eliminato con successo!");
    } catch (Exception e) {
        return org.springframework.http.ResponseEntity.badRequest().body("Errore durante l'eliminazione del documento.");
    }
}

    // 8. LA NUOVA PORTA PER LO STORICO SCONTRINI <-- ECCOLA QUI!
    @GetMapping("/beneficiario/storico")
    public List<Transazione> getStoricoUtente(@RequestParam String tessera) {
        System.out.println("Richiesto storico per tessera: " + tessera);
        return transazioneDao.trovaStoricoPerTessera(tessera);
    }
}