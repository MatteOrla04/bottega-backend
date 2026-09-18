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

    // Aggiunto il PasswordEncoder al costruttore per importarlo
    public AdminController(OperatoreRepository operatoreRepository, PasswordEncoder passwordEncoder) {
        this.operatoreRepository = operatoreRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/operatori")
    public List<Operatore> getTuttiOperatori() {
        return operatoreRepository.findAll();
    }

    @PostMapping("/operatori")
    public ResponseEntity<?> creaOperatore(@RequestBody Operatore nuovoOperatore) {
        // Verifica se l'username esiste già
        if (operatoreRepository.findByUsername(nuovoOperatore.getUsername()).isPresent()) {
            return ResponseEntity.status(400).body("Username già esistente.");
        }
        
        // CRIPTIAMO LA PASSWORD PRIMA DI SALVARLA!
        String passwordCriptata = passwordEncoder.encode(nuovoOperatore.getPasswordHash());
        nuovoOperatore.setPasswordHash(passwordCriptata);
        
        // Salvataggio del nuovo operatore
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