package com.bottega.bottega_web.dto;



import java.util.List;

public class ScontrinoDTO {

    private String codiceTessera;
    private List<ElementoScontrinoDTO> elementi; 

    // Costruttore vuoto
    public ScontrinoDTO() {
    }

    // Getter e Setter
    public String getCodiceTessera() {
        return codiceTessera;
    }

    public void setCodiceTessera(String codiceTessera) {
        this.codiceTessera = codiceTessera;
    }

    public List<ElementoScontrinoDTO> getElementi() {
        return elementi;
    }

    public void setElementi(List<ElementoScontrinoDTO> elementi) {
        this.elementi = elementi;
    }
}