package com.mycompany.poggioadventure.model;

import com.mycompany.poggioadventure.parser.CommandType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.Serializable;

public class Room implements Serializable {
    
    private static final long serialVersionUID = 123456789L;

    private final int id;
    private String name;
    private String description; 
    private String baseLookDescription; 
    private boolean hasBeenObserved = false;
    private CommandType linkedDirection = CommandType.NONE;
    
    private Room south = null;
    private Room north = null;
    private Room east = null;
    private Room west = null;

    private final List<AdvObject> objectsInRoom = new ArrayList<>();
    private final Map<Integer, String> objectLookLabels = new HashMap<>(); // Usa l'ID dell'oggetto come chiave

    private Room linkedRoom = null;
    private String imagePath = null;

    // --- COSTRUTTORI ---
    public Room(int id) {
        this.id = id;
    }

    public Room(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.baseLookDescription = null;
    }

    public Room(int id, String name, String description, String baseLook) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.baseLookDescription = baseLook;
    }

    // --- GETTER E SETTER ESISTENTI (name, description, collegamenti stanze, etc.) ---
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean hasBeenObserved() { return hasBeenObserved; }
    public void setHasBeenObserved(boolean observed) { this.hasBeenObserved = observed; }
    public Room getSouth() { return south; }
    public void setSouth(Room south) { this.south = south; }
    public Room getNorth() { return north; }
    public void setNorth(Room north) { this.north = north; }
    public Room getEast() { return east; }
    public void setEast(Room east) { this.east = east; }
    public Room getWest() { return west; }
    public void setWest(Room west) { this.west = west; }
    public Room getLinkedRoom() { return linkedRoom; }
    public void setLinkedRoom(Room linkedRoom, CommandType dir) {
        this.linkedRoom = linkedRoom;
        this.linkedDirection = dir;
    }
    public CommandType getLinkedDirection() { return linkedDirection; }
    public int getId() { return id; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setBaseLookDescription(String baseLookText) { this.baseLookDescription = baseLookText; }
    public String getBaseLookDescription() { return this.baseLookDescription; }

    public List<AdvObject> getObjects() {
        return objectsInRoom;
    }

    /**
     * Aggiunge un oggetto alla stanza.
     * @param object L'oggetto da aggiungere.
     */
    public void addObject(AdvObject object) {
        if (object != null && !this.objectsInRoom.contains(object)) {
            this.objectsInRoom.add(object);
        }
    }

    /**
     * Aggiunge un oggetto alla stanza con un'etichetta opzionale per il comando "guarda".
     * @param object L'oggetto da aggiungere.
     * @param lookLabel L'etichetta da mostrare quando si guarda la stanza (può essere null).
     */
    public void addObject(AdvObject object, String lookLabel) {
        addObject(object); // Chiama il metodo addObject esistente
        if (object != null && lookLabel != null && !lookLabel.trim().isEmpty()) {
            this.objectLookLabels.put(object.getId(), lookLabel.trim());
        }
    }

    public boolean removeObject(AdvObject object) {
        if (object != null) {
            this.objectLookLabels.remove(object.getId()); // Rimuovi anche l'etichetta associata
            return this.objectsInRoom.remove(object);
        }
        return false;
    }
    
    public AdvObject getObjectByName(String name) {
        if (name == null) return null;
        for (AdvObject obj : objectsInRoom) {
            if (obj.getName().equalsIgnoreCase(name)) {
                return obj;
            }
            if (obj.getAlias() != null && obj.getAlias().contains(name.toLowerCase())) {
                return obj;
            }
        }
        return null;
    }
    
    /**
     * Imposta o aggiorna l'etichetta 'look' per un oggetto specifico in questa stanza.
     * Se l'oggetto non è nella stanza, l'etichetta non viene memorizzata (o potresti decidere di aggiungerlo).
     * @param objectId L'ID dell'oggetto.
     * @param lookLabel L'etichetta da mostrare. Se null o vuota, rimuove l'etichetta esistente.
     */
    public void setObjectLookLabel(int objectId, String lookLabel) {
        if (lookLabel != null && !lookLabel.trim().isEmpty()) {
            this.objectLookLabels.put(objectId, lookLabel.trim());
        } else {
            this.objectLookLabels.remove(objectId); // Rimuovi l'etichetta se è nulla o vuota
        }
    }

    /**
     * Recupera l'etichetta 'look' per un oggetto specifico.
     * @param objectId L'ID dell'oggetto.
     * @return L'etichetta, o null se non definita.
     */
    public String getObjectLookLabel(int objectId) {
        return this.objectLookLabels.get(objectId);
    }


    // --- METODO getLook() MODIFICATO ---
    public String getLook() { // Sovrascrivilo se estendi una classe base, altrimenti è un metodo normale
        StringBuilder dynamicLook = new StringBuilder();

        if (this.baseLookDescription != null && !this.baseLookDescription.trim().isEmpty()) {
            dynamicLook.append("\n").append(this.baseLookDescription).append("\n");
        } else if (this.description != null && !this.description.trim().isEmpty()) {
            dynamicLook.append(this.description).append("\n");
        } else {
            dynamicLook.append("Ti guardi intorno nella stanza '").append(this.name).append("'.").append("\n");
        }

        List<String> itemDescriptions = new ArrayList<>();
        List<String> npcDescriptions = new ArrayList<>();

        for (AdvObject obj : this.objectsInRoom) {
            if (obj == null) continue;

            String name = obj.getName();
            String label = this.objectLookLabels.get(obj.getId()); // Prendi l'etichetta dalla Map
            String displayText = name;

            if (label != null) {
                displayText += " (" + label + ")"; // Aggiungi l'etichetta al nome: "tavolo (coperto di cianfrusaglie)"
            }

            if (obj instanceof AdvNPC) {
                npcDescriptions.add(displayText);
            } else {
                itemDescriptions.add(displayText);
            }
        }

        if (!itemDescriptions.isEmpty()) {
            dynamicLook.append("\nNoti anche i seguenti oggetti: \n");
            dynamicLook.append(String.join(", ", itemDescriptions)); // String.join è più semplice di stream per questo
            dynamicLook.append(".");
        }

        if (!npcDescriptions.isEmpty()) {
            dynamicLook.append("\nCi sono delle persone qui: \n");
            dynamicLook.append(String.join(", ", npcDescriptions));
            dynamicLook.append(".");
        }

        if (itemDescriptions.isEmpty() && npcDescriptions.isEmpty()) {
            if ((this.baseLookDescription == null || this.baseLookDescription.trim().isEmpty()) &&
                (this.description != null && this.description.toLowerCase().contains("stanza"))) {
                 dynamicLook.append("\nAl momento, non sembra esserci nient'altro di particolare.");
            }
        }
        
        this.hasBeenObserved = true;
        return dynamicLook.toString().trim();
    }

    // --- METODI HASHCODE, EQUALS, E GETOBJECT(BY ID) ESISTENTI ---
    @Override
    public int hashCode() { return 83 * id; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Room other = (Room) obj;
        return id == other.id;
    }
    
    public AdvObject getObject(int id) { // Cerca per ID
        for (AdvObject o : objectsInRoom) {
            if (o.getId() == id) {
                return o;
            }
        }
        return null;
    }
}