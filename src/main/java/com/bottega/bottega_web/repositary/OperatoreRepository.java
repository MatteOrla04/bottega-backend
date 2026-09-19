package com.bottega.bottega_web.repositary;

import com.bottega.bottega_web.model.Operatore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface OperatoreRepository extends JpaRepository<Operatore, Long> {
    Optional<Operatore> findByUsername(String username);
    
    List<Operatore> findByIdBottega(Long idBottega);

    // CERCA L'ID PIÙ ALTO ESISTENTE
    @Query("SELECT MAX(o.idBottega) FROM Operatore o")
    Long findMaxIdBottega();
}