package com.mycompany.poggioadventure.core.abstracts;

import com.mycompany.poggioadventure.core.Engine;

/**
 * Interfaccia base per il pattern State applicato ai livelli del gioco.
 * 
 * <p>Ogni stato rappresenta un livello distinto dell'avventura con:
 * <ul>
 *   <li>Condizioni di completamento specifiche</li>
 *   <li>Timer dedicato per il livello</li>
 *   <li>Gestione di successo/fallimento</li>
 *   <li>Transizioni verso il livello successivo</li>
 * </ul>
 */
public interface GameState {
    
    /**
     * Inizializza il livello corrente.
     * @param engine Riferimento al motore di gioco
     */
    void enter(Engine engine);
    
    /**
     * Verifica se il livello è stato completato con successo.
     * @param game Stato corrente del gioco
     * @return true se il livello è completato
     */
    boolean isCompleted(GameDescription game);
    
    /**
     * Verifica se si è verificata una condizione di fallimento.
     * @param game Stato corrente del gioco
     * @param elapsedTime Tempo trascorso nel livello (in millisecondi)
     * @return true se il livello è fallito
     */
    boolean isFailureConditionMet(GameDescription game, long elapsedTime);
    
    /**
     * Gestisce il successo del livello.
     * @param engine Motore di gioco per gestire la transizione
     */
    void handleSuccess(Engine engine);
    
    /**
     * Gestisce il fallimento del livello.
     * @param engine Motore di gioco
     * @param failureType Tipo di fallimento (LIGHT per reset livello, SEVERE per reset gioco)
     */
    void handleFailure(Engine engine, FailureType failureType);
    
    /**
     * Restituisce il nome identificativo del livello.
     * @return Nome del livello
     */
    String getLevelName();
    
    /**
     * Restituisce il tempo massimo consentito per questo livello (in millisecondi).
     * @return Tempo limite del livello
     */
    long getTimeLimit();
    
    /**
     * Tipi di fallimento possibili
     */
    enum FailureType {
        LIGHT,  // Reset del livello corrente
        SEVERE  // Reset completo del gioco
    }
}