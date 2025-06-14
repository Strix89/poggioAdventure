package com.mycompany.poggioserver.db;

import com.mycompany.poggioserver.model.PlayerRecord;
import com.mycompany.poggioserver.resources.RankingEntryDTO;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

/**
 * Interfaccia DAO per la gestione dei giocatori.
 * Definisce le operazioni CRUD e di classifica.
 * 
 * @author Strix89
 */
public interface PlayerDAO {

    /**
     * Aggiunge un nuovo giocatore con solo username.
     * @throws SQLException se l'username esiste già o per errori DB
     */
    void addPlayer(String username) throws SQLException;

    /**
     * Recupera un giocatore tramite username.
     * @return PlayerRecord o null se non trovato
     */
    PlayerRecord getPlayer(String username) throws SQLException;

    /**
     * Registra una vittoria per un giocatore esistente.
     * @param punteggio Il punteggio calcolato per la partita
     * @return true se aggiornato, false se utente non trovato
     */
    boolean recordVictory(String username, Date data, Time ora, String logFilePath, Long durataMs, Integer punteggio) throws SQLException;

    /**
     * Elimina un giocatore dal database.
     * @return true se eliminato, false se non trovato
     */
    boolean deletePlayer(String username) throws SQLException;
    
    /**
     * Recupera la classifica ordinata per punteggio decrescente.
     * Include solo giocatori con punteggio registrato.
     */
    List<RankingEntryDTO> getRanking() throws SQLException;

    /**
     * Recupera tutti i giocatori dal database.
     */
    List<PlayerRecord> getAllPlayers() throws SQLException;
}
