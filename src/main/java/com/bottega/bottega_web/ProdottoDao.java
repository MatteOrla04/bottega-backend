package com.bottega.bottega_web;

import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;
import com.bottega.bottega_web.model.Prodotto;

@Repository
public class ProdottoDao {

    private final DataSource dataSource;

    public ProdottoDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void inserisciProdotto(Prodotto p){
        // Aggiunto id_bottega
        String sql= "INSERT INTO prodotti (codice_barre, descrizione, punti_costo, scorta_magazzino, id_bottega) VALUES (?, ?, ?, ?, ?)";

        try(Connection conn = dataSource.getConnection();
            PreparedStatement psmt = conn.prepareStatement(sql)){

            psmt.setString(1, p.getCodiceBarre());
            psmt.setString(2, p.getDescrizione());
            psmt.setInt(3, p.getPuntiCosto());
            psmt.setInt(4, p.getScortaMagazzino());
            psmt.setLong(5, p.getIdBottega() != null ? p.getIdBottega() : 1L);

            int righeInserite = psmt.executeUpdate();
            if(righeInserite > 0){
                System.out.println("Prodotto inserito! "+ p.getDescrizione()+ " aggiunto al magazzino della Bottega " + p.getIdBottega());
            }

        }catch(SQLException e){
            System.out.println("Errore durante l'inserimento del prodotto nel database");
            e.printStackTrace();
        }
    }

    // MODIFICA: Ora cerca il codice a barre SOLO dentro la bottega specifica
    public Prodotto cercaPerCodiceBarre(String codiceCercato, Long idBottega){
        String sql= "SELECT * FROM prodotti WHERE codice_barre = ? AND id_bottega = ?";
        Prodotto prodottoTrovato = null;

        try(Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, codiceCercato);
            pstmt.setLong(2, idBottega);

            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    prodottoTrovato = new Prodotto(
                        rs.getInt("id"),
                        rs.getString("codice_barre"),
                        rs.getString("descrizione"),
                        rs.getInt("punti_costo"),
                        rs.getInt("scorta_magazzino")
                    );
                    prodottoTrovato.setIdBottega(rs.getLong("id_bottega"));
                }
            }

        }catch(SQLException e){
            System.out.println("Errore durante la ricerca del prodotto");
            e.printStackTrace();
        }

        return prodottoTrovato;
    }

    // MODIFICA: Aggiorna la scorta SOLO se il prodotto appartiene alla tua bottega
    public void aggiornaScorta(String codiceBarre, int nuovaScorta, Long idBottega){
        String sql= "UPDATE prodotti SET scorta_magazzino = ? WHERE codice_barre = ? AND id_bottega = ?";

        try(Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1, nuovaScorta);
            pstmt.setString(2, codiceBarre);
            pstmt.setLong(3, idBottega);

            pstmt.executeUpdate();
        }catch(SQLException e){
            System.out.println("Errore durante l'aggiornamento");
            e.printStackTrace();
        }
    }

    // MODIFICA: Scarica tutto il listino, ma SOLO della tua bottega
    public List<Prodotto> trovaTutti(Long idBottega) {
        String sql = "SELECT * FROM prodotti WHERE id_bottega = ?"; 
        List<Prodotto> listaInventario = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
             pstmt.setLong(1, idBottega);
             
             try(java.sql.ResultSet rs = pstmt.executeQuery()){
                 while (rs.next()) {
                     Prodotto p = new Prodotto(
                         rs.getInt("id"),
                         rs.getString("codice_barre"),
                         rs.getString("descrizione"),
                         rs.getInt("punti_costo"),
                         rs.getInt("scorta_magazzino")
                     );
                     p.setIdBottega(rs.getLong("id_bottega"));
                     listaInventario.add(p);
                 }
             }

        } catch (SQLException e) {
            System.out.println("Errore durante il caricamento dell'inventario");
            e.printStackTrace();
        }

        return listaInventario;
    }
}