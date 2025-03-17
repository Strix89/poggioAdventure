package di.uniba.map.b.adventure;

import di.uniba.map.b.adventure.type.AdvObject;
import di.uniba.map.b.adventure.type.Room;
import java.io.Serializable;
import java.util.List;

public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;

    private String playerName;
    private String chapter;
    private Room currentRoom;
    private List<AdvObject> inventory;

    public GameState(String playerName, String chapter, Room currentRoom, List<AdvObject> inventory) {
        this.playerName = playerName;
        this.chapter = chapter;
        this.currentRoom = currentRoom;
        this.inventory = inventory;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getChapter() {
        return chapter;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public List<AdvObject> getInventory() {
        return inventory;
    }
}
