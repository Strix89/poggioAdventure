package com.mycompany.poggioadventure.model;

import com.mycompany.poggioadventure.parser.CommandType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.Serializable;

/**
 * Rappresenta una stanza nel gioco di avventura.
 * Ogni stanza può contenere oggetti, essere collegata ad altre stanze,
 * e fornire descrizioni dinamiche basate sul suo stato.
 *
 * @author Strix89 & MikeRvsso
 */
public class Room implements Serializable {
    
    private static final long serialVersionUID = 123456789L;

    // --- ATTRIBUTI PRINCIPALI ---
    private final int id;                           // Identificatore univoco della stanza
    private String name;                            // Nome della stanza
    private String description;                     // Descrizione base della stanza
    private String baseLookDescription;             // Descrizione alternativa per il comando "guarda"
    private boolean hasBeenObserved = false;        // Flag per tracciare se il giocatore ha visitato la stanza
    private boolean isForbidden = false;            // Flag per stanze non accessibili
    private String imagePath = null;                // Percorso dell'immagine associata alla stanza

    // --- COLLEGAMENTI TRA STANZE ---
    private Room south = null;                      // Stanza a sud
    private Room north = null;                      // Stanza a nord  
    private Room east = null;                       // Stanza a est
    private Room west = null;                       // Stanza a ovest
    
    // --- COLLEGAMENTI SPECIALI (per collegare piani diversi) ---
    private Room linkedRoom = null;                 // Stanza collegata specialmente (es. scale)
    private CommandType linkedDirection = CommandType.NONE; // Direzione del collegamento speciale

    // --- GESTIONE OGGETTI ---
    private final List<AdvObject> objectsInRoom = new ArrayList<>();           // Lista oggetti nella stanza
    private final Map<Integer, String> objectLookLabels = new HashMap<>();     // Etichette personalizzate per oggetti

    // --- COSTRUTTORI ---
    
    /**
     * Costruttore base con solo ID
     */
    public Room(int id) {
        this.id = id;
    }

    /**
     * Costruttore con ID, nome e descrizione
     */
    public Room(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.baseLookDescription = null;
    }

    /**
     * Costruttore completo con descrizione personalizzata per il comando "guarda"
     */
    public Room(int id, String name, String description, String baseLook) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.baseLookDescription = baseLook;
    }

    // --- GETTER E SETTER ---
    
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
    public List<AdvObject> getObjects() { return objectsInRoom; }
    public boolean isForbidden() { return isForbidden; }
    public void setForbidden(boolean forbidden) { this.isForbidden = forbidden; }

    // --- GESTIONE OGGETTI ---

    /**
     * Aggiunge un oggetto alla stanza se non è già presente
     */
    public void addObject(AdvObject object) {
        if (object != null && !this.objectsInRoom.contains(object)) {
            this.objectsInRoom.add(object);
        }
    }

    /**
     * Aggiunge un oggetto con un'etichetta personalizzata per il comando "guarda"
     * Se l'etichetta è null o vuota, usa la descrizione dell'oggetto
     */
    public void addObject(AdvObject object, String lookLabel) {
        addObject(object);
        if (object != null && lookLabel != null && !lookLabel.trim().isEmpty()) {
            this.objectLookLabels.put(object.getId(), lookLabel.trim());
        } else if (lookLabel == null || lookLabel.trim().isEmpty()) {
            String description = object.getDescription();
            if (description != null && !description.trim().isEmpty()) {
                this.objectLookLabels.put(object.getId(), description.trim());
            }
        }
    }

    /**
     * Rimuove un oggetto dalla stanza e la sua etichetta associata
     */
    public boolean removeObject(AdvObject object) {
        if (object != null) {
            this.objectLookLabels.remove(object.getId());
            return this.objectsInRoom.remove(object);
        }
        return false;
    }
    
    /**
     * Cerca un oggetto per nome o alias
     */
    public AdvObject getObjectByName(String name) {
        if (name == null) return null;
        
        for (AdvObject obj : objectsInRoom) {
            // Controllo nome esatto
            if (obj.getName().equalsIgnoreCase(name)) {
                return obj;
            }
            // Controllo alias
            if (obj.getAlias() != null && obj.getAlias().contains(name.toLowerCase())) {
                return obj;
            }
        }
        return null;
    }

    /**
     * Cerca un oggetto per ID
     */
    public AdvObject getObject(int id) {
        for (AdvObject o : objectsInRoom) {
            if (o.getId() == id) {
                return o;
            }
        }
        return null;
    }
    
    /**
     * Imposta un'etichetta personalizzata per un oggetto
     */
    public void setObjectLookLabel(int objectId, String lookLabel) {
        if (lookLabel != null && !lookLabel.trim().isEmpty()) {
            this.objectLookLabels.put(objectId, lookLabel.trim());
        } else {
            this.objectLookLabels.remove(objectId);
        }
    }

    /**
     * Recupera l'etichetta personalizzata di un oggetto
     */
    public String getObjectLookLabel(int objectId) {
        return this.objectLookLabels.get(objectId);
    }

    /**
     * Modifica l'etichetta personalizzata di un oggetto esistente
     * @param objectId ID dell'oggetto
     * @param newLookLabel Nuova etichetta da assegnare
     * @return true se l'etichetta è stata modificata, false se l'oggetto non aveva un'etichetta
     */
    public boolean modifyObjectLookLabel(int objectId, String newLookLabel) {
        if (this.objectLookLabels.containsKey(objectId)) {
            if (newLookLabel != null && !newLookLabel.trim().isEmpty()) {
                this.objectLookLabels.put(objectId, newLookLabel.trim());
                return true;
            } else {
                // Se la nuova etichetta è vuota, rimuovi l'etichetta
                this.objectLookLabels.remove(objectId);
                return true;
            }
        }
        return false;
    }

    /**
     * Elimina completamente l'etichetta personalizzata di un oggetto
     * @param objectId ID dell'oggetto
     * @return true se l'etichetta è stata rimossa, false se non esisteva
     */
    public boolean removeObjectLookLabel(int objectId) {
        return this.objectLookLabels.remove(objectId) != null;
    }

    // --- METODO PRINCIPALE: DESCRIZIONE DINAMICA DELLA STANZA ---

    /**
     * Genera una descrizione completa della stanza includendo:
     * - Descrizione base o personalizzata
     * - Direzioni disponibili con nomi delle stanze (se osservate)
     * - Lista degli oggetti presenti
     * - Lista degli NPC presenti
     */
    public String getLook() {
        StringBuilder dynamicLook = new StringBuilder();

        // === DESCRIZIONE DELLA STANZA ===
        if (this.baseLookDescription != null && !this.baseLookDescription.trim().isEmpty()) {
            // Usa descrizione personalizzata se disponibile
            dynamicLook.append("\n").append(this.baseLookDescription).append("\n");
        } else if (this.description != null && !this.description.trim().isEmpty()) {
            // Usa descrizione standard
            dynamicLook.append(this.description).append("\n");
        } else {
            // Descrizione di fallback
            dynamicLook.append("Ti guardi intorno nella stanza '").append(this.name).append("'.").append("\n");
        }

        // === CONTROLLO DIREZIONI DISPONIBILI ===
        boolean hasNorth = this.north != null;
        boolean hasSouth = this.south != null;
        boolean hasEast = this.east != null;
        boolean hasWest = this.west != null;
        boolean hasLinked = this.linkedRoom != null && this.linkedDirection != CommandType.NONE;
        
        // === GENERAZIONE LISTA DIREZIONI ===
        if (hasNorth || hasSouth || hasEast || hasWest || hasLinked) {
            dynamicLook.append("\n[BRIGHT_YELLOW]Direzioni[/] disponibili:\n");
            
            List<String> directions = new ArrayList<>();
            
            // NORD - Freccia su
            if (hasNorth) {
                String northText = "[BRIGHT_YELLOW]^^[/]";
                if (this.north.hasBeenObserved()) {
                    northText += " " + this.north.getName();
                }
                directions.add(northText);
            }
            
            // SUD - Freccia giù
            if (hasSouth) {
                String southText = "[BRIGHT_YELLOW]vv[/]";
                if (this.south.hasBeenObserved()) {
                    southText += " " + this.south.getName();
                }
                directions.add(southText);
            }
            
            // OVEST - Freccia sinistra
            if (hasWest) {
                String westText = "[BRIGHT_YELLOW]<<[/]";
                if (this.west.hasBeenObserved()) {
                    westText += " " + this.west.getName();
                }
                directions.add(westText);
            }
            
            // EST - Freccia destra
            if (hasEast) {
                String eastText = "[BRIGHT_YELLOW]>>[/]";
                if (this.east.hasBeenObserved()) {
                    eastText += " " + this.east.getName();
                }
                directions.add(eastText);
            }
            
            // COLLEGAMENTO SPECIALE - Con asterisco
            if (hasLinked) {
                String linkedText = "";
                switch (this.linkedDirection) {
                    case NORD:  linkedText = "[BRIGHT_CYAN]^^*[/]"; break;
                    case SOUTH: linkedText = "[BRIGHT_CYAN]vv*[/]"; break;
                    case EAST:  linkedText = "[BRIGHT_CYAN]>>*[/]"; break;
                    case WEST:  linkedText = "[BRIGHT_CYAN]<<*[/]"; break;
                    default: break;
                }
                if (!linkedText.isEmpty()) {
                    if (this.linkedRoom.hasBeenObserved()) {
                        linkedText += " " + this.linkedRoom.getName();
                    }
                    directions.add(linkedText);
                }
            }
            
            // Stampa direzioni formattate: | direzione | direzione | direzione |
            dynamicLook.append("[ORANGE]|[/] ")
                      .append(String.join(" [ORANGE]|[/] ", directions))
                      .append(" [ORANGE]|[/]")
                      .append("\n");
        }

        // === ELABORAZIONE OGGETTI E NPC ===
        List<String> itemDescriptions = new ArrayList<>();
        List<String> npcDescriptions = new ArrayList<>();

        for (AdvObject obj : this.objectsInRoom) {
            if (obj == null) continue;

            String name = obj.getName();
            String label = this.objectLookLabels.get(obj.getId());
            String displayText;

            // Distingui tra NPC e oggetti normali
            if (obj instanceof AdvNPC) {
                displayText = "-[NPC]" + name + "[/]";
                if (label != null) {
                    displayText += ": [ " + label + " ]";
                }
                npcDescriptions.add(displayText);
            } else {
                displayText = "-[ITEM]" + name + "[/]";
                if (label != null) {
                    displayText += ": [ " + label + " ]";
                }
                itemDescriptions.add(displayText);
            }
        }

        // === STAMPA OGGETTI ===
        if (!itemDescriptions.isEmpty()) {
            dynamicLook.append("\n[ITEM]Noti[/] i seguenti [ITEM]oggetti[/]: \n");
            dynamicLook.append(String.join(", ", itemDescriptions));
            dynamicLook.append(!npcDescriptions.isEmpty() ? "\n" : "");
        }

        // === STAMPA NPC ===
        if (!npcDescriptions.isEmpty()) {
            dynamicLook.append("\n[NPC]Noti[/] delle [NPC]persone[/] qui: \n");
            dynamicLook.append(String.join(", ", npcDescriptions));
        }

        // === MESSAGGIO DI FALLBACK ===
        if (itemDescriptions.isEmpty() && npcDescriptions.isEmpty()) {
            if ((this.baseLookDescription == null || this.baseLookDescription.trim().isEmpty()) &&
                (this.description != null && this.description.toLowerCase().contains("stanza"))) {
                dynamicLook.append("\nAl momento, non sembra esserci nient'altro di particolare.");
            }
        }
        
        return dynamicLook.toString().trim();
    }

    // --- METODI DI UTILITÀ ---

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
}