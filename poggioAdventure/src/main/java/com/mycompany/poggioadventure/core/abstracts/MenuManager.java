package com.mycompany.poggioadventure.core.abstracts;

/**
 * Interfaccia che definisce i requisiti per la gestione dei menu di navigazione del gioco.
 * 
 * <p>Questa interfaccia stabilisce il contratto per la gestione del flusso di navigazione
 * tra le diverse schermate funzionali dell'applicazione. Fornisce una chiara separazione
 * tra la logica di presentazione e la logica di gioco, facilitando l'implementazione
 * di diverse interfacce utente mantenendo coerenza nei flussi di navigazione.
 * 
 * <p>Le schermate principali gestite sono:
 * <ul>
 *   <li>Menu principale - hub centrale di navigazione</li>
 *   <li>Nuova partita - creazione e configurazione sessione</li>
 *   <li>Caricamento partita - ripristino stato salvato</li>
 *   <li>Classifica - visualizzazione statistiche</li>
 *   <li>Uscita - terminazione controllata</li>
 * </ul>
 * 
 * <p>Pattern implementativi comuni:
 * <ul>
 *   <li>{@code CLIMenu} - implementazione testuale per terminale</li>
 *   <li>{@code UI_Init} - implementazione grafica con componenti UI</li>
 * </ul>
 */
public interface MenuManager {

    /**
     * Visualizza il menu principale e gestisce la navigazione verso altre schermate.
     * 
     * <p>Questo metodo deve renderizzare l'interfaccia del menu principale e gestire
     * l'interazione con l'utente, indirizzando il flusso verso le appropriate
     * funzionalità in base alla selezione:
     * <ul>
     *   <li>Nuova partita - inizializzazione nuova sessione</li>
     *   <li>Carica partita - recupero stato precedente</li>
     *   <li>Classifica - consultazione punteggi</li>
     *   <li>Esci - terminazione applicazione</li>
     * </ul>
     */
    void showMainMenu();

    /**
     * Gestisce il processo di creazione e avvio di una nuova partita.
     * 
     * <p>Implementa la logica necessaria per raccogliere i parametri iniziali
     * e inizializzare lo stato di gioco. Tipicamente si occupa di:
     * <ul>
     *   <li>Acquisire dati giocatore (nome, profilo)</li>
     *   <li>Configurare parametri sessione (difficoltà, modalità)</li>
     *   <li>Inizializzare e avviare il motore di gioco</li>
     * </ul>
     */
    void showNewGame();

    /**
     * Gestisce il caricamento di partite precedentemente salvate.
     * 
     * <p>Questo metodo deve implementare:
     * <ul>
     *   <li>Scansione e presentazione dei salvataggi disponibili</li>
     *   <li>Interfaccia di selezione salvataggio</li>
     *   <li>Logica di deserializzazione e ripristino stato</li>
     * </ul>
     */
    void showLoadGame();

    /**
     * Visualizza la classifica e le statistiche di gioco.
     * 
     * <p>Presenta all'utente le informazioni sulle performance storiche:
     * <ul>
     *   <li>Migliori punteggi (top 10 o altra configurazione)</li>
     *   <li>Statistiche avanzate (quando implementate)</li>
     *   <li>Controlli di navigazione per tornare al menu</li>
     * </ul>
     */
    void showRanking();

    /**
     * Implementa la procedura di uscita sicura dall'applicazione.
     * 
     * <p>Gestisce la terminazione controllata dell'applicazione:
     * <ul>
     *   <li>Richiesta conferma per prevenire uscite accidentali</li>
     *   <li>Salvataggio configurazioni e stato se necessario</li>
     *   <li>Rilascio risorse e terminazione processi</li>
     * </ul>
     */
    void exit();
}
