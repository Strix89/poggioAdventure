package com.mycompany.poggioadventure.ui;

/**
 * Interfaccia per la gestione degli errori nel motore di gioco.
 * 
 * <p>Fornisce metodi per gestire diversi tipi di errori:
 * <ul>
 *   <li>Errori fatali che richiedono la terminazione dell'applicazione</li>
 *   <li>Errori recuperabili che permettono al gioco di continuare</li>
 * </ul>
 * 
 * <p>Le implementazioni dovrebbero fornire una gestione appropriata
 * sia per ambienti CLI che GUI.
 * 
 * @author Strix89
 */
public interface ErrorHandler {

    /**
     * Gestisce un errore fatale che richiede la terminazione dell'applicazione.
     * 
     * @param messaggio Descrizione dell'errore
     * @param ex L'eccezione che ha causato l'errore (può essere null)
     */
    void handleFatalError(String messaggio, Throwable ex);

    /**
     * Gestisce un errore recuperabile che non termina l'applicazione.
     * 
     * @param messaggio Descrizione dell'errore
     */
    void handleRecoverableError(String messaggio);
}