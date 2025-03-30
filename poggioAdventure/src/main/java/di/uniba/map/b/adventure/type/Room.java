package di.uniba.map.b.adventure.type;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/* Classe Room che definisce e costruisce le stanze che poi costituiranno la 
    mappa di gioco. Ogni stanza ha un id, nome, descrizione, se è visibile o meno, 
    le stanze collegate a nord, sud, est e ovest e l'eventuale lista di oggetti 
    presenti nella stanza. È stato aggiunto un puntatore alla stanza su un altro piano.
*/

public class Room implements Serializable {
    
    private static final long serialVersionUID = 123456789L;

    private final int id;
    private String name;
    private String description;
    private String look;
    private boolean visible = true;
    private CommandType linkedDirection = CommandType.NONE;
    
    // Riferimenti alle stanze collegate nello stesso piano
    private Room south = null;
    private Room north = null;
    private Room east = null;
    private Room west = null;

    // Lista di oggetti presenti nella stanza
    private final List<AdvObject> objects = new ArrayList<>();

    // Riferimento alla stanza su un altro piano (nuovo attributo)
    private Room linkedRoom = null;  // Stanza collegata su un altro piano

    public Room(int id) {
        this.id = id;
    }

    public Room(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Getter e Setter per il nome, la descrizione, la visibilità
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    // Getter e Setter per le stanze collegate (nord, sud, est, ovest)
    public Room getSouth() {
        return south;
    }

    public void setSouth(Room south) {
        this.south = south;
    }

    public Room getNorth() {
        return north;
    }

    public void setNorth(Room north) {
        this.north = north;
    }

    public Room getEast() {
        return east;
    }

    public void setEast(Room east) {
        this.east = east;
    }

    public Room getWest() {
        return west;
    }

    public void setWest(Room west) {
        this.west = west;
    }

    // Aggiungere un metodo per gestire la stanza su un altro piano
    public Room getLinkedRoom() {
        return linkedRoom;
    }

    public void setLinkedRoom(Room linkedRoom, CommandType dir) {
        this.linkedRoom = linkedRoom;  // Set della stanza su un altro 
        this.linkedDirection = dir;
    }

    public CommandType getLinkedDirection() {
        return linkedDirection;
    }

    // Getter per oggetti della stanza
    public List<AdvObject> getObjects() {
        return objects;
    }

    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return 83 * id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Room other = (Room) obj;
        return id == other.id;
    }

    // Getter e Setter per il look (descrizione per "guardare")
    public String getLook() {
        return look;
    }

    public void setLook(String look) {
        this.look = look;
    }

    // Metodo per ottenere un oggetto dalla stanza (dato un id)
    public AdvObject getObject(int id) {
        for (AdvObject o : objects) {
            if (o.getId() == id) {
                return o;
            }
        }
        return null;
    }
}
