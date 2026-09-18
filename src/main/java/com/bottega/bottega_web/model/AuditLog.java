package com.bottega.bottega_web.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Colleghiamo il log all'operatore che ha fatto l'azione
    @ManyToOne
    @JoinColumn(name = "operatore_id")
    private Operatore operatore;

    @Column(nullable = false)
    private String azione;

    @Column(length = 1000) // Diamo un po' di spazio per scrivere i dettagli lunghi
    private String dettagli;

    @Column(nullable = false)
    private LocalDateTime dataOra;

    // --- GETTER E SETTER ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Operatore getOperatore() { return operatore; }
    public void setOperatore(Operatore operatore) { this.operatore = operatore; }

    public String getAzione() { return azione; }
    public void setAzione(String azione) { this.azione = azione; }

    public String getDettagli() { return dettagli; }
    public void setDettagli(String dettagli) { this.dettagli = dettagli; }

    public LocalDateTime getDataOra() { return dataOra; }
    public void setDataOra(LocalDateTime dataOra) { this.dataOra = dataOra; }
}