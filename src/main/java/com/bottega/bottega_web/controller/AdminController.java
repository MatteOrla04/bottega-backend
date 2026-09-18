package com.bottega.bottega_web.controller;

import com.bottega.bottega_web.model.Operatore;
import com.bottega.bottega_web.repositary.OperatoreRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final OperatoreRepository operatoreRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(OperatoreRepository operatoreRepository, PasswordEncoder passwordEncoder) {
        this.operatoreRepository = operatoreRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // MODIFICA FONDAMENTALE: Restituisce SOLO i dipendenti della bottega specificata
    @GetMapping("/operatori/bottega/{idBottega}")
    public List<Operatore> getTuttiOperatori(@PathVariable Long idBottega) {
        return operatoreRepository.findByIdBottega(idBottega);
    }

    @PostMapping("/operatori")
    public ResponseEntity<?> creaOperatore(@RequestBody Operatore nuovoOperatore) {
        if (operatoreRepository.findByUsername(nuovoOperatore.getUsername()).isPresent()) {
            return ResponseEntity.status(400).body("Username già esistente.");
        }
        
        String passwordCriptata = passwordEncoder.encode(nuovoOperatore.getPasswordHash());
        nuovoOperatore.setPasswordHash(passwordCriptata);
        
        // L'idBottega arriverà automaticamente dal frontend tramite il JSON. Salviamo e basta!
        operatoreRepository.save(nuovoOperatore);
        return ResponseEntity.ok().body("Dipendente creato con successo!");
    }

    @DeleteMapping("/operatori/{id}")
    public ResponseEntity<?> eliminaOperatore(@PathVariable Long id) {
        if (operatoreRepository.existsById(id)) {
            operatoreRepository.deleteById(id);
            return ResponseEntity.ok().body("Operatore eliminato con successo.");
        } else {
            return ResponseEntity.status(404).body("Operatore non trovato.");
        }
    }
}