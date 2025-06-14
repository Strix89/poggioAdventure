package com.mycompany.poggioserver.db;

import com.mycompany.poggioserver.model.PlayerRecord;
import com.mycompany.poggioserver.resources.RankingEntryDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementazione del DAO per la gestione dei giocatori nel database.
 * Gestisce le operazioni CRUD e la classifica giocatori.
 * 
 * @author Strix89
 */
public class PlayerDAOImpl implements PlayerDAO {

    private static final Logger logger = LoggerFactory.getLogger(PlayerDAOImpl.class);

    /**
     * Aggiunge un nuovo giocatore con solo lo username.
     * Gli altri campi rimangono null fino alla prima vittoria.
     */
    @Override
    public void addPlayer(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username non può essere vuoto.");
        }

        String sql = "INSERT INTO players (username) VALUES (?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creazione giocatore fallita, nessuna riga aggiunta (username: " + username + ").");
            }
            logger.info("Giocatore '{}' aggiunto con successo", username);

        } catch (SQLException e) {
            // Gestisce username duplicato (violazione chiave primaria)
            if ("23505".equals(e.getSQLState())) {
                logger.warn("Tentativo di inserire username duplicato: {}", username);
                throw new SQLException("Username '" + username + "' esiste già.", e.getSQLState(), e);
            } else {
                logger.error("Errore SQL durante l'aggiunta del giocatore '{}': {}", username, e.getMessage(), e);
                throw e;
            }
        }
    }

    @Override
    public List<PlayerRecord> getAllPlayers() throws SQLException {
        String sql = "SELECT * FROM players";
        List<PlayerRecord> players = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                PlayerRecord player = new PlayerRecord(
                    rs.getString("username"),
                    rs.getDate("data"),
                    rs.getTime("ora"),
                    rs.getString("percorso_file_log"),
                    rs.getObject("durata_ms", Long.class),
                    rs.getObject("punteggio", Integer.class)
                );
                players.add(player);
            }
        }
        return players;
    }

    /**
     * Recupera un giocatore dal database tramite username.
     * @return PlayerRecord se trovato, null altrimenti
     */
    @Override
    public PlayerRecord getPlayer(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username non può essere vuoto per la ricerca.");
        }

        String sql = "SELECT username, data, ora, percorso_file_log, durata_ms, punteggio FROM players WHERE username = ?";
        PlayerRecord player = null;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Date data = rs.getDate("data");
                    Time ora = rs.getTime("ora");
                    String logPath = rs.getString("percorso_file_log");

                    // Gestione corretta dei campi nullable
                    long durataValue = rs.getLong("durata_ms");
                    Long durataMs = rs.wasNull() ? null : durataValue;

                    int punteggioValue = rs.getInt("punteggio");
                    Integer punteggio = rs.wasNull() ? null : punteggioValue;

                    player = new PlayerRecord(rs.getString("username"), data, ora, logPath, durataMs, punteggio);
                    logger.debug("Giocatore trovato: {}", player);
                } else {
                    logger.debug("Giocatore '{}' non trovato", username);
                }
            }

        } catch (SQLException e) {
            logger.error("Errore SQL durante il recupero del giocatore '{}': {}", username, e.getMessage(), e);
            throw e;
        }

        return player;
    }

    /**
     * Registra una vittoria per un giocatore esistente.
     * Aggiorna tutti i campi relativi alla vittoria.
     * @return true se l'aggiornamento è riuscito, false se l'utente non esiste
     */
    @Override
    public boolean recordVictory(String username, Date data, Time ora, String logFilePath, Long durataMs, Integer punteggio) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username non può essere vuoto per registrare la vittoria.");
        }

        String sql = "UPDATE players SET data = ?, ora = ?, percorso_file_log = ?, durata_ms = ?, punteggio = ? WHERE username = ?";
        boolean updated = false;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, data);
            pstmt.setTime(2, ora);
            pstmt.setString(3, logFilePath);
            pstmt.setObject(4, durataMs);    // setObject gestisce automaticamente i null
            pstmt.setObject(5, punteggio);
            pstmt.setString(6, username);

            int affectedRows = pstmt.executeUpdate();
            updated = (affectedRows > 0);

            if (updated) {
                logger.info("Vittoria registrata per '{}'", username);
            } else {
                logger.warn("Nessun utente '{}' trovato per registrare la vittoria", username);
            }

        } catch (SQLException e) {
            logger.error("Errore SQL durante la registrazione vittoria per '{}': {}", username, e.getMessage(), e);
            throw e;
        }

        return updated;
    }

    /**
     * Elimina un giocatore dal database.
     * @return true se eliminato, false se non trovato
     */
    @Override
    public boolean deletePlayer(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username non può essere vuoto per l'eliminazione.");
        }

        String sql = "DELETE FROM players WHERE username = ?";
        boolean deleted = false;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            int affectedRows = pstmt.executeUpdate();
            deleted = (affectedRows > 0);

            if (deleted) {
                logger.info("Giocatore '{}' eliminato", username);
            } else {
                logger.warn("Giocatore '{}' non trovato per l'eliminazione", username);
            }
        } catch (SQLException e) {
            logger.error("Errore SQL durante l'eliminazione di '{}': {}", username, e.getMessage(), e);
            throw e;
        }

        return deleted;
    }

    /**
     * Recupera la classifica ordinata per punteggio decrescente.
     * Include solo giocatori con punteggio non nullo.
     */
    @Override
    public List<RankingEntryDTO> getRanking() throws SQLException {
        List<RankingEntryDTO> ranking = new ArrayList<>();
        String sql = "SELECT username, data, ora, punteggio FROM players " +
                     "WHERE punteggio IS NOT NULL " +
                     "ORDER BY punteggio DESC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String username = rs.getString("username");
                Date data = rs.getDate("data");
                Time ora = rs.getTime("ora");
                Integer punteggio = rs.getInt("punteggio");

                RankingEntryDTO entry = new RankingEntryDTO(username, data, ora, punteggio);
                ranking.add(entry);
            }
            logger.debug("Recuperate {} voci dalla classifica", ranking.size());

        } catch (SQLException e) {
            logger.error("Errore SQL durante il recupero della classifica: {}", e.getMessage(), e);
            throw e;
        }

        return ranking;
    }
}