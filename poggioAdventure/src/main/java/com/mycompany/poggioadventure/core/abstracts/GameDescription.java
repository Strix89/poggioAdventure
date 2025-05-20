package com.mycompany.poggioadventure.core.abstracts;

import com.mycompany.poggioadventure.core.GameMap;
import com.mycompany.poggioadventure.ui.OutputHandler;
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
    
    private String currentChapter;

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
     *
     * @throws Exception
     */
    public abstract void init() throws Exception;

    /**
     *
     * @param list
     * @param out
     */
    public abstract void nextMove(List<ParserOutput> list, OutputHandler out);
    
    public String getCurrentChapter(){
        return currentChapter;
    }
    
    public void setCurrentChapeter(String cp){
       currentChapter = cp;
    }
    
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
}
