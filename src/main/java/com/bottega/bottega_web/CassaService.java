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

    // Usiamo @Transactional: o tutto il carrello va a buon fine, o si annulla tutto
    @Transactional
    public void processaScontrino(ScontrinoDTO scontrino) {
        System.out.println("--- INIZIO ELABORAZIONE SCONTRINO ---");

        // 1. Troviamo il cliente
        Beneficiario cliente = beneficiarioDao.cercaPerTessera(scontrino.getCodiceTessera());
        if (cliente == null) {
            throw new IllegalStateException("Errore: tessera " + scontrino.getCodiceTessera() + " non trovata");
        }

        int costoTotaleScontrino = 0;

        // 2. Cicliamo su ogni prodotto del carrello
        for (ElementoScontrinoDTO elemento : scontrino.getElementi()) {
            Prodotto articolo = prodottoDao.cercaPerCodiceBarre(elemento.getCodiceBarre());
            
            if (articolo == null) {
                throw new IllegalStateException("Errore: Prodotto " + elemento.getCodiceBarre() + " non trovato");
            }

            int quantita = elemento.getQuantita();
            
            // --- INIZIO MODIFICA MAGICA ---
            // Se il front-end ci passa un prezzo su misura (come per il prodotto libero), usiamo quello!
            // Altrimenti, usiamo il prezzo normale da magazzino.
            int costoRiga = 0;
            if (elemento.getPrezzoCustom() != null) {
                costoRiga = elemento.getPrezzoCustom() * quantita;
            } else {
                costoRiga = articolo.getPuntiCosto() * quantita;
            }
            // --- FINE MODIFICA MAGICA ---

            costoTotaleScontrino += costoRiga;

            // Riduciamo la scorta in memoria e aggiorniamo la tabella dei prodotti
            articolo.riduciScorta(quantita);
            prodottoDao.aggiornaScorta(articolo.getCodiceBarre(), articolo.getScortaMagazzino());

            // Salviamo la singola riga di transazione INCLUDENDO LE NOTE
            Transazione transazioneRiga = new Transazione(
                scontrino.getCodiceTessera(), 
                articolo.getCodiceBarre(), 
                quantita, 
                costoRiga, 
                elemento.getNote() // Aggiungiamo la descrizione testuale (es. "Zucchine")
            );
            transazioneDao.salvaTransazione(transazioneRiga);
        }

        // 3. Alla fine di tutto, scaliamo i punti totali all'utente in un colpo solo
        cliente.detraiPunti(costoTotaleScontrino);
        beneficiarioDao.aggiornaSaldoPunti(cliente.getCodiceTessera(), cliente.getSaldoPunti());

        System.out.println("Transazione completata con successo!");
        System.out.println("Saldo residuo di " + cliente.getNome() + ": " + cliente.getSaldoPunti());
        System.out.println("-------------------------------------\n");
    }
}