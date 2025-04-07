package com.mycompany.poggioserver.db;

import com.mycompany.poggioserver.model.PlayerRecord;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Time;

// Principio OOP: Astrazione (definisce il contratto per l'accesso ai dati)
/**
 *
 * @author Strix89
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
     * @param logFilePath Il percorso del file di log (può essere null).
     * @return true se l'aggiornamento ha avuto successo (giocatore trovato), false altrimenti.
     * @throws SQLException Per errori DB o di parsing data/ora.
     */
    boolean recordVictory(String username, Date data, Time ora, String logFilePath) throws SQLException;

    // Potrebbe includere altri metodi come deletePlayer, getAllPlayers, etc.
}
