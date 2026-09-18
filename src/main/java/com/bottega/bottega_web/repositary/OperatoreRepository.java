package com.bottega.bottega_web.repositary;

import com.bottega.bottega_web.model.Operatore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface OperatoreRepository extends JpaRepository<Operatore, Long> {
    Optional<Operatore> findByUsername(String username);
    
    // IL FILTRO DI SICUREZZA: Trova solo i dipendenti di una specifica bottega
    List<Operatore> findByIdBottega(Long idBottega);
}