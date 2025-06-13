package com.mycompany.poggioadventure.core.abstracts;

import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.model.Room;
import com.mycompany.poggioadventure.ui.OutputHandler;
import java.io.Serializable;
import java.util.List;

/**
 * Classe astratta che definisce la struttura e il comportamento di un livello di gioco.
 * 
 * <p>Implementa il pattern State per gestire la logica specifica di ogni livello,
 * fornendo un contratto uniforme per:
 * <ul>
 *   <li>Configurazione del livello (tempo limite, oggetti richiesti/vietati)</li>
 *   <li>Gestione delle condizioni di vittoria e sconfitta</li>
 *   <li>Callback pattern per transizioni di stato</li>
 *   <li>Serializzazione per salvataggio/caricamento</li>
 * </ul>
 * 
 * <p>Ogni livello concreto deve implementare la propria logica di inizializzazione,
 * completamento e gestione degli eventi tramite i metodi astratti.
 */
public abstract class GameState implements Serializable {
    
    /** Tempo massimo consentito per completare il livello (millisecondi) */
    private final long timeLimit;
    
    /** Stanza di spawn del giocatore per questo livello */
    private Room startingRoom = null;
    
    /** ID degli oggetti necessari per completare il livello */
    private final List<Integer> requiredIDObjects;
    
    /** ID degli oggetti che causano fallimento se raccolti */
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
     * Template method per inizializzazione del livello senza dipendenze circolari.
     * Configura lo stato del mondo, posizione iniziale e parametri specifici.
     * 
     * @param gameDescription Stato principale del gioco da configurare
     * @param output Handler per comunicazione con l'utente
     * @param playerName Identificativo del giocatore per messaggi personalizzati
     */
    public abstract void enter(GameDescription gameDescription, OutputHandler output, String playerName);
    
    /** Restituisce la stanza di partenza configurata per questo livello */
    public Room getStartingRoom(){
        return startingRoom;
    }

    /** Imposta la stanza di partenza per questo livello */
    public void setStartingRoom(Room startingRoom){
        this.startingRoom = startingRoom;
    }

    /** Lista degli ID oggetti necessari per completare il livello */
    public List<Integer> getRequiredIDObjects() {
        return requiredIDObjects;
    }

    /** Lista degli ID oggetti vietati che causano fallimento del livello */
    public List<Integer> getForbidenIDObjects() {
        return forbidenIDObjects;
    }
    
    /**
     * Valuta se il livello è stato completato con successo.
     * Implementazioni concrete definiscono condizioni specifiche.
     * 
     * @param game Stato corrente del mondo di gioco
     * @return true se tutte le condizioni di vittoria sono soddisfatte
     */
    public abstract boolean isCompleted(GameDescription game);
    
    /**
     * Verifica se si sono verificate condizioni di fallimento irreversibile.
     * 
     * @param game Stato corrente del mondo di gioco
     * @return true se il livello deve essere considerato fallito
     */
    public abstract boolean isFailureConditionMet(GameDescription game);
    
    /**
     * Gestisce la transizione di successo tramite callback per evitare dipendenze.
     * 
     * @param onSuccess Callback per avanzamento al livello successivo
     */
    public abstract void handleSuccess(Runnable onSuccess);
    
    /**
     * Gestisce il fallimento del livello con callback per azioni appropriate.
     * 
     * @param onFailure Callback per gestione sconfitta (reset o game over)
     */
    public abstract void handleFailure(Runnable onFailure);
    
    /** Identificativo univoco del livello per logging e debugging */
    public abstract String getLevelName();
    
    /** Restituisce il tempo limite configurato per questo livello in millisecondi */
    public long getTimeLimit(){
        return timeLimit;
    }

    /**
     * Visualizza informazioni introduttive del livello con formattazione specifica.
     * 
     * @param output Handler per output formattato
     * @param playerName Nome per personalizzazione messaggi
     * @param remaininTime Tempo rimanente formattato per display
     */
    public abstract void getLevelDescription(OutputHandler output, String playerName, String remaininTime);

    /** Crea deep copy del livello utilizzando serializzazione per isolamento completo */
    public GameState clone() {
        return Utils.deepClone(this);
    }
}