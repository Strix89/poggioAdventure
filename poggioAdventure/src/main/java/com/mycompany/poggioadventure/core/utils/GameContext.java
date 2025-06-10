package com.mycompany.poggioadventure.core.utils;

import java.util.List;
import com.mycompany.poggioadventure.ui.ErrorHandler;
import com.mycompany.poggioadventure.ui.InputHandler;
import com.mycompany.poggioadventure.ui.OutputHandler;

/**
 * Context Object che incapsula componenti core del sistema di gioco.
 * 
 * <p>Evita il passaggio di molteplici parametri tra sottosistemi fornendo
 * accesso centralizzato a handler I/O, gestione errori e logging temporaneo.
 * Garantisce thread-safety attraverso immutabilità dei riferimenti.
 * 
 * <p><b>Responsabilità:</b>
 * <ul>
 *   <li>Accesso centralizzato ai gestori I/O e errori</li>
 *   <li>Gestione buffer temporaneo per logging</li>
 *   <li>Riferimento al cronometro di sessione</li>
 * </ul>
 * 
 * <p><b>Pattern:</b> Context Object, Immutable Object per thread-safety
 */
public class GameContext {

    /** Gestore output verso interfaccia utente (CLI/GUI) */
    private final OutputHandler outputHandler;
    
    /** Gestore input da interfaccia utente */
    private final InputHandler inputHandler;
    
    /** Gestore centralizzato errori di sistema */
    private final ErrorHandler errorHandler;
    
    /** Buffer temporaneo per raccolta comandi da loggare */
    private final List<String> templog;
    
    /** Cronometro di gioco per misurazione tempo sessione */
    private final StopWatch stopWatch;
    
    /**
     * Inizializza context con tutti i componenti necessari per il funzionamento.
     * Tutti i parametri sono memorizzati come final per garantire immutabilità.
     * 
     * @param inputHandler Gestore input utente
     * @param outputHandler Gestore output verso utente
     * @param errorHandler Gestore errori sistema
     * @param templog Buffer temporaneo logging
     * @param stopWatch Cronometro di gioco
     */
    public GameContext(InputHandler inputHandler, OutputHandler outputHandler, 
                      ErrorHandler errorHandler, List<String> templog, StopWatch stopWatch) {
        this.outputHandler = outputHandler;
        this.inputHandler = inputHandler;
        this.errorHandler = errorHandler;
        this.templog = templog;
        this.stopWatch = stopWatch;
    }
    
    /** Restituisce gestore output per scrittura verso interfaccia */
    public OutputHandler getOutputHandler() { 
        return outputHandler; 
    }
    
    /** Restituisce gestore errori per gestione centralizzata */
    public ErrorHandler getErrorHandler() { 
        return errorHandler; 
    }
    
    /** 
     * Restituisce buffer temporaneo log per raccolta comandi.
     * Buffer mutabile utilizzato prima del flush definitivo su file.
     */
    public List<String> getTemplog() { 
        return templog; 
    }
    
    /** Restituisce cronometro per misurazione tempo sessione */
    public StopWatch getStopWatch() { 
        return stopWatch; 
    }
    
    /** Restituisce gestore input per ricezione comandi utente */
    public InputHandler getInputHandler() { 
        return inputHandler; 
    }
}