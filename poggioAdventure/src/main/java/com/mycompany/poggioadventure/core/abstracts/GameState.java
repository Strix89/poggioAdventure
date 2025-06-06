package com.mycompany.poggioadventure.core.abstracts;

import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.model.Room;
import com.mycompany.poggioadventure.ui.OutputHandler;
import java.io.Serializable;
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
public abstract class GameState implements Serializable {
    
    private final long timeLimit; // Tempo massimo consentito per il livello (in millisecondi)
    private Room startingRoom = null;
    private final List<Integer> requiredIDObjects;
    private final List<Integer> forbidenIDObjects;

    public GameState(long timeLimit, Room startingRoom, List<Integer> requiredIDObjects, List<Integer> forbiddenIDObjects) {
        this.timeLimit = timeLimit;
        this.requiredIDObjects = requiredIDObjects;
        this.forbidenIDObjects = forbiddenIDObjects;
        this.startingRoom = startingRoom;
    }

    public GameState(long timeLimit, List<Integer> requiredIDObjects, List<Integer> forbiddenIDObjects) {
        this.timeLimit = timeLimit;
        this.requiredIDObjects = requiredIDObjects;
        this.forbidenIDObjects = forbiddenIDObjects;
    }
    /**
     * Inizializza il livello corrente senza dipendenze da Engine.
     * 
     * @param gameDescription Stato del gioco da configurare
     * @param output Handler per messaggi all'utente
     * @param playerName Nome del giocatore
     */
    public abstract void enter(GameDescription gameDescription, OutputHandler output, String playerName);
    
    /**
     * Restituisce la stanza iniziale per questo livello.
     * @return Stanza di partenza del livello
     */
    public Room getStartingRoom(){
        return startingRoom;
    }

    /**
     * imposta la stanza iniziale per questo livello.
     */
    public void setStartingRoom(Room startingRoom){
        this.startingRoom = startingRoom;
    }

    public List<Integer> getRequiredIDObjects() {
        return requiredIDObjects;
    }

    public List<Integer> getForbidenIDObjects() {
        return forbidenIDObjects;
    }
    
    /**
     * Verifica se il livello è stato completato con successo.
     * @param game Stato corrente del gioco
     * @return true se il livello è completato
     */
    public abstract boolean isCompleted(GameDescription game);
    
    /**
     * Verifica se si è verificata una condizione di fallimento.
     * @param game Stato corrente del gioco
     * @param elapsedTime Tempo trascorso nel livello (in millisecondi)
     * @return true se il livello è fallito
     */
    public abstract boolean isFailureConditionMet(GameDescription game);
    
    /**
     * Gestisce il successo del livello tramite callback.
     * @param onSuccess Callback da eseguire per la transizione al livello successivo
     */
    public abstract void handleSuccess(Runnable onSuccess);
    
    /**
     * Gestisce il fallimento del livello tramite callback.
     * @param failureType Tipo di fallimento
     * @param onFailure Callback da eseguire per gestire il fallimento
     */
    public abstract void handleFailure(Runnable onFailure);
    
    /**
     * Restituisce il nome identificativo del livello.
     * @return Nome del livello
     */
    public abstract String getLevelName();
    
    /**
     * Restituisce il tempo massimo consentito per questo livello (in millisecondi).
     * @return Tempo limite del livello
     */
    public long getTimeLimit(){
        return timeLimit;
    }

    /**
     * Stampa una descrizione del livello corrente.
     * @return Tempo trascorso
     */
    public abstract void getLevelDescription(OutputHandler output, String playerName, String remaininTime);

    /**
     * Clona questo GameState utilizzando il metodo generico in Utils.
     */
    public GameState clone() {
        return Utils.deepClone(this);
    }
}