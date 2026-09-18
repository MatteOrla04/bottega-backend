package com.bottega.bottega_web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList; 
import java.util.List;      
import javax.sql.DataSource; 
import org.springframework.stereotype.Repository; 

import com.bottega.bottega_web.model.Beneficiario; 

@Repository 
public class BeneficiarioDao {

    private final DataSource dataSource;

    public BeneficiarioDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void inserisciBeneficiario(Beneficiario b){
        
        // --- CALCOLO AUTOMATICO PUNTI ---
        int nucleo = b.getNumeroNucleoFamiliare();
        int puntiIniziali = (nucleo <= 1) ? 10 : 20 + ((nucleo - 2) * 5);
        b.setSaldoPunti(puntiIniziali);

        String sql = "INSERT INTO beneficiari (codice_tessera, nome, cognome, telefono, " +
        "cittadinanza, citta, indirizzo_abitazione, numero_civico, provincia, " +
        "numero_nucleo_familiare, valore_isee, saldo_punti, documento_base64) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
            pstmt.setString(1, b.getCodiceTessera());
            pstmt.setString(2, b.getNome());
            pstmt.setString(3, b.getCognome());
            pstmt.setString(4, b.getTelefono());
            pstmt.setString(5, b.getCittadinanza());
            pstmt.setString(6, b.getCitta());
            pstmt.setString(7, b.getIndirizzo_abitazione()); 
            // 🔥 QUI: Ora usa setString per il civico!
            pstmt.setString(8, b.getNumero_civico());           
            pstmt.setString(9, b.getProvincia());
            pstmt.setInt(10, b.getNumeroNucleoFamiliare());  
            pstmt.setString(11, b.getValoreIsee());
            pstmt.setInt(12, b.getSaldoPunti());
            pstmt.setString(13, b.getDocumentoBase64()); 
    
            int righeInserite = pstmt.executeUpdate();
    
            if (righeInserite > 0) {
                System.out.println("✅ Inserimento completato! " + b.getNome() + " " + b.getCognome() + " salvato con documento.");
            }
    
        } catch(SQLException e) {
            System.out.println("❌ ERRORE REALE DAL DB: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Errore Database: " + e.getMessage()); 
        }
    }

   public Beneficiario cercaPerTessera(String codiceCercato) {
        
    String sql = "SELECT * FROM beneficiari WHERE codice_tessera = ?";
    Beneficiario beneficiarioTrovato = null;

    try (Connection conn = dataSource.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, codiceCercato);
        
        try (java.sql.ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                beneficiarioTrovato = new Beneficiario(
                    rs.getInt("id"),
                    rs.getString("codice_tessera"),
                    rs.getString("nome"),
                    rs.getString("cognome"),
                    rs.getInt("saldo_punti"),
                    rs.getString("telefono"),
                    rs.getString("citta"),
                    rs.getString("cittadinanza"),
                    null, 
                    rs.getString("indirizzo_abitazione"),
                   
                    rs.getString("numero_civico"),
                    rs.getString("provincia"),
                    rs.getInt("numero_nucleo_familiare"),
                    rs.getString("valore_isee")
                );
                beneficiarioTrovato.setDocumentoBase64(rs.getString("documento_base64"));
            }
        }
        
    } catch (SQLException e) {
        System.out.println("❌ Errore durante la ricerca nel database");
        e.printStackTrace();
    }
    
    return beneficiarioTrovato;
}

public void aggiornaSaldoPunti(String codiceTessera, int nuovoSaldo){
    String sql= "UPDATE beneficiari SET saldo_punti = ? WHERE codice_tessera = ?";

    try(Connection conn = dataSource.getConnection();
        PreparedStatement psmt=conn.prepareStatement(sql)){

        psmt.setInt(1, nuovoSaldo);
        psmt.setString(2, codiceTessera);

        int righeModificate=psmt.executeUpdate();

        if(righeModificate>0){
            System.out.println("Database aggiornato con successo: il nuovo saldo è "+ nuovoSaldo);
        }else{
            System.out.println("Nessun Aggiornamento.Sicuro che la tessera esista?");
        }
    }catch(SQLException e){
        System.out.println("Errore durante l'aggiornamento del database");
        e.printStackTrace();
    }
}

public void eliminaBeneficiario(String codiceTessera) {
        
    String sql = "DELETE FROM beneficiari WHERE codice_tessera = ?";

    try (Connection conn = dataSource.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, codiceTessera);

        int righeEliminate = pstmt.executeUpdate();

        if (righeEliminate > 0) {
            System.out.println("🗑️ Eliminazione completata! L'utente con tessera " + codiceTessera + " è stato rimosso dal database.");
        } else {
            System.out.println("⚠️ Nessuna eliminazione effettuata. Sicuro che la tessera esista?");
        }

    } catch (SQLException e) {
        System.out.println(" Errore durante l'eliminazione nel database");
        e.printStackTrace();
    }
}

public List<Beneficiario> trovaTutti() {
   
    String sql = "SELECT id, codice_tessera, nome, cognome, saldo_punti, telefono, citta, cittadinanza, indirizzo_abitazione, numero_civico, provincia, numero_nucleo_familiare, valore_isee FROM beneficiari";
    List<Beneficiario> lista = new ArrayList<>();

    try (Connection conn = dataSource.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         java.sql.ResultSet rs = pstmt.executeQuery()) {
        
        while (rs.next()) {
            Beneficiario b = new Beneficiario(
                rs.getInt("id"),
                rs.getString("codice_tessera"),
                rs.getString("nome"),
                rs.getString("cognome"),
                rs.getInt("saldo_punti"),
                rs.getString("telefono"),
                rs.getString("citta"),
                rs.getString("cittadinanza"),
                null, // Nessuna immagine caricata nella lista generale per risparmiare memoria
                rs.getString("indirizzo_abitazione"),
                rs.getString("numero_civico"),
                rs.getString("provincia"),
                rs.getInt("numero_nucleo_familiare"),
                rs.getString("valore_isee")
            );
            // Lasciamo documentoBase64 a null o vuoto nella lista
            
            lista.add(b);
        }
    } catch (SQLException e) {
        System.out.println("Errore durante il recupero di tutti i beneficiari");
        e.printStackTrace();
    }
    return lista;
}
public void eliminaDocumento(String codiceTessera) {
    String sql = "UPDATE beneficiari SET documento_base64 = NULL WHERE codice_tessera = ?";

    try (Connection conn = dataSource.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, codiceTessera);
        pstmt.executeUpdate();
        System.out.println("Documento rimosso per la tessera " + codiceTessera);

    } catch (SQLException e) {
        System.out.println("Errore durante l'eliminazione del documento");
        e.printStackTrace();
    }
}

    public void resetPuntiSettimanale() {
        String sql = "UPDATE beneficiari SET saldo_punti = CASE " +
                     "WHEN numero_nucleo_familiare <= 1 THEN 10 " +
                     "ELSE 20 + ((numero_nucleo_familiare - 2) * 5) END";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            int righeAggiornate = pstmt.executeUpdate();
            System.out.println("RESET ESEGUITO: Punti ricaricati per " + righeAggiornate + " beneficiari.");
            
        } catch (SQLException e) {
            System.out.println("Errore durante il reset settimanale dei punti!");
            e.printStackTrace();
        }
    }
}