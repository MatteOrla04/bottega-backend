package com.bottega.bottega_web.model;

import jakarta.persistence.*;

@Entity
@Table(name = "operatori")
public class Operatore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String ruolo = "OPERATORE";

    // IL NUOVO CAMPO FONDAMENTALE PER IL MULTI-TENANT
    @Column(name = "id_bottega")
    private Long idBottega;

    // --- GETTER E SETTER ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRuolo() {
        return ruolo;
    }

    public void setRuolo(String ruolo) {
        this.ruolo = ruolo;
    }

    public Long getIdBottega() {
        return idBottega;
    }

    public void setIdBottega(Long idBottega) {
        this.idBottega = idBottega;
    }
}