package com.mycompany.poggioadventure.core.abstracts;

import com.mycompany.poggioadventure.core.GameMap;
import com.mycompany.poggioadventure.core.utils.GameContext;
import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.parser.ParserOutput;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.parser.Command;
import com.mycompany.poggioadventure.model.Room;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe astratta che definisce la struttura base per tutti i giochi adventure.
 * 
 * <p>Fornisce il contratto per:
 * <ul>
 *   <li>Gestione dello stato del mondo di gioco (mappa, inventario, stanza corrente)</li>
 *   <li>Sistema di comandi e parsing</li>
 *   <li>Serializzazione completa per salvataggio/caricamento</li>
 *   <li>Template methods per inizializzazione e logica di gioco</li>
 * </ul>
 * 
 * <p>Implementa Serializable per supportare persistenza dello stato.
 * Le implementazioni concrete devono fornire la logica specifica del gioco
 * tramite i metodi astratti.
 */
public abstract class GameDescription implements Serializable {
    private static final long serialVersionUID = 537489926633277910L;
    
    /** Struttura spaziale del mondo di gioco */
    private final GameMap gameMap = new GameMap();

    /** Registro dei comandi disponibili con alias */
    private final List<Command> commands = new ArrayList<>();

    /** Inventario del giocatore con oggetti raccolti */
    private final List<AdvObject> inventory = new ArrayList<>();

    /** Posizione corrente del giocatore nel mondo */
    private Room currentRoom;

    public List<Command> getCommands() {
        return commands;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    public List<AdvObject> getInventory() {
        return inventory;
    }

    /**
     * Sostituisce completamente l'inventario corrente.
     * Utilizzato per ripristino stato durante reset livelli o caricamento save.
     * 
     * @param inventory Nuova collezione di oggetti (null-safe)
     */
    public void setInventory(List<AdvObject> inventory) {
        this.inventory.clear();
        if (inventory != null) {
            this.inventory.addAll(inventory);
        }
    }

    /**
     * Sostituisce completamente la lista comandi disponibili.
     * Utilizzato per configurazioni specifiche di livello o modalità.
     * 
     * @param commands Nuova collezione di comandi (null-safe)
     */
    public void setCommands(List<Command> commands) {
        this.commands.clear();
        if (commands != null) {
            this.commands.addAll(commands);
        }
    }

    /**
     * Sostituisce la mappa di gioco eseguendo deep copy delle strutture.
     * Necessario per reset dei livelli mantenendo isolamento tra stati.
     * 
     * @param gameMap Nuova mappa di gioco da copiare
     */
    public void setGameMap(GameMap gameMap) {
        // Non possiamo sostituire l'istanza final, ma possiamo copiare il contenuto
        if (gameMap != null) {
            this.gameMap.getAllFloors().clear();
            // Copia correttamente ogni piano e le sue stanze
            for (int i = 0; i < gameMap.getAllFloors().size(); i++) {
                List<Room> originalFloor = gameMap.getAllFloors().get(i);
                List<Room> clonedFloor = new ArrayList<>();
                
                // Clona ogni stanza del piano
                for (Room room : originalFloor) {
                    Room clonedRoom = (Room) Utils.deepClone(room);
                    clonedFloor.add(clonedRoom);
                }
                
                this.gameMap.getAllFloors().add(clonedFloor);
            }
        }
    }

    /**
     * Template method per inizializzazione specifica del gioco.
     * Le implementazioni devono configurare comandi, observer, mondo di gioco.
     * 
     * @throws Exception se l'inizializzazione fallisce
     */
    public abstract void init() throws Exception;

    /**
     * Template method per elaborazione dei comandi del giocatore.
     * Deve gestire la logica di gioco e aggiornare lo stato del mondo.
     * 
     * @param list Lista di output del parser da elaborare
     * @param gameContext Contesto con handler I/O e utilità
     */
    public abstract void nextMove(List<ParserOutput> list, GameContext gameContext);
    
    /** Messaggio di benvenuto formattato per interfaccia CLI */
    public abstract String getCLIWelcomeMsg();
    
    /** Messaggio di benvenuto formattato per interfaccia GUI */
    public abstract String getGUIWelcomeMsg();
    
    /** Versione del gioco formattata per interfaccia CLI */
    public abstract String getCLIGameVersion();
    
    /** Versione del gioco formattata per interfaccia GUI */
    public abstract String getGUIGameVersion();

    public GameMap getGameMap() {
        return gameMap;
    }

    /**
     * Crea deep copy dell'intera istanza utilizzando serializzazione.
     * Garantisce isolamento completo tra copie per salvataggi e checkpoint.
     * 
     * @return Nuova istanza clonata completamente indipendente
     */
    @Override
    public Object clone() {
        return Utils.deepClone(this);
    }
}
