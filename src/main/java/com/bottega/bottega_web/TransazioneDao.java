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
        // AGGIUNTO id_bottega ALLA QUERY
        String sql = "INSERT INTO transazioni (codice_tessera, codice_barre, quantita, punti_spesi, data_operazione, note, id_bottega) VALUES (?, ?, ?, ?, NOW(), ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, t.getCodiceTessera());
            pstmt.setString(2, t.getCodiceBarre());
            pstmt.setInt(3, t.getQuantita());
            pstmt.setInt(4, t.getPuntiSpesi());
            pstmt.setString(5, t.getNote()); 
            pstmt.setLong(6, t.getIdBottega() != null ? t.getIdBottega() : 1L); // LA CHIAVE MULTI-TENANT

            pstmt.executeUpdate();
            System.out.println("Scontrino archiviato nello storico per la Bottega " + t.getIdBottega());

        } catch (SQLException e) {
            System.out.println("Errore durante il salvataggio dello scontrino nel database");
            e.printStackTrace();
        }
    }

    // AGGIUNTO IL PARAMETRO idBottega E IL FILTRO NELLA QUERY
    public List<Transazione> trovaStoricoPerTessera(String tessera, Long idBottega) {
        String sql = "SELECT * FROM transazioni WHERE codice_tessera = ? AND id_bottega = ? ORDER BY data_operazione DESC";
        List<Transazione> storico = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, tessera);
            pstmt.setLong(2, idBottega); // Filtro sicurezza
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    storico.add(new Transazione(
                        rs.getString("codice_tessera"),
                        rs.getString("codice_barre"),
                        rs.getInt("quantita"),
                        rs.getInt("punti_spesi"),
                        rs.getString("data_operazione"), 
                        rs.getString("note"),             
                        rs.getLong("id_bottega") // 7° parametro per il nuovo costruttore
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