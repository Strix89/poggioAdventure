package com.mycompany.poggioadventure.model;

import com.mycompany.poggioadventure.parser.CommandType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.Serializable;

/**
 * Rappresenta una stanza navigabile nel mondo di gioco con gestione dinamica contenuti.
 * 
 * <p>Fornisce funzionalità complete per navigazione spaziale, gestione oggetti/NPC,
 * descrizioni contestuali e collegamenti tra aree. Supporta serializzazione per
 * salvataggio stato e descrizioni adaptive basate su esplorazione giocatore.
 * 
 * <p><b>Funzionalità principali:</b>
 * <ul>
 *   <li>Sistema navigazione direzionale (N/S/E/W) con collegamenti speciali</li>
 *   <li>Gestione dinamica oggetti e NPC con etichette personalizzate</li>
 *   <li>Descrizioni contestuali basate su stato esplorazione</li>
 *   <li>Sistema contenitori con visualizzazione contenuto</li>
 *   <li>Controllo accesso con stanze proibite</li>
 *   <li>Supporto immagini per rappresentazione visuale</li>
 * </ul>
 * 
 * <p><b>Pattern implementati:</b>
 * <ul>
 *   <li>Composite: gestione oggetti e contenitori</li>
 *   <li>State: tracking esplorazione per descrizioni adaptive</li>
 *   <li>Strategy: rendering differenziato per oggetti vs NPC</li>
 * </ul>
 */
public class Room implements Serializable {
    
    private static final long serialVersionUID = 123456789L;

    /** Identificatore univoco immutabile della stanza */
    private final int id;
    
    /** Nome della stanza */
    private String name;
    
    /** Descrizione base della stanza */
    private String description;
    
    /** Descrizione alternativa per comando "guarda" */
    private String baseLookDescription;
    
    /** Flag tracking prima visita giocatore */
    private boolean hasBeenObserved = false;
    
    /** Flag controllo accesso (stanze bloccate) */
    private boolean isForbidden = false;
    
    /** Path immagine per rappresentazione visuale */
    private String imagePath = null;

    /** Collegamenti direzionali standard */
    private Room south = null;
    private Room north = null;
    private Room east = null;
    private Room west = null;
    
    /** Collegamento speciale (es. scale tra piani) */
    private Room linkedRoom = null;
    
    /** Direzione del collegamento speciale */
    private CommandType linkedDirection = CommandType.NONE;

    /** Oggetti presenti nella stanza */
    private final List<AdvObject> objectsInRoom = new ArrayList<>();
    
    /** Etichette personalizzate per oggetti nel comando "guarda" */
    private final Map<Integer, String> objectLookLabels = new HashMap<>();

    /** Costruttore minimale con solo ID */
    public Room(int id) {
        this.id = id;
    }

    /** Costruttore standard con identificazione e descrizione */
    public Room(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.baseLookDescription = null;
    }

    /** Costruttore completo con descrizione personalizzata per comando "guarda" */
    public Room(int id, String name, String description, String baseLook) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.baseLookDescription = baseLook;
    }

    // === GETTER E SETTER STANDARD ===
    
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
    
    /**
     * Configura collegamento speciale con direzione associata.
     * Utilizzato per scale, ascensori o passaggi non standard.
     */
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

    // === GESTIONE OGGETTI ===

    /**
     * Aggiunge oggetto evitando duplicati.
     * 
     * @param object Oggetto da aggiungere alla stanza
     */
    public void addObject(AdvObject object) {
        if (object != null && !this.objectsInRoom.contains(object)) {
            this.objectsInRoom.add(object);
        }
    }

    /**
     * Aggiunge oggetto con etichetta personalizzata per comando "guarda".
     * Fallback su descrizione oggetto se etichetta vuota.
     * 
     * @param object Oggetto da aggiungere
     * @param lookLabel Etichetta personalizzata (opzionale)
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
     * Rimuove oggetto e etichetta associata dalla stanza.
     * 
     * @param object Oggetto da rimuovere
     * @return true se rimosso con successo
     */
    public boolean removeObject(AdvObject object) {
        if (object != null) {
            this.objectLookLabels.remove(object.getId());
            return this.objectsInRoom.remove(object);
        }
        return false;
    }
    
    /** Rappresentazione debug della stanza */
    @Override
    public String toString() {
        return String.format("Room[id=%d, name='%s', objects=%d]", 
            id, name, objectsInRoom.size());
    }

    /**
     * Ricerca oggetto per nome o alias con matching flessibile.
     * 
     * @param name Nome o alias da cercare
     * @return Oggetto trovato o null
     */
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
     * Ricerca oggetto per ID specifico.
     * 
     * @param id ID oggetto cercato
     * @return Oggetto trovato o null
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
     * Configura etichetta personalizzata per oggetto specifico.
     * 
     * @param objectId ID oggetto target
     * @param lookLabel Nuova etichetta (null/vuoto per rimuovere)
     */
    public void setObjectLookLabel(int objectId, String lookLabel) {
        if (lookLabel != null && !lookLabel.trim().isEmpty()) {
            this.objectLookLabels.put(objectId, lookLabel.trim());
        } else {
            this.objectLookLabels.remove(objectId);
        }
    }

    /** Recupera etichetta personalizzata oggetto */
    public String getObjectLookLabel(int objectId) {
        return this.objectLookLabels.get(objectId);
    }

    /**
     * Modifica etichetta esistente con validazione.
     * 
     * @param objectId ID oggetto target
     * @param newLookLabel Nuova etichetta
     * @return true se modificata con successo
     */
    public boolean modifyObjectLookLabel(int objectId, String newLookLabel) {
        if (this.objectLookLabels.containsKey(objectId)) {
            if (newLookLabel != null && !newLookLabel.trim().isEmpty()) {
                this.objectLookLabels.put(objectId, newLookLabel.trim());
                return true;
            } else {
                this.objectLookLabels.remove(objectId);
                return true;
            }
        }
        return false;
    }

    /**
     * Elimina etichetta personalizzata oggetto.
     * 
     * @param objectId ID oggetto target
     * @return true se rimossa con successo
     */
    public boolean removeObjectLookLabel(int objectId) {
        return this.objectLookLabels.remove(objectId) != null;
    }

    // === SISTEMA DESCRIZIONE DINAMICA ===

    /**
     * Genera descrizione completa contextual della stanza.
     * Include descrizione base, direzioni disponibili con nomi stanze
     * esplorate, lista oggetti con etichette e NPC presenti.
     * 
     * @return Descrizione formattata con markup colori
     */
    public String getLook() {
        StringBuilder dynamicLook = new StringBuilder();

        // Descrizione base della stanza
        if (this.baseLookDescription != null && !this.baseLookDescription.trim().isEmpty()) {
            dynamicLook.append("\n").append(this.baseLookDescription).append("\n");
        } else if (this.description != null && !this.description.trim().isEmpty()) {
            dynamicLook.append(this.description).append("\n");
        } else {
            dynamicLook.append("Ti guardi intorno nella stanza '").append(this.name).append("'.").append("\n");
        }

        // Analisi direzioni disponibili
        boolean hasNorth = this.north != null;
        boolean hasSouth = this.south != null;
        boolean hasEast = this.east != null;
        boolean hasWest = this.west != null;
        boolean hasLinked = this.linkedRoom != null && this.linkedDirection != CommandType.NONE;
        
        // Generazione mappa direzioni con indicatori visivi
        if (hasNorth || hasSouth || hasEast || hasWest || hasLinked) {
            dynamicLook.append("\n[BRIGHT_YELLOW]Direzioni[/] disponibili:\n");
            
            List<String> directions = new ArrayList<>();
            
            // Direzioni standard con frecce
            if (hasNorth) {
                String northText = "[BRIGHT_YELLOW]^^[/]";
                if (this.north.hasBeenObserved()) {
                    northText += " " + this.north.getName();
                }
                directions.add(northText);
            }
            
            if (hasSouth) {
                String southText = "[BRIGHT_YELLOW]vv[/]";
                if (this.south.hasBeenObserved()) {
                    southText += " " + this.south.getName();
                }
                directions.add(southText);
            }
            
            if (hasWest) {
                String westText = "[BRIGHT_YELLOW]<<[/]";
                if (this.west.hasBeenObserved()) {
                    westText += " " + this.west.getName();
                }
                directions.add(westText);
            }
            
            if (hasEast) {
                String eastText = "[BRIGHT_YELLOW]>>[/]";
                if (this.east.hasBeenObserved()) {
                    eastText += " " + this.east.getName();
                }
                directions.add(eastText);
            }
            
            // Collegamenti speciali con asterisco distintivo
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
            
            // Formattazione stile tabella per direzioni
            dynamicLook.append("[ORANGE]|[/] ")
                      .append(String.join(" [ORANGE]|[/] ", directions))
                      .append(" [ORANGE]|[/]")
                      .append("\n");
        }

        // Categorizzazione oggetti per rendering differenziato
        List<String> itemDescriptions = new ArrayList<>();
        List<String> npcDescriptions = new ArrayList<>();

        for (AdvObject obj : this.objectsInRoom) {
            if (obj == null) continue;

            String name = obj.getName();
            String label = this.objectLookLabels.get(obj.getId());
            String displayText;

            // Rendering specifico per tipo oggetto
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
                
                // Espansione contenuto per contenitori aperti
                if (obj instanceof AdvObjectContainer) {
                    AdvObjectContainer container = (AdvObjectContainer) obj;
                    if (container.isOpen()) {
                        displayText += getContainerContentDescription(container);
                    }
                }
                
                itemDescriptions.add(displayText);
            }
        }

        // Sezione oggetti con formatting dedicato
        if (!itemDescriptions.isEmpty()) {
            dynamicLook.append("\n[ITEM]Noti[/] i seguenti [ITEM]oggetti[/]: \n");
            dynamicLook.append(String.join("\n", itemDescriptions));
            dynamicLook.append(!npcDescriptions.isEmpty() ? "\n" : "");
        }

        // Sezione NPC con formatting dedicato
        if (!npcDescriptions.isEmpty()) {
            dynamicLook.append("\n[NPC]Noti[/] delle [NPC]persone[/] qui: \n");
            dynamicLook.append(String.join("\n", npcDescriptions));
        }

        // Messaggio fallback per stanze vuote
        if (itemDescriptions.isEmpty() && npcDescriptions.isEmpty()) {
            if ((this.baseLookDescription == null || this.baseLookDescription.trim().isEmpty()) &&
                (this.description != null && this.description.toLowerCase().contains("stanza"))) {
                dynamicLook.append("\nAl momento, non sembra esserci nient'altro di particolare.");
            }
        }
        
        return dynamicLook.toString().trim();
    }

    /**
     * Genera descrizione dettagliata contenuto contenitore aperto.
     * Formatta lista oggetti interni con indentazione e markup.
     * 
     * @param container Contenitore di cui mostrare contenuto
     * @return Stringa formattata con contenuto
     */
    private String getContainerContentDescription(AdvObjectContainer container) {
        if (container.getList().isEmpty()) {
            return "\n  [OLIVE]Contiene[/]: nulla";
        }
        
        StringBuilder contentDesc = new StringBuilder(":");
        for (AdvObject innerObj : container.getList()) {
            contentDesc.append("\n    • [ITEM]")
                      .append(innerObj.getName())
                      .append("[/]: [ ")
                      .append(innerObj.getDescription() + " ]");
        }
        
        return contentDesc.toString();
    }

    // === OVERRIDE STANDARD ===

    /** Hash code basato su ID per collezioni efficienti */
    @Override
    public int hashCode() { 
        return 83 * id; 
    }

    /** Uguaglianza basata esclusivamente su ID */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Room other = (Room) obj;
        return id == other.id;
    }
}