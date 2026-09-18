package com.bottega.bottega_web;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bottega.bottega_web.model.Beneficiario;
import com.bottega.bottega_web.model.Prodotto;
import com.bottega.bottega_web.model.Transazione;
import com.bottega.bottega_web.dto.ScontrinoDTO;
import com.bottega.bottega_web.dto.ElementoScontrinoDTO;

@Service 
public class CassaService {

    private final BeneficiarioDao beneficiarioDao;
    private final ProdottoDao prodottoDao;
    private final TransazioneDao transazioneDao;

    public CassaService(BeneficiarioDao beneficiarioDao, ProdottoDao prodottoDao, TransazioneDao transazioneDao){
        this.beneficiarioDao = beneficiarioDao;
        this.prodottoDao = prodottoDao;
        this.transazioneDao = transazioneDao;
    }

    @Transactional
    // AGGIUNTO IL PARAMETRO idBottega
    public void processaScontrino(ScontrinoDTO scontrino, Long idBottega) {
        System.out.println("--- INIZIO ELABORAZIONE SCONTRINO (Bottega " + idBottega + ") ---");

        // 1. Troviamo il cliente DELLA TUA BOTTEGA
        Beneficiario cliente = beneficiarioDao.cercaPerTessera(scontrino.getCodiceTessera(), idBottega);
        if (cliente == null) {
            throw new IllegalStateException("Errore: tessera " + scontrino.getCodiceTessera() + " non trovata nella tua bottega");
        }

        int costoTotaleScontrino = 0;

        // 2. Cicliamo su ogni prodotto del carrello
        for (ElementoScontrinoDTO elemento : scontrino.getElementi()) {
            
            // Cerchiamo il prodotto NEL TUO MAGAZZINO
            Prodotto articolo = prodottoDao.cercaPerCodiceBarre(elemento.getCodiceBarre(), idBottega);
            
            if (articolo == null) {
                throw new IllegalStateException("Errore: Prodotto " + elemento.getCodiceBarre() + " non trovato nel tuo magazzino");
            }

            int quantita = elemento.getQuantita();
            
            int costoRiga = 0;
            if (elemento.getPrezzoCustom() != null) {
                costoRiga = elemento.getPrezzoCustom() * quantita;
            } else {
                costoRiga = articolo.getPuntiCosto() * quantita;
            }

            costoTotaleScontrino += costoRiga;

            // Riduciamo la scorta e aggiorniamo il TUO magazzino
            articolo.riduciScorta(quantita);
            prodottoDao.aggiornaScorta(articolo.getCodiceBarre(), articolo.getScortaMagazzino(), idBottega);

            // Salviamo lo scontrino NEL TUO STORICO
            Transazione transazioneRiga = new Transazione(
                scontrino.getCodiceTessera(), 
                articolo.getCodiceBarre(), 
                quantita, 
                costoRiga, 
                elemento.getNote(), 
                idBottega // PASSIAMO LA CHIAVE!
            );
            transazioneDao.salvaTransazione(transazioneRiga);
        }

        // 3. Scaliamo i punti al TUO cliente
        cliente.detraiPunti(costoTotaleScontrino);
        beneficiarioDao.aggiornaSaldoPunti(cliente.getCodiceTessera(), cliente.getSaldoPunti(), idBottega);

        System.out.println("Transazione completata con successo!");
        System.out.println("Saldo residuo di " + cliente.getNome() + ": " + cliente.getSaldoPunti());
        System.out.println("-------------------------------------\n");
    }
}