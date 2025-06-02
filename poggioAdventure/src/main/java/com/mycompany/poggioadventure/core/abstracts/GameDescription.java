package com.mycompany.poggioadventure.core.abstracts;

import com.mycompany.poggioadventure.core.GameMap;
import com.mycompany.poggioadventure.core.utils.GameContext;
import com.mycompany.poggioadventure.parser.ParserOutput;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.parser.Command;
import com.mycompany.poggioadventure.model.Room;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        return inventory.stream()
                .filter(obj -> obj.isVisible())
                .collect(Collectors.toList());
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
    public GameDescription clone() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.close();
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            GameDescription cloned = (GameDescription) ois.readObject();
            ois.close();
            
            return cloned;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Errore durante la clonazione", e);
        }
    }
}
