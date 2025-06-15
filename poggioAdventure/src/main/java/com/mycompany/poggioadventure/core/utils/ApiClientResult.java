package com.mycompany.poggioadventure.core.utils;

/**
 * Enumerazione che rappresenta i possibili risultati delle operazioni API.
 * 
 * Questo enum fornisce un'astrazione dei codici di stato HTTP e degli errori
 * di comunicazione che possono verificarsi durante le interazioni client-server.
 * Permette di gestire in modo uniforme le risposte e gli errori, semplificando
 * la logica di gestione degli errori nel codice client.
 * 
 * I valori sono raggruppati in tre categorie principali:
 * 1. Successo - Operazioni completate correttamente
 * 2. Fallimenti attesi - Situazioni di errore previste e gestibili
 * 3. Errori - Problemi tecnici o di comunicazione
 */
public enum ApiClientResult {
    // ======== Codici di Successo ========
    /**
     * Indica che una risorsa è stata creata con successo.
     * Corrisponde tipicamente a una risposta HTTP 201 Created.
     */
    SUCCESS_CREATED,
    
    /**
     * Indica che un'operazione è stata completata con successo.
     * Corrisponde tipicamente a una risposta HTTP 200 OK.
     */
    SUCCESS_OK,

    // ======== Codici di Fallimento Atteso / Conflitto ========
    /**
     * Indica che si è tentato di creare un utente già esistente.
     * Corrisponde tipicamente a una risposta HTTP 409 Conflict.
     */
    USER_ALREADY_EXISTS,
    
    /**
     * Indica che l'utente richiesto non è stato trovato.
     * Corrisponde tipicamente a una risposta HTTP 404 Not Found.
     */
    USER_NOT_FOUND,
    
    /**
     * Indica che si è tentato di registrare un log già esistente.
     * Corrisponde tipicamente a una risposta HTTP 409 Conflict.
     */
    LOG_ALREADY_EXISTS,

    // ======== Codici di Errore ========
    /**
     * Indica un errore di validazione rilevato lato client.
     * Ad esempio: username vuoto o percorso file non valido.
     */
    INVALID_INPUT_CLIENT,
    
    /**
     * Indica un errore di validazione rilevato lato server.
     * Corrisponde tipicamente a una risposta HTTP 400 Bad Request.
     */
    INVALID_INPUT_SERVER,
    
    /**
     * Indica un problema di autenticazione.
     * Corrisponde tipicamente a una risposta HTTP 401 Unauthorized.
     */
    UNAUTHORIZED,
    
    /**
     * Indica un errore di comunicazione con il server.
     * Ad esempio: timeout, server non raggiungibile, problemi di rete.
     */
    CONNECTION_ERROR,
    
    /**
     * Indica un errore interno del server.
     * Corrisponde tipicamente a risposte HTTP 5xx.
     */
    SERVER_ERROR,
    
    /**
     * Indica un errore di I/O locale durante operazioni su file.
     * Ad esempio: errori di lettura/scrittura durante preparazione upload.
     */
    FILE_ERROR,
    
    /**
     * Indica un errore non previsto o non classificabile.
     * Utilizzato come fallback per situazioni eccezionali.
     */
    UNKNOWN_ERROR
}
