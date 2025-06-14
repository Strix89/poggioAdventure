package com.mycompany.poggioadventure.core.utils;

/**
 * Value Object che rappresenta il risultato dell'elaborazione di un comando Flipper Zero.
 * 
 * <p>Implementa il pattern Value Object per incapsulare in modo immutabile
 * tutte le informazioni relative al risultato di un comando del Flipper Zero.
 * 
 * <p><b>Responsabilità principali:</b>
 * <ul>
 *   <li>Contenere il messaggio di risposta dell'operazione</li>
 *   <li>Specificare il tipo di risultato (successo/errore/avviso/info)</li>
 *   <li>Indicare se il comando ha completato il gioco</li>
 *   <li>Specificare eventuali modifiche al timer di gioco</li>
 * </ul>
 * 
 * <p><b>Pattern utilizzati:</b>
 * <ul>
 *   <li>Value Object: oggetto immutabile che rappresenta un valore</li>
 *   <li>Factory Method: metodi statici per creazione di istanze tipizzate</li>
 *   <li>Enum Strategy: usa enum per definire i tipi di risultato</li>
 * </ul>
 * 
 * <p><b>Utilizzo tipico:</b>
 * <pre>{@code
 * // Creazione di un risultato di successo
 * FlipperResult result = FlipperResult.success("Robot reindirizzati!", true, 0);
 * 
 * // Creazione di un errore
 * FlipperResult error = FlipperResult.error("Frequenza non valida");
 * 
 * // Verifica del tipo di risultato
 * if (result.getType() == FlipperResult.ResultType.SUCCESS) {
 *     // gestisci successo
 * }
 * }</pre>
 * 
 * @author Strix89
 * @version 1.0
 * @since 1.0
 */
public class FlipperResult {
    
    /**
     * Enumerazione che definisce i possibili tipi di risultato
     * per un comando Flipper Zero.
     * 
     * <p>Ogni tipo ha un significato specifico:
     * <ul>
     *   <li>{@code SUCCESS}: Operazione completata con successo</li>
     *   <li>{@code ERROR}: Errore nell'esecuzione del comando</li>
     *   <li>{@code WARNING}: Comando eseguito ma con problemi</li>
     *   <li>{@code INFO}: Informazione o comando di utilità</li>
     * </ul>
     */
    public enum ResultType {
        /** Comando eseguito con successo completo */
        SUCCESS, 
        /** Errore nell'esecuzione - comando fallito */
        ERROR, 
        /** Comando eseguito ma con avvertimenti o penalità */
        WARNING, 
        /** Comando informativo o di utilità */
        INFO
    }
    
    /** Messaggio descrittivo del risultato dell'operazione */
    private final String message;
    
    /** Tipo di risultato (successo, errore, avviso, info) */
    private final ResultType type;
    
    /** Flag che indica se il comando ha completato il gioco/sfida */
    private final boolean gameCompleted;
    
    
    /**
     * Costruttore privato per garantire l'uso dei factory methods.
     * 
     * @param message Messaggio descrittivo del risultato
     * @param type Tipo di risultato
     * @param gameCompleted Se l'operazione ha completato il gioco
     */
    public FlipperResult(String message, ResultType type, boolean gameCompleted) {
        this.message = message;
        this.type = type;
        this.gameCompleted = gameCompleted;
    }
    
    /**
     * Factory method per creare un risultato di successo.
     * 
     * <p>Utilizzato quando un comando Flipper viene eseguito correttamente
     * e produce l'effetto desiderato.
     * 
     * @param message Messaggio di successo da mostrare all'utente
     * @param gameCompleted Se questo successo completa il gioco/sfida
     * @param timeModification Modifica al timer (0 = nessuna modifica)
     * @return Nuova istanza di FlipperResult di tipo SUCCESS
     * 
     * @example
     * <pre>{@code
     * return FlipperResult.success("Robot reindirizzati!", true, 0);
     * }</pre>
     */
    public static FlipperResult success(String message, boolean gameCompleted) {
        return new FlipperResult(message, ResultType.SUCCESS, gameCompleted);
    }
    
    /**
     * Factory method per creare un risultato di errore.
     * 
     * <p>Utilizzato quando un comando Flipper fallisce o non può essere eseguito.
     * Gli errori non modificano il timer e non completano mai il gioco.
     * 
     * @param message Messaggio di errore da mostrare all'utente
     * @return Nuova istanza di FlipperResult di tipo ERROR
     * 
     * @example
     * <pre>{@code
     * return FlipperResult.error("Frequenza non valida");
     * }</pre>
     */
    public static FlipperResult error(String message) {
        return new FlipperResult(message, ResultType.ERROR, false);
    }
    
    /**
     * Factory method per creare un risultato di avviso.
     * 
     * <p>Utilizzato quando un comando viene eseguito ma con effetti negativi
     * o indesiderati, tipicamente con penalità al timer.
     * 
     * @param message Messaggio di avviso da mostrare all'utente
     * @return Nuova istanza di FlipperResult di tipo WARNING
     * 
     * @example
     * <pre>{@code
     * return FlipperResult.warning("Override causato instabilità!", -30);
     * }</pre>
     */
    public static FlipperResult warning(String message) {
        return new FlipperResult(message, ResultType.WARNING, false);
    }
    
    /**
     * Factory method per creare un risultato informativo.
     * 
     * <p>Utilizzato per comandi di utilità che forniscono informazioni
     * o bonus al giocatore senza essere critici per il completamento.
     * 
     * @param message Messaggio informativo da mostrare all'utente
     * @return Nuova istanza di FlipperResult di tipo INFO
     * 
     * @example
     * <pre>{@code
     * return FlipperResult.info("Robot fermati, bonus tempo!", 60);
     * }</pre>
     */
    public static FlipperResult info(String message) {
        return new FlipperResult(message, ResultType.INFO, false);
    }
    
    /**
     * Restituisce il messaggio descrittivo del risultato.
     * 
     * @return Stringa con il messaggio da mostrare all'utente
     */
    public String getMessage() { 
        return message; 
    }
    
    /**
     * Restituisce il tipo di risultato.
     * 
     * @return Enum indicante se è successo, errore, avviso o info
     */
    public ResultType getType() { 
        return type; 
    }
    
    /**
     * Indica se questo risultato completa il gioco o la sfida.
     * 
     * @return true se il comando ha completato il gioco, false altrimenti
     */
    public boolean isGameCompleted() { 
        return gameCompleted; 
    }
    

}