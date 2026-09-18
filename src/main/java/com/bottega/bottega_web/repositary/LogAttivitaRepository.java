package com.bottega.bottega_web.repositary;

import com.bottega.bottega_web.model.LogAttivita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogAttivitaRepository extends JpaRepository<LogAttivita, Long> {
}