package com.bottega.bottega_web.controller;

import com.bottega.bottega_web.model.Operatore;
import com.bottega.bottega_web.repositary.OperatoreRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final OperatoreRepository operatoreRepository;

    public AdminController(OperatoreRepository operatoreRepository) {
        this.operatoreRepository = operatoreRepository;
    }

    @GetMapping("/operatori")
    public List<Operatore> getTuttiOperatori() {
        return operatoreRepository.findAll();
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