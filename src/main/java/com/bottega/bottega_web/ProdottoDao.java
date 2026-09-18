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
        String sql= "INSERT INTO prodotti (codice_barre, descrizione, punti_costo, scorta_magazzino) VALUES (?, ?, ?, ?)";

        try(Connection conn = dataSource.getConnection();
            PreparedStatement psmt = conn.prepareStatement(sql)){

            psmt.setString(1, p.getCodiceBarre());
            psmt.setString(2, p.getDescrizione());
            psmt.setInt(3, p.getPuntiCosto());
            psmt.setInt(4, p.getScortaMagazzino());

            int righeInserite = psmt.executeUpdate();
            if(righeInserite > 0){
                System.out.println("Prodotto inserito! "+ p.getDescrizione()+ " (Scorta "+ p.getScortaMagazzino()+ ") aggiunto al magazzino");
            }

        }catch(SQLException e){
            System.out.println("Errore durante l'inserimento del prodotto nel database");
            e.printStackTrace();
        }
    }

    public Prodotto cercaPerCodiceBarre(String codiceCercato){
        String sql= "SELECT * FROM prodotti WHERE codice_barre = ?";
        Prodotto prodottoTrovato = null;

        try(Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, codiceCercato);

            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    prodottoTrovato = new Prodotto(
                        rs.getInt("id"),
                        rs.getString("codice_barre"),
                        rs.getString("descrizione"),
                        rs.getInt("punti_costo"),
                        rs.getInt("scorta_magazzino")
                    );
                }
            }

        }catch(SQLException e){
            System.out.println("Errore durante la ricerca del prodotto");
            e.printStackTrace();
        }

        return prodottoTrovato;
    }

    public void aggiornaScorta(String codiceBarre, int nuovaScorta){
        String sql= "UPDATE prodotti SET scorta_magazzino = ? WHERE codice_barre = ?";

        try(Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1, nuovaScorta);
            pstmt.setString(2, codiceBarre);

            int righeModificate = pstmt.executeUpdate();
            if(righeModificate > 0){
                System.out.println("Magazzino aggiornato! Nuova scorta: "+ nuovaScorta);
            }else{
                System.out.println("Nessun aggiornamento. Sicuro che il codice a barre sia corretto o esista?");
            }
        }catch(SQLException e){
            System.out.println("Errore durante l'aggiornamento");
            e.printStackTrace();
        }
    }

    // 1. NUOVO METODO: Estrae tutto il listino (scritto nel tuo stile JDBC)
    public List<Prodotto> trovaTutti() {
        String sql = "SELECT * FROM prodotti"; 
        List<Prodotto> listaInventario = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Prodotto p = new Prodotto(
                    rs.getInt("id"),
                    rs.getString("codice_barre"),
                    rs.getString("descrizione"),
                    rs.getInt("punti_costo"),
                    rs.getInt("scorta_magazzino")
                );
                listaInventario.add(p);
            }

        } catch (SQLException e) {
            System.out.println("Errore durante il caricamento dell'inventario");
            e.printStackTrace();
        }

        return listaInventario;
    }
}