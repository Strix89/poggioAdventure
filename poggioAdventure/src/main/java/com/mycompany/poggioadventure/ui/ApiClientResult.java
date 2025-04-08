package com.mycompany.poggioadventure.ui;

/**
 * Enum che rappresenta i possibili risultati delle operazioni del PoggioClientJersey.
 */
/**
 *
 * @author Strix89
 */
public enum ApiClientResult {
    // Codici di Successo
    SUCCESS_CREATED,        // Es: Utente aggiunto (HTTP 201)
    SUCCESS_OK,             // Es: Operazione OK (HTTP 200 - utente trovato, vittoria registrata)

    // Codici di Fallimento Atteso / Conflitto
    USER_ALREADY_EXISTS,    // Es: Add user fallito (HTTP 409)
    USER_NOT_FOUND,         // Es: Check user fallito (HTTP 404), Record victory fallito per utente non trovato (HTTP 404)
    LOG_ALREADY_EXISTS,     // Es: Record victory fallito (HTTP 409 - log già presente)

    // Codici di Errore
    INVALID_INPUT_CLIENT,   // Es: Username vuoto, percorso file non valido (rilevato nel client)
    INVALID_INPUT_SERVER,   // Es: Il server ha risposto HTTP 400 Bad Request
    UNAUTHORIZED,           // Es: Chiave API errata (HTTP 401)
    CONNECTION_ERROR,       // Es: Errore di rete, connessione fallita, timeout (ProcessingException)
    SERVER_ERROR,           // Es: Errore interno del server (HTTP 5xx)
    FILE_ERROR,             // Es: Errore I/O locale durante preparazione upload
    UNKNOWN_ERROR           // Errore generico non previsto
}
