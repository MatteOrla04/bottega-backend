package com.bottega.bottega_web.model; 

public class NuovoUtenteRequest {
    private String nome;
    private String cognome;
    private String codiceTessera;
    private Double valoreIsee; // Usa Double o String in base a come lo vuoi gestire

    // Genera Getter e Setter
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getCodiceTessera() { return codiceTessera; }
    public void setCodiceTessera(String codiceTessera) { this.codiceTessera = codiceTessera; }

    public Double getValoreIsee() { return valoreIsee; }
    public void setValoreIsee(Double valoreIsee) { this.valoreIsee = valoreIsee; }
}