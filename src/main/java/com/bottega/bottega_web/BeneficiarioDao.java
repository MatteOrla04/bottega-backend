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
        int nucleo = b.getNumeroNucleoFamiliare();
        int puntiIniziali = (nucleo <= 1) ? 10 : 20 + ((nucleo - 2) * 5);
        b.setSaldoPunti(puntiIniziali);

        // AGGIUNTO id_bottega
        String sql = "INSERT INTO beneficiari (codice_tessera, nome, cognome, telefono, " +
        "cittadinanza, citta, indirizzo_abitazione, numero_civico, provincia, " +
        "numero_nucleo_familiare, valore_isee, saldo_punti, documento_base64, id_bottega) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
            pstmt.setString(1, b.getCodiceTessera());
            pstmt.setString(2, b.getNome());
            pstmt.setString(3, b.getCognome());
            pstmt.setString(4, b.getTelefono());
            pstmt.setString(5, b.getCittadinanza());
            pstmt.setString(6, b.getCitta());
            pstmt.setString(7, b.getIndirizzo_abitazione()); 
            pstmt.setString(8, b.getNumero_civico());           
            pstmt.setString(9, b.getProvincia());
            pstmt.setInt(10, b.getNumeroNucleoFamiliare());  
            pstmt.setString(11, b.getValoreIsee());
            pstmt.setInt(12, b.getSaldoPunti());
            pstmt.setString(13, b.getDocumentoBase64()); 
            pstmt.setLong(14, b.getIdBottega() != null ? b.getIdBottega() : 1L); // IL NUOVO CAMPO
    
            int righeInserite = pstmt.executeUpdate();
    
            if (righeInserite > 0) {
                System.out.println("✅ Inserimento completato! " + b.getNome() + " salvato nella Bottega " + b.getIdBottega());
            }
    
        } catch(SQLException e) {
            System.out.println("❌ ERRORE REALE DAL DB: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Errore Database: " + e.getMessage()); 
        }
    }

   // MODIFICA: Cerca solo i clienti della tua bottega
   public Beneficiario cercaPerTessera(String codiceCercato, Long idBottega) {
    String sql = "SELECT * FROM beneficiari WHERE codice_tessera = ? AND id_bottega = ?";
    Beneficiario beneficiarioTrovato = null;

    try (Connection conn = dataSource.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, codiceCercato);
        pstmt.setLong(2, idBottega);
        
        try (java.sql.ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                beneficiarioTrovato = new Beneficiario(
                    rs.getInt("id"), rs.getString("codice_tessera"), rs.getString("nome"),
                    rs.getString("cognome"), rs.getInt("saldo_punti"), rs.getString("telefono"),
                    rs.getString("citta"), rs.getString("cittadinanza"), null, 
                    rs.getString("indirizzo_abitazione"), rs.getString("numero_civico"),
                    rs.getString("provincia"), rs.getInt("numero_nucleo_familiare"), rs.getString("valore_isee")
                );
                beneficiarioTrovato.setDocumentoBase64(rs.getString("documento_base64"));
                beneficiarioTrovato.setIdBottega(rs.getLong("id_bottega"));
            }
        }
    } catch (SQLException e) {
        System.out.println("❌ Errore durante la ricerca nel database");
        e.printStackTrace();
    }
    return beneficiarioTrovato;
}

public void aggiornaSaldoPunti(String codiceTessera, int nuovoSaldo, Long idBottega){
    String sql= "UPDATE beneficiari SET saldo_punti = ? WHERE codice_tessera = ? AND id_bottega = ?";
    try(Connection conn = dataSource.getConnection();
        PreparedStatement psmt=conn.prepareStatement(sql)){
        psmt.setInt(1, nuovoSaldo);
        psmt.setString(2, codiceTessera);
        psmt.setLong(3, idBottega);
        psmt.executeUpdate();
    }catch(SQLException e){
        e.printStackTrace();
    }
}

public void eliminaBeneficiario(String codiceTessera, Long idBottega) {
    String sql = "DELETE FROM beneficiari WHERE codice_tessera = ? AND id_bottega = ?";
    try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, codiceTessera);
        pstmt.setLong(2, idBottega);
        pstmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

public List<Beneficiario> trovaTutti(Long idBottega) {
    String sql = "SELECT * FROM beneficiari WHERE id_bottega = ?";
    List<Beneficiario> lista = new ArrayList<>();
    try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setLong(1, idBottega);
        try(java.sql.ResultSet rs = pstmt.executeQuery()){
            while (rs.next()) {
                Beneficiario b = new Beneficiario(
                    rs.getInt("id"), rs.getString("codice_tessera"), rs.getString("nome"), rs.getString("cognome"),
                    rs.getInt("saldo_punti"), rs.getString("telefono"), rs.getString("citta"), rs.getString("cittadinanza"),
                    null, rs.getString("indirizzo_abitazione"), rs.getString("numero_civico"), rs.getString("provincia"),
                    rs.getInt("numero_nucleo_familiare"), rs.getString("valore_isee")
                );
                b.setIdBottega(rs.getLong("id_bottega"));
                lista.add(b);
            }
        }
    } catch (SQLException e) { e.printStackTrace(); }
    return lista;
}

public void eliminaDocumento(String codiceTessera, Long idBottega) {
    String sql = "UPDATE beneficiari SET documento_base64 = NULL WHERE codice_tessera = ? AND id_bottega = ?";
    try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, codiceTessera);
        pstmt.setLong(2, idBottega);
        pstmt.executeUpdate();
    } catch (SQLException e) { e.printStackTrace(); }
}

public void resetPuntiSettimanale(Long idBottega) {
    String sql = "UPDATE beneficiari SET saldo_punti = CASE " +
                 "WHEN numero_nucleo_familiare <= 1 THEN 10 ELSE 20 + ((numero_nucleo_familiare - 2) * 5) END " +
                 "WHERE id_bottega = ?";
    try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setLong(1, idBottega);
        pstmt.executeUpdate();
    } catch (SQLException e) { e.printStackTrace(); }
}

// NUOVO METODO: Usato solo dal Robot notturno per aggiornare tutte le botteghe insieme
    public void resetPuntiSettimanaleGlobale() {
        String sql = "UPDATE beneficiari SET saldo_punti = CASE " +
                     "WHEN numero_nucleo_familiare <= 1 THEN 10 ELSE 20 + ((numero_nucleo_familiare - 2) * 5) END";
        try (java.sql.Connection conn = dataSource.getConnection(); 
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int righeAggiornate = pstmt.executeUpdate();
            System.out.println("RESET ESEGUITO: Punti ricaricati per " + righeAggiornate + " beneficiari su tutto il server.");
        } catch (java.sql.SQLException e) { 
            e.printStackTrace(); 
        }
    }
}