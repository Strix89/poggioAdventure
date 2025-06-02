package com.mycompany.poggioadventure.core.utils;

import java.util.List;
import com.mycompany.poggioadventure.ui.ErrorHandler;
import com.mycompany.poggioadventure.ui.InputHandler;
import com.mycompany.poggioadventure.ui.OutputHandler;

/**
 * Context Object che incapsula tutte le informazioni di contesto necessarie
 * agli observer e ai sottosistemi del gioco per funzionare correttamente.
 * 
 * <p>Implementa il pattern Context Object per evitare il passaggio di
 * numerosi parametri tra i vari componenti del sistema.
 * 
 * <p><b>Responsabilità principali:</b>
 * <ul>
 *   <li>Fornire accesso centralizzato ai componenti core del gioco</li>
 *   <li>Mantenere i riferimenti ai gestori di I/O ed errori</li>
 *   <li>Gestire il log temporaneo delle operazioni</li>
 *   <li>Fornire accesso al cronometro di gioco</li>
 * </ul>
 * 
 * <p><b>Pattern utilizzati:</b>
 * <ul>
 *   <li>Context Object: incapsula informazioni di contesto</li>
 *   <li>Immutable Object: tutti i campi sono final per thread-safety</li>
 * </ul>
 * 
 * <p><b>Utilizzo tipico:</b>
 * <pre>{@code
 * GameContext context = new GameContext(gameDesc, input, output, error, log, timer);
 * // Passa il context agli observer invece di parametri multipli
 * observer.update(gameDescription, parserOutput, context);
 * }</pre>
 * 
 * @author Strix89
 * @version 1.0
 * @since 1.0
 */
public class GameContext {

    /** 
     * Gestore dell'output verso l'interfaccia utente.
     * Può essere CLI o GUI-based.
     */
    private final OutputHandler outputHandler;
    
    /** 
     * Gestore dell'input dall'interfaccia utente.
     * Astrae la fonte dell'input (console, GUI, ecc.).
     */
    private final InputHandler inputHandler;
    
    /** 
     * Gestore centralizzato degli errori del sistema.
     * Gestisce sia errori recuperabili che fatali.
     */
    private final ErrorHandler errorHandler;
    
    /** 
     * Buffer temporaneo per i comandi e le operazioni da loggare.
     * Utilizzato per raccogliere informazioni prima del flush sul logger.
     */
    private final List<String> templog;
    
    /** 
     * Cronometro di gioco per misurare il tempo di sessione.
     * Implementa il pattern Singleton per consistenza globale.
     */
    private final StopWatch stopWatch;
    
    /**
     * Costruttore che inizializza il context con tutti i componenti necessari.
     * 
     * <p>Tutti i parametri sono validati e memorizzati come final per
     * garantire immutabilità e thread-safety del context.
     * 
     * @param gameDescription Il modello principale del gioco
     * @param inputHandler Gestore dell'input utente
     * @param outputHandler Gestore dell'output verso l'utente
     * @param errorHandler Gestore degli errori di sistema
     * @param templog Buffer temporaneo per il logging
     * @param stopWatch Cronometro di gioco
     * 
     * @throws IllegalArgumentException se uno dei parametri richiesti è null
     */
    public GameContext(InputHandler inputHandler, OutputHandler outputHandler, 
                      ErrorHandler errorHandler, List<String> templog, StopWatch stopWatch) {
        this.outputHandler = outputHandler;
        this.inputHandler = inputHandler;
        this.errorHandler = errorHandler;
        this.templog = templog;
        this.stopWatch = stopWatch;
    }
    
    /**
     * Restituisce il gestore dell'output.
     * 
     * @return L'istanza di OutputHandler per scrivere verso l'interfaccia
     */
    public OutputHandler getOutputHandler() { 
        return outputHandler; 
    }
    
    /**
     * Restituisce il gestore degli errori.
     * 
     * @return L'istanza di ErrorHandler per la gestione centralizzata degli errori
     */
    public ErrorHandler getErrorHandler() { 
        return errorHandler; 
    }
    
    /**
     * Restituisce il buffer temporaneo dei log.
     * 
     * <p>Questo buffer viene utilizzato per raccogliere comandi e operazioni
     * prima di scriverli definitivamente nei file di log.
     * 
     * @return Lista mutabile dei messaggi temporanei di log
     */
    public List<String> getTemplog() { 
        return templog; 
    }
    
    /**
     * Restituisce il cronometro di gioco.
     * 
     * @return L'istanza di StopWatch per misurare il tempo di sessione
     */
    public StopWatch getStopWatch() { 
        return stopWatch; 
    }
    
    /**
     * Restituisce il gestore dell'input.
     * 
     * @return L'istanza di InputHandler per ricevere input dall'utente
     */
    public InputHandler getInputHandler() { 
        return inputHandler; 
    }
}