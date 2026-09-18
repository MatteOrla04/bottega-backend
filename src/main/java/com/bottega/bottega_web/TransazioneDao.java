package com.bottega.bottega_web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;

import com.bottega.bottega_web.model.Transazione;

@Repository
public class TransazioneDao {

    private final DataSource dataSource;

    public TransazioneDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void salvaTransazione(Transazione t) {
        // Aggiungiamo anche le Note al salvataggio
        String sql = "INSERT INTO transazioni (codice_tessera, codice_barre, quantita, punti_spesi, data_operazione, note) VALUES (?, ?, ?, ?, NOW(), ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, t.getCodiceTessera());
            pstmt.setString(2, t.getCodiceBarre());
            pstmt.setInt(3, t.getQuantita());
            pstmt.setInt(4, t.getPuntiSpesi());
            pstmt.setString(5, t.getNote()); 

            pstmt.executeUpdate();
            System.out.println("Scontrino archiviato nello storico con Data, Ora e Note!");

        } catch (SQLException e) {
            System.out.println("Errore durante il salvataggio dello scontrino nel database");
            e.printStackTrace();
        }
    }

    public List<Transazione> trovaStoricoPerTessera(String tessera) {
        String sql = "SELECT * FROM transazioni WHERE codice_tessera = ? ORDER BY data_operazione DESC";
        List<Transazione> storico = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, tessera);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    storico.add(new Transazione(
                        rs.getString("codice_tessera"),
                        rs.getString("codice_barre"),
                        rs.getInt("quantita"),
                        rs.getInt("punti_spesi"),
                        rs.getString("data_operazione"), // 5° parametro (La Data torna al suo posto!)
                        rs.getString("note")             // 6° parametro (Le Note per il prodotto libero)
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Errore durante la lettura dello storico");
            e.printStackTrace();
        }
        return storico;
    }
}