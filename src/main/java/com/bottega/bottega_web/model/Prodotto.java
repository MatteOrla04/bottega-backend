package com.bottega.bottega_web.model;

public class Prodotto {
    
    private Integer id;
    private String codiceBarre;
    private String descrizione;
    private int puntiCosto; 
    private int scortaMagazzino;
    
    // IL NUOVO CAMPO MULTI-TENANT
    private Long idBottega;
    
    public Prodotto() {}

    public Prodotto(Integer id, String codiceBarre, String descrizione, int puntiCosto, int scortaMagazzino) {
        this.id = id;
        this.codiceBarre = codiceBarre;
        this.descrizione = descrizione; 
        
        if (puntiCosto < 0) {
            throw new IllegalArgumentException("Il costo in punti non può essere negativo.");
        }
        this.puntiCosto = puntiCosto;
        
        if (scortaMagazzino < 0) {
            throw new IllegalArgumentException("La scorta in magazzino non può essere negativa.");
        }
        this.scortaMagazzino = scortaMagazzino;
    }

    public void setId(Integer id) { this.id = id; }
    public Integer getId() { return id; }

    public void setCodiceBarre(String codiceBarre) { this.codiceBarre = codiceBarre; }
    public String getCodiceBarre() { return codiceBarre; }

    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
    public String getDescrizione() { return descrizione; }

    public void setPuntiCosto(int puntiCosto) { 
        if (puntiCosto < 0) throw new IllegalArgumentException("Il costo non può scendere sotto lo zero.");
        this.puntiCosto = puntiCosto; 
    }
    public int getPuntiCosto() { return puntiCosto; }

    public void setScortaMagazzino(int scortaMagazzino) { 
        if (scortaMagazzino < 0) throw new IllegalArgumentException("La scorta non può essere negativa.");
        this.scortaMagazzino = scortaMagazzino; 
    }
    public int getScortaMagazzino() { return scortaMagazzino; }

    // --- GETTER E SETTER MULTI-TENANT ---
    public Long getIdBottega() { return idBottega; }
    public void setIdBottega(Long idBottega) { this.idBottega = idBottega; }

    // --- METODI DI BUSINESS ---
    public void riduciScorta(int quantitaComprata) {
        if (quantitaComprata <= 0) throw new IllegalArgumentException("La quantità da scalare deve essere maggiore di zero.");
        if (this.scortaMagazzino - quantitaComprata < 0) throw new IllegalStateException("Errore: Impossibile prelevare " + quantitaComprata + " pezzi.");
        this.scortaMagazzino -= quantitaComprata;
    }

    public void aggiungiScorta(int quantitaArrivata) {
        if (quantitaArrivata <= 0) throw new IllegalArgumentException("La quantità da aggiungere deve essere maggiore di zero.");
        this.scortaMagazzino += quantitaArrivata;
    }
}