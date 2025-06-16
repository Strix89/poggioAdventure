package com.mycompany.poggioadventure.core.utils;

/**
 * Classe immutabile che rappresenta il risultato di un'operazione del Flipper Zero.
 * 
 * Questa classe implementa il pattern Value Object per incapsulare tutti i dati
 * relativi all'esito di un comando eseguito sul dispositivo Flipper Zero.
 * Fornisce informazioni sul successo/fallimento dell'operazione, eventuali messaggi
 * all'utente e lo stato di avanzamento del gioco.
 * 
 * Caratteristiche principali:
 * - Immutabilità: una volta creato, un FlipperResult non può essere modificato
 * - Factory methods: creazione facilitata attraverso metodi statici tipizzati
 * - Categorizzazione: risultati classificati per tipo (successo, errore, ecc.)
 */
public class FlipperResult {
    
    /**
     * Definisce i possibili tipi di risultato di un'operazione Flipper.
     * 
     * Ogni tipo rappresenta una categoria semantica di risposta, utilizzata
     * per determinare il comportamento dell'interfaccia e del gioco.
     */
    public enum ResultType {
        /** Operazione completata con successo */
        SUCCESS, 
        /** Operazione fallita a causa di un errore */
        ERROR, 
        /** Operazione riuscita ma con effetti collaterali negativi */
        WARNING, 
        /** Risultato informativo, non critico per il gameplay */
        INFO
    }
    
    /** Messaggio testuale associato al risultato */
    private final String message;
    
    /** Categoria del risultato */
    private final ResultType type;
    
    /** Indica se questa operazione ha portato al completamento del gioco */
    private final boolean gameCompleted;
    
    /**
     * Costruttore interno per creare istanze di FlipperResult.
     * 
     * Preferire l'uso dei factory methods per una creazione più semantica.
     * 
     * @param message Messaggio di feedback all'utente
     * @param type Tipo di risultato
     * @param gameCompleted Flag di completamento gioco
     */
    public FlipperResult(String message, ResultType type, boolean gameCompleted) {
        this.message = message;
        this.type = type;
        this.gameCompleted = gameCompleted;
    }
    
    /**
     * Crea un risultato positivo per un'operazione completata con successo.
     * 
     * Da utilizzare quando un comando ha prodotto l'effetto desiderato.
     * 
     * @param message Descrizione del successo ottenuto
     * @param gameCompleted Se questo successo completa la sfida corrente
     * @return Istanza configurata come successo
     */
    public static FlipperResult success(String message, boolean gameCompleted) {
        return new FlipperResult(message, ResultType.SUCCESS, gameCompleted);
    }
    
    /**
     * Crea un risultato negativo per un'operazione fallita.
     * 
     * Da utilizzare quando un comando non può essere eseguito o ha fallito.
     * Per convenzione, un errore non può mai completare il gioco.
     * 
     * @param message Descrizione dell'errore
     * @return Istanza configurata come errore
     */
    public static FlipperResult error(String message) {
        return new FlipperResult(message, ResultType.ERROR, false);
    }
    
    /**
     * Crea un risultato di avvertimento per operazioni parzialmente riuscite.
     * 
     * Da utilizzare quando un comando ha prodotto effetti misti o ha causato
     * penalità ma è comunque stato eseguito.
     * 
     * @param message Descrizione dell'avvertimento
     * @return Istanza configurata come avvertimento
     */
    public static FlipperResult warning(String message) {
        return new FlipperResult(message, ResultType.WARNING, false);
    }
    
    /**
     * Crea un risultato informativo per fornire dettagli non critici.
     * 
     * Da utilizzare per comandi che restituiscono informazioni utili
     * ma non influenzano direttamente lo stato del gioco.
     * 
     * @param message Contenuto informativo
     * @return Istanza configurata come informazione
     */
    public static FlipperResult info(String message) {
        return new FlipperResult(message, ResultType.INFO, false);
    }
    
    /**
     * Restituisce il messaggio associato al risultato.
     * 
     * @return Messaggio testuale di feedback
     */
    public String getMessage() { 
        return message; 
    }
    
    /**
     * Restituisce la categoria di questo risultato.
     * 
     * @return Tipo di risultato (SUCCESS, ERROR, WARNING, INFO)
     */
    public ResultType getType() { 
        return type; 
    }
    
    /**
     * Verifica se questa operazione ha completato il gioco.
     * 
     * @return true se l'operazione ha portato alla vittoria, false altrimenti
     */
    public boolean isGameCompleted() { 
        return gameCompleted; 
    }
}