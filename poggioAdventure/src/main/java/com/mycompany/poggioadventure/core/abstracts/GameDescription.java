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
 *
 * @author pierpaolo
 */
public abstract class GameDescription implements Serializable {
    private static final long serialVersionUID = 537489926633277910L;
    
    private final GameMap gameMap = new GameMap();

    private final List<Command> commands = new ArrayList<>();

    private final List<AdvObject> inventory = new ArrayList<>();

    private Room currentRoom;

    /**
     *
     * @return
     */
    public List<Command> getCommands() {
        return commands;
    }

    /**
     *
     * @return
     */
    public Room getCurrentRoom() {
        return currentRoom;
    }

    /**
     *
     * @param currentRoom
     */
    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    /**
     *
     * @return
     */
    public List<AdvObject> getInventory() {
        return inventory;
    }

    /**
     * Imposta la lista dell'inventario.
     * Utilizzato per il ripristino dello stato durante il reset dei livelli.
     * 
     * @param inventory Nuova lista di oggetti nell'inventario
     */
    public void setInventory(List<AdvObject> inventory) {
        this.inventory.clear();
        if (inventory != null) {
            this.inventory.addAll(inventory);
        }
    }

    /**
     * Imposta la lista dei comandi disponibili.
     * Utilizzato per il ripristino dello stato durante il reset dei livelli.
     * 
     * @param commands Nuova lista di comandi
     */
    public void setCommands(List<Command> commands) {
        this.commands.clear();
        if (commands != null) {
            this.commands.addAll(commands);
        }
    }

    /**
     * Imposta la mappa di gioco.
     * Utilizzato per il ripristino dello stato durante il reset dei livelli.
     * 
     * @param gameMap Nuova mappa di gioco
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
     *
     * @throws Exception
     */
    public abstract void init() throws Exception;

    /**
     *
     * @param list
     * @param out
     */
    public abstract void nextMove(List<ParserOutput> list, GameContext gameContext);
    
    /**
     *
     * @return
     */
    public abstract String getCLIWelcomeMsg();
    public abstract String getGUIWelcomeMsg();
    
    public abstract String getCLIGameVersion();
    public abstract String getGUIGameVersion();

    public GameMap getGameMap() {
        return gameMap;
    }

    /**
     * Crea una copia profonda usando la serializzazione.
     * @return Una nuova istanza clonata
     */
    @Override
    public Object clone() {
        return Utils.deepClone(this);
    }
}
