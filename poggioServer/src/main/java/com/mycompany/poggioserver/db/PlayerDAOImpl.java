package com.mycompany.poggioserver.db;

import com.mycompany.poggioserver.model.PlayerRecord;
import java.sql.*; // Manteniamo l'import generico
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Strix89
 */
public class PlayerDAOImpl implements PlayerDAO {

    private static final Logger logger = LoggerFactory.getLogger(PlayerDAOImpl.class);

    @Override
    public void addPlayer(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username non può essere vuoto.");
        }

        String sql = "INSERT INTO players (username) VALUES (?)";

        // Try-with-resources gestisce conn e pstmt
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creazione giocatore fallita, nessuna riga aggiunta.");
            }
            logger.info("Giocatore '{}' aggiunto con successo.", username);

        } catch (SQLException e) {
            // Gestione errore duplicato e altri errori SQL (come prima)
            if ("23505".equals(e.getSQLState())) { // Codice errore H2 per duplicato
                 logger.warn("Tentativo di inserire username duplicato: {}", username);
                 throw new SQLException("Username '" + username + "' esiste già.", e.getSQLState(), e);
             } else {
                 logger.error("Errore SQL durante l'aggiunta del giocatore '{}': {}", username, e.getMessage(), e);
                 throw e; // Rilancia l'eccezione originale
             }
        }
        // Non serve blocco finally per chiudere le risorse
    }

    @Override
    public PlayerRecord getPlayer(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username non può essere vuoto per la ricerca.");
        }

        String sql = "SELECT username, data, ora, percorso_file_log, durata_ms FROM players WHERE username = ?";
        PlayerRecord player = null;

        // Try-with-resources gestisce conn, pstmt, e rs
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) { // Anche ResultSet è AutoCloseable

                if (rs.next()) {
                    Date data = rs.getDate("data");
                    Time ora = rs.getTime("ora");
                    String logPath = rs.getString("percorso_file_log");
                    long durataValue = rs.getLong("durata_ms");
                    Long durataMs = rs.wasNull() ? null : durataValue;

                    player = new PlayerRecord(
                        rs.getString("username"),
                        data,
                        ora,
                        logPath,
                        durataMs
                    );
                    logger.debug("Giocatore trovato: {}", player);
                } else {
                    logger.debug("Giocatore con username '{}' non trovato.", username);
                }
            } // rs viene chiuso qui
        } catch (SQLException e) {
            logger.error("Errore SQL durante il recupero del giocatore '{}': {}", username, e.getMessage(), e);
            throw e;
        } // conn e pstmt vengono chiusi qui
        return player;
    }

    @Override
    public boolean recordVictory(String username, Date data, Time ora, String logFilePath, Long durataMs) throws SQLException {
         if (username == null || username.trim().isEmpty()) {
             throw new IllegalArgumentException("Username non può essere vuoto per registrare la vittoria.");
         }

        String sql = "UPDATE players SET data = ?, ora = ?, percorso_file_log = ?, durata_ms = ? WHERE username = ?";
        boolean updated = false;

        // Try-with-resources gestisce conn e pstmt
        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, data);
            pstmt.setTime(2, ora);
            pstmt.setString(3, logFilePath);
            pstmt.setObject(4, durataMs); // Indice 4 per durata_ms
            pstmt.setString(5, username);  // Indice 5 per username nel WHERE

            int affectedRows = pstmt.executeUpdate();
            updated = (affectedRows > 0);

            if (updated) {
                 logger.info("Vittoria registrata con successo per l'utente '{}'.", username);
             } else {
                 logger.warn("Nessun utente trovato con username '{}' per registrare la vittoria.", username);
             }

        } catch (SQLException e) {
            logger.error("Errore SQL durante la registrazione della vittoria per '{}': {}", username, e.getMessage(), e);
            throw e;
        } // conn e pstmt vengono chiusi qui
        return updated;
    }
}