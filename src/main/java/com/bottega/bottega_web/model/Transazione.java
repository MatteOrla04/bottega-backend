package com.bottega.bottega_web.model;

public class Transazione {
    
    private String codiceTessera;
    private String codiceBarre;
    private int quantita;
    private int puntiSpesi;
    private String dataOperazione;
    private String note;

    // IL NUOVO CAMPO MULTI-TENANT
    private Long idBottega;

    // COSTRUTTORE 1: Usato per salvare NUOVI scontrini
    public Transazione(String codiceTessera, String codiceBarre, int quantita, int puntiSpesi, String note, Long idBottega){
        this.codiceTessera = codiceTessera;
        this.codiceBarre = codiceBarre;
        this.quantita = quantita;
        this.puntiSpesi = puntiSpesi;
        this.note = note;
        this.idBottega = idBottega;
    }

    // COSTRUTTORE 2: Usato per leggere i VECCHI scontrini
    public Transazione(String codiceTessera, String codiceBarre, int quantita, int puntiSpesi, String dataOperazione, String note, Long idBottega){
        this.codiceTessera = codiceTessera;
        this.codiceBarre = codiceBarre;
        this.quantita = quantita;
        this.puntiSpesi = puntiSpesi;
        this.dataOperazione = dataOperazione;
        this.note = note;
        this.idBottega = idBottega;
    }

    public String getCodiceTessera() { return codiceTessera; }
    public String getCodiceBarre() { return codiceBarre; }
    public int getQuantita() { return quantita; }
    public int getPuntiSpesi() { return puntiSpesi; }
    public String getDataOperazione() { return dataOperazione; }
    public String getNote() { return note; }
    
    // GETTER E SETTER MULTI-TENANT
    public Long getIdBottega() { return idBottega; }
    public void setIdBottega(Long idBottega) { this.idBottega = idBottega; }
}