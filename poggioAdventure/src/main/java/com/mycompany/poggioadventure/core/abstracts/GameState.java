package com.mycompany.poggioadventure.core.abstracts;

import com.mycompany.poggioadventure.model.Room;
import com.mycompany.poggioadventure.ui.OutputHandler;
import java.util.List;

/**
 * Interfaccia base per il pattern State applicato ai livelli del gioco.
 * 
 * <p>Ogni livello implementa questa interfaccia per definire:
 * <ul>
 *   <li>Stanza iniziale e condizioni di vittoria</li>
 *   <li>Timeout e gestione del tempo</li>
 *   <li>Logica di transizione tra livelli</li>
 * </ul>
 */
public interface GameState {
    
    /**
     * Inizializza il livello corrente senza dipendenze da Engine.
     * 
     * @param gameDescription Stato del gioco da configurare
     * @param output Handler per messaggi all'utente
     */
    void enter(GameDescription gameDescription, OutputHandler output);
    
    /**
     * Restituisce la stanza iniziale per questo livello.
     * @return Stanza di partenza del livello
     */
    Room getStartingRoom();
    
    /**
     * Restituisce gli oggetti richiesti nell'inventario per completare il livello.
     * @return Lista degli ID degli oggetti necessari
     */
    List<Integer> getRequiredObjects();
    
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
     * Gestisce il successo del livello tramite callback.
     * @param onSuccess Callback da eseguire per la transizione al livello successivo
     */
    void handleSuccess(Runnable onSuccess);
    
    /**
     * Gestisce il fallimento del livello tramite callback.
     * @param failureType Tipo di fallimento
     * @param onFailure Callback da eseguire per gestire il fallimento
     */
    void handleFailure(FailureType failureType, Runnable onFailure);
    
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