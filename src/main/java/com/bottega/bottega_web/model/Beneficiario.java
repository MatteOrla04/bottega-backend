package com.bottega.bottega_web.model;

public class Beneficiario {
    
    private Integer id;
    private String codiceTessera;
    private String nome;
    private String cognome;
    private Integer saldoPunti = 0; 
    private String numero_civico; 
    private Integer numeroNucleoFamiliare = 1; 
    private String telefono;
    private String citta;
    private String cittadinanza;
    private String residenza;
    private String indirizzo_abitazione;
    private String provincia; 
    private String valoreIsee; 
    private String documentoBase64;

    // IL NUOVO CAMPO MULTI-TENANT
    private Long idBottega;

    public Beneficiario(){}

    public Beneficiario(Integer id, String codiceTessera, String nome, String cognome, Integer saldoPunti, 
                        String telefono, String citta, String cittadinanza, String residenza, 
                        String indirizzo_abitazione, String numero_civico, String provincia, 
                        Integer numeroNucleoFamiliare, String valoreIsee) {
        this.id = id;
        this.codiceTessera = codiceTessera;
        this.nome = nome;
        this.cognome = cognome;
        if(saldoPunti != null && saldoPunti < 0) throw new IllegalArgumentException("Il saldo punti iniziale non può essere negativo");
        this.saldoPunti = saldoPunti != null ? saldoPunti : 0;
        this.telefono = telefono;
        this.citta = citta;
        this.cittadinanza = cittadinanza;
        this.residenza = residenza;
        this.indirizzo_abitazione = indirizzo_abitazione;
        this.numero_civico = numero_civico; 
        this.provincia = provincia;
        this.numeroNucleoFamiliare = numeroNucleoFamiliare != null ? numeroNucleoFamiliare : 1;
        this.valoreIsee = valoreIsee;
    }

    // --- GETTER E SETTER ---
    public void setId(Integer id){ this.id = id; }
    public Integer getId(){ return id; }

    public void setCodiceTessera(String codiceTessera){ this.codiceTessera = codiceTessera; }
    public String getCodiceTessera(){ return codiceTessera; }

    public void setNome(String nome){ this.nome = nome; }
    public String getNome(){ return nome; }

    public void setCognome(String cognome){ this.cognome = cognome; }
    public String getCognome(){ return cognome; }

    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getTelefono() { return telefono; }

    public void setCitta(String citta) { this.citta = citta; }
    public String getCitta() { return citta; }

    public void setCittadinanza(String cittadinanza) { this.cittadinanza = cittadinanza; }
    public String getCittadinanza() { return cittadinanza; }

    public void setResidenza(String residenza) { this.residenza = residenza; }
    public String getResidenza() { return residenza; }

    public void setIndirizzo_abitazione(String indirizzo_abitazione) { this.indirizzo_abitazione = indirizzo_abitazione; }
    public String getIndirizzo_abitazione() { return indirizzo_abitazione; }

    public void setProvincia(String provincia) { this.provincia = provincia; }
    public String getProvincia() { return provincia; }

    public void setValoreIsee(String valoreIsee) { this.valoreIsee = valoreIsee; }
    public String getValoreIsee() { return valoreIsee; }

    public void setNumero_civico(String numero_civico) { this.numero_civico = numero_civico; }
    public String getNumero_civico() { return this.numero_civico; }

    public void setNumeroNucleoFamiliare(Integer numeroNucleoFamiliare) { this.numeroNucleoFamiliare = numeroNucleoFamiliare; }
    public int getNumeroNucleoFamiliare() { return this.numeroNucleoFamiliare != null ? this.numeroNucleoFamiliare : 1; }

    public void setSaldoPunti(Integer saldoPunti) {
        if(saldoPunti != null && saldoPunti < 0) throw new IllegalArgumentException("Il saldo non può essere negativo");
        this.saldoPunti = saldoPunti;
    }
    public int getSaldoPunti() { return this.saldoPunti != null ? this.saldoPunti : 0; }

    public String getDocumentoBase64() { return documentoBase64; }
    public void setDocumentoBase64(String documentoBase64) { this.documentoBase64 = documentoBase64; }

    // --- GETTER E SETTER MULTI-TENANT ---
    public Long getIdBottega() { return idBottega; }
    public void setIdBottega(Long idBottega) { this.idBottega = idBottega; }

    // --- METODI BUSINESS ---
    public void detraiPunti(int puntiDaDetrarre){
        if(puntiDaDetrarre < 0) throw new IllegalArgumentException("Impossibile detrarre un ammontare negativo");
        if(getSaldoPunti() - puntiDaDetrarre < 0) throw new IllegalStateException("Saldo punti insufficiente");
        this.saldoPunti -= puntiDaDetrarre;
    }

    public void ricaricaPunti(int puntiDaAggiungere){
        if(puntiDaAggiungere < 0) throw new IllegalArgumentException("I punti da aggiungere non possono essere negativi");
        this.saldoPunti = getSaldoPunti() + puntiDaAggiungere; 
    }
}