package com.bottega.bottega_web.repositary;

import com.bottega.bottega_web.model.Operatore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OperatoreRepository extends JpaRepository<Operatore, Long> {
    // Ci servirà per trovare l'utente durante il Login
    Optional<Operatore> findByUsername(String username);
}