package com.mycompany.poggioserver.db;

import com.mycompany.poggioserver.model.PlayerRecord;
import com.mycompany.poggioserver.resources.RankingEntryDTO;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

/**
 * Principio OOP: Astrazione (definisce il contratto per l'accesso ai dati)
 */
public interface PlayerDAO {

    /**
     * Aggiunge un nuovo giocatore solo con l'username.
     * Gli altri campi saranno null di default nel DB.
     * @param username Lo username univoco del giocatore.
     * @throws SQLException Se l'username esiste già o per errori DB.
     */
    void addPlayer(String username) throws SQLException;

    /**
     * Recupera i dati di un giocatore tramite username.
     * @param username Lo username del giocatore da cercare.
     * @return Il PlayerRecord corrispondente o null se non trovato.
     * @throws SQLException Per errori DB.
     */
    PlayerRecord getPlayer(String username) throws SQLException;

    /**
     * Aggiorna i dati di un giocatore esistente registrando una vittoria.
     * @param username Lo username del giocatore.
     * @param data La data della vittoria.
     * @param ora L'ora della vittoria.
     * @param logFilePath Il percorso del file di log sul server (può essere null).
     * @param durataMs La durata della partita in millisecondi (può essere null).
     * @param punteggio Il punteggio calcolato per la partita (può essere null). // NUOVO PARAMETRO
     * @return true se l'aggiornamento ha avuto successo, false altrimenti.
     * @throws SQLException Per errori DB.
     */
    boolean recordVictory(String username, Date data, Time ora, String logFilePath, Long durataMs, Integer punteggio) throws SQLException;

    /**
    * Elimina un giocatore dal database tramite username.
    * @param username Lo username del giocatore da eliminare.
    * @return true se il giocatore è stato trovato ed eliminato, false altrimenti.
    * @throws SQLException Per errori DB.
    */
    boolean deletePlayer(String username) throws SQLException;
    
    /**
     * Recupera la lista dei giocatori ordinata per punteggio (decrescente)
     * per formare una classifica. Include solo giocatori con un punteggio.
     * @return Una lista di RankingEntryDTO ordinata per punteggio DESC.
     * @throws SQLException Per errori DB.
     */
    List<RankingEntryDTO> getRanking() throws SQLException;
}
