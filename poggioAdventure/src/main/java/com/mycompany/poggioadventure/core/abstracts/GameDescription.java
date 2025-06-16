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
 * Implementa il pattern Template Method per la gestione del ciclo di gioco e
 * fornisce l'infrastruttura per la memorizzazione dello stato di gioco.
 * 
 * La classe gestisce:
 * - La mappa di gioco con tutte le stanze e i piani
 * - L'inventario del giocatore
 * - I comandi disponibili
 * - La posizione corrente del giocatore
 * 
 * Le sottoclassi devono implementare la logica specifica del gioco attraverso
 * i metodi astratti definiti.
 */
public abstract class GameDescription implements Serializable {
    private static final long serialVersionUID = 537489926633277910L;
    
    /** Mappa del gioco che contiene tutti i piani e le stanze */
    private final GameMap gameMap = new GameMap();

    /** Lista dei comandi disponibili nel gioco */
    private final List<Command> commands = new ArrayList<>();

    /** Inventario del giocatore che contiene gli oggetti raccolti */
    private final List<AdvObject> inventory = new ArrayList<>();

    /** Stanza in cui si trova attualmente il giocatore */
    private Room currentRoom;

    /**
     * Restituisce la lista dei comandi disponibili nel gioco.
     * 
     * @return Lista dei comandi disponibili
     */
    public List<Command> getCommands() {
        return commands;
    }

    /**
     * Restituisce la stanza corrente in cui si trova il giocatore.
     * 
     * @return Stanza corrente
     */
    public Room getCurrentRoom() {
        return currentRoom;
    }

    /**
     * Imposta la stanza corrente in cui si trova il giocatore.
     * Utilizzato per spostare il giocatore tra le stanze.
     * 
     * @param currentRoom Nuova stanza corrente
     */
    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    /**
     * Restituisce l'inventario del giocatore.
     * 
     * @return Lista degli oggetti nell'inventario
     */
    public List<AdvObject> getInventory() {
        return inventory;
    }

    /**
     * Sostituisce completamente l'inventario corrente.
     * Utile durante il caricamento di un salvataggio o il reset del gioco.
     * 
     * @param inventory Nuova collezione di oggetti (può essere null)
     */
    public void setInventory(List<AdvObject> inventory) {
        this.inventory.clear();
        if (inventory != null) {
            this.inventory.addAll(inventory);
        }
    }

    /**
     * Sostituisce completamente la lista dei comandi disponibili.
     * Utile per personalizzare i comandi in base al livello o modalità di gioco.
     * 
     * @param commands Nuova collezione di comandi (può essere null)
     */
    public void setCommands(List<Command> commands) {
        this.commands.clear();
        if (commands != null) {
            this.commands.addAll(commands);
        }
    }

    /**
     * Sostituisce il contenuto della mappa di gioco effettuando una copia profonda.
     * Fondamentale per il reset dei livelli mantenendo l'isolamento tra gli stati.
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
     * Inizializza il gioco configurando comandi, observer e mondo di gioco.
     * Le sottoclassi devono implementare questo metodo per definire lo stato iniziale.
     * 
     * @throws Exception Se si verificano errori durante l'inizializzazione
     */
    public abstract void init() throws Exception;

    /**
     * Elabora il prossimo comando del giocatore aggiornando lo stato del gioco.
     * Le sottoclassi devono implementare la logica di gioco specifica.
     * 
     * @param list Lista di output dal parser da elaborare
     * @param gameContext Contesto di gioco con gestori I/O e utilities
     */
    public abstract void nextMove(List<ParserOutput> list, GameContext gameContext);
    
    /**
     * Restituisce il messaggio di benvenuto formattato per l'interfaccia a riga di comando.
     * 
     * @return Messaggio di benvenuto per CLI
     */
    public abstract String getCLIWelcomeMsg();
    
    /**
     * Restituisce il messaggio di benvenuto formattato per l'interfaccia grafica.
     * 
     * @return Messaggio di benvenuto per GUI
     */
    public abstract String getGUIWelcomeMsg();
    
    /**
     * Restituisce la versione del gioco formattata per l'interfaccia a riga di comando.
     * 
     * @return Versione del gioco per CLI
     */
    public abstract String getCLIGameVersion();
    
    /**
     * Restituisce la versione del gioco formattata per l'interfaccia grafica.
     * 
     * @return Versione del gioco per GUI
     */
    public abstract String getGUIGameVersion();

    /**
     * Restituisce la mappa di gioco.
     * 
     * @return Mappa del gioco con tutti i piani e le stanze
     */
    public GameMap getGameMap() {
        return gameMap;
    }

    /**
     * Crea una copia profonda (deep copy) dell'intera istanza di gioco.
     * Utilizza la serializzazione per garantire un isolamento completo tra le copie.
     * Fondamentale per implementare salvataggi e checkpoint.
     * 
     * @return Nuova istanza clonata completamente indipendente
     */
    @Override
    public Object clone() {
        return Utils.deepClone(this);
    }

    /**
     * Rimuove un oggetto dall'inventario del giocatore tramite il suo ID.
     * Utile quando un oggetto viene utilizzato o scambiato.
     * 
     * @param id ID dell'oggetto da rimuovere
     * @return true se l'oggetto è stato trovato e rimosso, false altrimenti
     */
    public boolean removeFromInventoryById(int id) {
        return inventory.removeIf(obj -> obj.getId() == id);
    }
}
