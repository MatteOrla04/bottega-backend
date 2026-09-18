package com.bottega.bottega_web.dto;

public class ElementoScontrinoDTO {
    
    private String codiceBarre;
    private int quantita;
    private String note;           // La descrizione ("Zucchine")
    private Integer prezzoCustom;  // Il prezzo scelto a mano in cassa

    public ElementoScontrinoDTO() {}

    public String getCodiceBarre() { return codiceBarre; }
    public void setCodiceBarre(String codiceBarre) { this.codiceBarre = codiceBarre; }

    public int getQuantita() { return quantita; }
    public void setQuantita(int quantita) { this.quantita = quantita; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Integer getPrezzoCustom() { return prezzoCustom; }
    public void setPrezzoCustom(Integer prezzoCustom) { this.prezzoCustom = prezzoCustom; }
}