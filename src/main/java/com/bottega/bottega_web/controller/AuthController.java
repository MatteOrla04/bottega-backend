package com.bottega.bottega_web.controller;

import com.bottega.bottega_web.model.Operatore;
import com.bottega.bottega_web.repositary.OperatoreRepository; // Mantenuto il tuo import esatto!
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

    // --- REGISTRAZIONE ---
    @PostMapping("/registrazione")
    public ResponseEntity<String> registra(@RequestBody Operatore nuovoOperatore) {
        
        // 1. Controlliamo che l'username non sia già stato rubato
        if (operatoreRepo.findByUsername(nuovoOperatore.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username già in uso!");
        }
        
        // 2. Criptiamo la password con BCrypt
        String passwordCriptata = passwordEncoder.encode(nuovoOperatore.getPasswordHash());
        nuovoOperatore.setPasswordHash(passwordCriptata);
        
        // 3. Assegniamo il ruolo di base (OPERATORE) se non c'è
        if (nuovoOperatore.getRuolo() == null || nuovoOperatore.getRuolo().isEmpty()) {
            nuovoOperatore.setRuolo("OPERATORE");
        }
        
        // 4. Salviamo nel database
        operatoreRepo.save(nuovoOperatore);
        
        return ResponseEntity.ok("Operatore registrato con successo!");
    }

    // --- LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Operatore credenziali) { // Modificato in <?> per inviare JSON
        
        // 1. Cerchiamo l'utente nel database
        Optional<Operatore> op = operatoreRepo.findByUsername(credenziali.getUsername());
        
        if (op.isPresent()) {
            // 2. Controlliamo se la password coincide
            if (passwordEncoder.matches(credenziali.getPasswordHash(), op.get().getPasswordHash())) {
                
                // 3. CREIAMO IL PACCHETTO JSON CON IL RUOLO DA INVIARE AL BROWSER
                Map<String, String> risposta = new HashMap<>();
                risposta.put("messaggio", "Login effettuato!");
                risposta.put("username", op.get().getUsername());
                
                String ruolo = op.get().getRuolo() != null ? op.get().getRuolo() : "OPERATORE";
                risposta.put("ruolo", ruolo);
                
                return ResponseEntity.ok(risposta);
            }
        }
        
        return ResponseEntity.status(401).body("Username o password errati!");
    }
}