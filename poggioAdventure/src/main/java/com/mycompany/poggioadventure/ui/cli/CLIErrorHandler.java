package com.mycompany.poggioadventure.ui.cli;

import com.mycompany.poggioadventure.ui.ErrorHandler;
import java.util.logging.Logger;

/**
 * Implementazione concreta di ErrorHandler per l'interfaccia a riga di comando (CLI).
 * Gestisce gli errori fatali e recuperabili registrandoli nel sistema di logging.
 */
public class CLIErrorHandler implements ErrorHandler {
    
    /**
     * Logger per la registrazione degli errori
     */
    private static final Logger LOGGER = Logger.getLogger(CLIErrorHandler.class.getName());

    /**
     * Gestisce un errore fatale registrandolo come SEVERE nel log.
     * Questo tipo di errore tipicamente causa la terminazione dell'applicazione.
     * 
     * @param message Messaggio descrittivo dell'errore
     * @param ex Eccezione associata all'errore (può essere null)
     */
    @Override
    public void handleFatalError(String message, Throwable ex) {
        LOGGER.log(java.util.logging.Level.SEVERE, message, ex);
    }

    /**
     * Gestisce un errore recuperabile registrandolo come WARNING nel log.
     * L'applicazione può continuare l'esecuzione dopo questo tipo di errore.
     * 
     * @param message Messaggio descrittivo dell'errore
     */
    @Override
    public void handleRecoverableError(String message) {
        LOGGER.warning(message);
    }
}