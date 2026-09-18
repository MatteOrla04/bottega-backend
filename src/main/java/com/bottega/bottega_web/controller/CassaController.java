package com.bottega.bottega_web.controller;

import java.util.List; 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bottega.bottega_web.CassaService;
import com.bottega.bottega_web.BeneficiarioDao;
import com.bottega.bottega_web.ProdottoDao;
import com.bottega.bottega_web.TransazioneDao; 
import com.bottega.bottega_web.model.Beneficiario;
import com.bottega.bottega_web.model.Prodotto;
import com.bottega.bottega_web.model.Transazione;
import com.bottega.bottega_web.dto.ScontrinoDTO;
import org.springframework.dao.DataIntegrityViolationException;

@RestController 
@RequestMapping("/api/cassa") 
public class CassaController {

    private final CassaService cassaService;
    private final BeneficiarioDao beneficiarioDao;
    private final ProdottoDao prodottoDao;
    private final TransazioneDao transazioneDao; 

    public CassaController(CassaService cassaService, BeneficiarioDao beneficiarioDao, ProdottoDao prodottoDao, TransazioneDao transazioneDao) {
        this.cassaService = cassaService;
        this.beneficiarioDao = beneficiarioDao;
        this.prodottoDao = prodottoDao;
        this.transazioneDao = transazioneDao;
    }

    @GetMapping("/beneficiario")
    public Beneficiario getBeneficiario(@RequestParam String tessera, @RequestHeader(value = "idBottega", defaultValue = "1") Long idBottega) {
        return beneficiarioDao.cercaPerTessera(tessera, idBottega);
    }

    @GetMapping("/prodotto")
    public Prodotto getProdotto(@RequestParam String barre, @RequestHeader(value = "idBottega", defaultValue = "1") Long idBottega) {
        Prodotto p = prodottoDao.cercaPerCodiceBarre(barre, idBottega);
        if (p == null || p.getScortaMagazzino() <= 0) {
            return null; 
        }
        return p;
    }

    @PostMapping("/acquisto")
    public ResponseEntity<String> registraAcquisto(@RequestBody ScontrinoDTO scontrino, @RequestHeader(value = "idBottega", defaultValue = "1") Long idBottega) {
        try {
            // NOTA: Dovremo aggiornare CassaService per passargli idBottega!
            cassaService.processaScontrino(scontrino, idBottega);
            return ResponseEntity.ok("Scontrino battuto ed elaborato con successo!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Errore durante la transazione: " + e.getMessage());
        }
    }

    @PostMapping("/beneficiario")
    public ResponseEntity<String> registraNuovoUtente(@RequestBody Beneficiario nuovoUtente, @RequestHeader(value = "idBottega", defaultValue = "1") Long idBottega) {
        try {
            nuovoUtente.setIdBottega(idBottega); // Assegniamo il cliente alla bottega che lo sta registrando
            beneficiarioDao.inserisciBeneficiario(nuovoUtente);
            return ResponseEntity.ok("Utente salvato correttamente nel DB!");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("Attenzione: Esiste già un utente registrato con questa Tessera!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Errore interno durante il salvataggio: " + e.getMessage());
        }
    }

    @GetMapping("/beneficiari")
    public List<Beneficiario> getTuttiIBeneficiari(@RequestHeader(value = "idBottega", defaultValue = "1") Long idBottega) {
        return beneficiarioDao.trovaTutti(idBottega);
    }

    @DeleteMapping("/beneficiario/{tessera}")
    public ResponseEntity<String> cancellaUtente(@PathVariable String tessera, @RequestHeader(value = "idBottega", defaultValue = "1") Long idBottega) {
        try {
            beneficiarioDao.eliminaBeneficiario(tessera, idBottega);
            return ResponseEntity.ok("Utente eliminato con successo!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Errore durante l'eliminazione: " + e.getMessage());
        }
    }

    @PostMapping("/beneficiario/ricarica")
    public ResponseEntity<String> ricaricaPunti(@RequestParam String tessera, @RequestParam int puntiAggiuntivi, @RequestHeader(value = "idBottega", defaultValue = "1") Long idBottega) {
        try {
            Beneficiario b = beneficiarioDao.cercaPerTessera(tessera, idBottega);
            if (b == null) return ResponseEntity.badRequest().body("Errore: Tessera non trovata.");
            
            int nuovoSaldo = b.getSaldoPunti() + puntiAggiuntivi;
            beneficiarioDao.aggiornaSaldoPunti(tessera, nuovoSaldo, idBottega);
            return ResponseEntity.ok("Ricarica completata! Il nuovo saldo è di " + nuovoSaldo + " punti.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Errore durante la ricarica: " + e.getMessage());
        }
    }

    @DeleteMapping("/beneficiario/documento")
    public ResponseEntity<String> eliminaDocumentoBeneficiario(@RequestParam String tessera, @RequestHeader(value = "idBottega", defaultValue = "1") Long idBottega) {
        try {
            beneficiarioDao.eliminaDocumento(tessera, idBottega);
            return ResponseEntity.ok("Documento d'identità eliminato con successo!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Errore durante l'eliminazione del documento.");
        }
    }

    @GetMapping("/beneficiario/storico")
    public List<Transazione> getStoricoUtente(@RequestParam String tessera, @RequestHeader(value = "idBottega", defaultValue = "1") Long idBottega) {
        // NOTA: Dovremo aggiornare TransazioneDao per questo metodo
        return transazioneDao.trovaStoricoPerTessera(tessera, idBottega);
    }
}