package com.bottega.bottega_web.controller;

import com.bottega.bottega_web.model.Operatore;
import com.bottega.bottega_web.repositary.OperatoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private OperatoreRepository operatoreRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/registrazione")
    public ResponseEntity<String> registra(@RequestBody Operatore nuovoOperatore) {
        
        if (operatoreRepo.findByUsername(nuovoOperatore.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username già in uso!");
        }
        
        String passwordCriptata = passwordEncoder.encode(nuovoOperatore.getPasswordHash());
        nuovoOperatore.setPasswordHash(passwordCriptata);
        
        // MULTI-TENANT: Se è un nuovo cliente o si registra come ADMIN, crea una nuova Bottega isolata
        if (nuovoOperatore.getRuolo() == null || nuovoOperatore.getRuolo().isEmpty() || nuovoOperatore.getRuolo().equals("ADMIN")) {
            nuovoOperatore.setRuolo("ADMIN");
            nuovoOperatore.setIdBottega(System.currentTimeMillis() % 100000); // Genera un ID Bottega univoco
        }
        
        operatoreRepo.save(nuovoOperatore);
        return ResponseEntity.ok("Registrazione completata con successo!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Operatore credenziali) {
        
        Optional<Operatore> op = operatoreRepo.findByUsername(credenziali.getUsername());
        
        if (op.isPresent() && passwordEncoder.matches(credenziali.getPasswordHash(), op.get().getPasswordHash())) {
            
            Map<String, String> risposta = new HashMap<>();
            risposta.put("messaggio", "Login effettuato!");
            risposta.put("username", op.get().getUsername());
            risposta.put("ruolo", op.get().getRuolo() != null ? op.get().getRuolo() : "OPERATORE");
            
            // Inviamo l'ID Bottega al frontend così se lo ricorda per tutta la sessione
            Long idBottega = op.get().getIdBottega() != null ? op.get().getIdBottega() : 1L;
            risposta.put("idBottega", String.valueOf(idBottega));
            
            return ResponseEntity.ok(risposta);
        }
        
        return ResponseEntity.status(401).body("Username o password errati!");
    }
}