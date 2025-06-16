package com.mycompany.poggioadventure.core;

import com.mycompany.poggioadventure.model.AdvNPC;
import com.mycompany.poggioadventure.model.Room;
import com.mycompany.poggioadventure.parser.CommandType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestisce la struttura spaziale del mondo di gioco organizzato su più piani.
 * 
 * <p>Fornisce operazioni per:
 * <ul>
 *   <li>Aggiunta e ricerca di stanze per ID o nome</li>
 *   <li>Collegamento bidirezionale tra stanze</li>
 *   <li>Manipolazione batch degli NPC presenti</li>
 * </ul>
 * 
 * <p>La struttura interna utilizza una lista di piani, dove ogni piano
 * contiene le stanze associate. Supporta serializzazione per persistenza.
 */
public class GameMap implements Serializable {
    /** Struttura multi-piano: ogni elemento è un piano contenente le sue stanze */
    private final List<List<Room>> allFloors = new ArrayList<>();

    /**
     * Inizializza la mappa con due piani vuoti (piano terra e primo piano).
     * Comportamento di default mantenuto per compatibilità.
     */
    public GameMap() {
        this(2); // Inizializza con 2 piani per default
    }

    /**
     * Inizializza la mappa con un numero specificato di piani vuoti.
     * 
     * @param numberOfFloors Numero di piani da creare (minimo 1)
     * @throws IllegalArgumentException se numberOfFloors < 1
     */
    public GameMap(int numberOfFloors) {
        if (numberOfFloors < 1) {
            throw new IllegalArgumentException("Numero di piani deve essere almeno 1");
        }
        
        for (int i = 0; i < numberOfFloors; i++) {
            allFloors.add(new ArrayList<>());
        }
    }

    /**
     * Aggiunge una stanza a un piano specifico.
     * @param room Stanza da aggiungere
     * @param floorNumber Numero del piano (0-based)
     */
    public void addRoom(Room room, int floorNumber) {
        if (floorNumber >= 0 && floorNumber < allFloors.size()) {
            allFloors.get(floorNumber).add(room);
        } else {
            throw new IllegalArgumentException("Piano non valido: " + floorNumber);
        }
    }

    /**
     * Aggiunge un nuovo piano vuoto alla mappa.
     * Il nuovo piano viene aggiunto alla fine della lista.
     * 
     * @return Indice del piano appena aggiunto
     */
    public int addFloor() {
        allFloors.add(new ArrayList<>());
        return allFloors.size() - 1;
    }

    /**
     * Aggiunge un nuovo piano con stanze preconfigurate.
     * 
     * @param rooms Lista di stanze da aggiungere al nuovo piano
     * @return Indice del piano appena aggiunto
     * @throws IllegalArgumentException se rooms è null
     */
    public int addFloor(List<Room> rooms) {
        if (rooms == null) {
            throw new IllegalArgumentException("La lista di stanze non può essere null");
        }
        
        List<Room> newFloor = new ArrayList<>(rooms);
        allFloors.add(newFloor);
        return allFloors.size() - 1;
    }

    /**
     * Inserisce un piano vuoto a un indice specifico.
     * I piani successivi vengono spostati in avanti.
     * 
     * @param floorIndex Posizione dove inserire il nuovo piano
     * @throws IndexOutOfBoundsException se l'indice non è valido
     */
    public void insertFloor(int floorIndex) {
        if (floorIndex < 0 || floorIndex > allFloors.size()) {
            throw new IndexOutOfBoundsException("Indice piano non valido: " + floorIndex);
        }
        
        allFloors.add(floorIndex, new ArrayList<>());
    }

    /**
     * Inserisce un piano con stanze preconfigurate a un indice specifico.
     * 
     * @param floorIndex Posizione dove inserire il nuovo piano
     * @param rooms Lista di stanze per il nuovo piano
     * @throws IndexOutOfBoundsException se l'indice non è valido
     * @throws IllegalArgumentException se rooms è null
     */
    public void insertFloor(int floorIndex, List<Room> rooms) {
        if (floorIndex < 0 || floorIndex > allFloors.size()) {
            throw new IndexOutOfBoundsException("Indice piano non valido: " + floorIndex);
        }
        if (rooms == null) {
            throw new IllegalArgumentException("La lista di stanze non può essere null");
        }
        
        List<Room> newFloor = new ArrayList<>(rooms);
        allFloors.add(floorIndex, newFloor);
    }

    /**
     * Rimuove un piano dalla mappa.
     * Tutti i collegamenti tra le stanze del piano rimosso e quelle di altri piani
     * vengono automaticamente eliminati per mantenere la coerenza.
     * 
     * @param floorIndex Indice del piano da rimuovere
     * @return Lista delle stanze che erano nel piano rimosso
     * @throws IndexOutOfBoundsException se l'indice non è valido
     * @throws IllegalStateException se si tenta di rimuovere l'ultimo piano rimasto
     */
    public List<Room> removeFloor(int floorIndex) {
        if (floorIndex < 0 || floorIndex >= allFloors.size()) {
            throw new IndexOutOfBoundsException("Indice piano non valido: " + floorIndex);
        }
        if (allFloors.size() <= 1) {
            throw new IllegalStateException("Non è possibile rimuovere l'ultimo piano");
        }
        
        List<Room> removedFloor = allFloors.get(floorIndex);
        
        // Rimuovi tutti i collegamenti dalle stanze del piano rimosso
        clearFloorConnections(removedFloor);
        
        return allFloors.remove(floorIndex);
    }

    /**
     * Stabilisce collegamento bidirezionale tra due stanze.
     * 
     * @param sourceRoom Stanza di partenza
     * @param targetRoom Stanza di destinazione
     * @param dir Direzione del collegamento (deve essere cardinale)
     * @throws IllegalArgumentException se la direzione non è valida
     */
    public void linkFloors(Room sourceRoom, Room targetRoom, CommandType dir) {
        if (!dir.isDirection()) {
            throw new IllegalArgumentException("La direzione deve essere NORD, SUD, EST o OVEST");
        }
        sourceRoom.setLinkedRoom(targetRoom, dir);
        targetRoom.setLinkedRoom(sourceRoom, dir.getOpposite());
    }

    /**
     * Pulisce tutti i collegamenti di un insieme di stanze.
     * Metodo helper per mantenere la coerenza quando si rimuovono piani.
     * 
     * @param rooms Stanze di cui pulire i collegamenti
     */
    private void clearFloorConnections(List<Room> rooms) {
        for (Room room : rooms) {
            // Rimuovi collegamenti bidirezionali standard
            clearRoomConnection(room, room.getNorth());
            clearRoomConnection(room, room.getSouth());
            clearRoomConnection(room, room.getEast());
            clearRoomConnection(room, room.getWest());
            
            // Rimuovi collegamento speciale se presente
            if (room.getLinkedRoom() != null) {
                clearSpecialConnection(room, room.getLinkedRoom());
            }
        }
    }

    /**
     * Rimuove il collegamento bidirezionale tra due stanze.
     * 
     * @param room1 Prima stanza
     * @param room2 Seconda stanza (può essere null)
     */
    private void clearRoomConnection(Room room1, Room room2) {
        if (room2 == null) return;
        
        // Trova e rimuovi il collegamento da room2 verso room1
        if (room2.getNorth() == room1) room2.setNorth(null);
        if (room2.getSouth() == room1) room2.setSouth(null);
        if (room2.getEast() == room1) room2.setEast(null);
        if (room2.getWest() == room1) room2.setWest(null);
        
        // Rimuovi anche eventuali collegamenti speciali
        if (room2.getLinkedRoom() == room1) {
            room2.setLinkedRoom(null, CommandType.NONE);
        }
    }

    /**
     * Rimuove collegamento speciale bidirezionale.
     * 
     * @param room1 Prima stanza
     * @param room2 Seconda stanza
     */
    private void clearSpecialConnection(Room room1, Room room2) {
        if (room2.getLinkedRoom() == room1) {
            room2.setLinkedRoom(null, CommandType.NONE);
        }
        room1.setLinkedRoom(null, CommandType.NONE);
    }

    /**
     * Restituisce il numero totale di piani nella mappa.
     * 
     * @return Numero di piani
     */
    public int getFloorCount() {
        return allFloors.size();
    }


    /**
     * Verifica se un indice di piano è valido.
     * 
     * @param floorIndex Indice da verificare
     * @return true se l'indice è valido
     */
    public boolean isValidFloorIndex(int floorIndex) {
        return floorIndex >= 0 && floorIndex < allFloors.size();
    }


    /**
     * Restituisce una copia delle stanze di un piano specifico.
     * 
     * @param floorIndex Indice del piano
     * @return Nuova lista contenente le stanze del piano
     * @throws IndexOutOfBoundsException se l'indice non è valido
     */
    public List<Room> getFloorRooms(int floorIndex) {
        if (!isValidFloorIndex(floorIndex)) {
            throw new IndexOutOfBoundsException("Indice piano non valido: " + floorIndex);
        }
        
        return new ArrayList<>(allFloors.get(floorIndex));
    }

    /**
     * Pulisce tutti i piani rimuovendo tutte le stanze.
     * Mantiene la struttura dei piani ma li svuota.
     */
    public void clearAllFloors() {
        for (List<Room> floor : allFloors) {
            // Prima pulisci i collegamenti per mantenere coerenza
            clearFloorConnections(floor);
            floor.clear();
        }
    }

    /**
     * Svuota un piano specifico rimuovendo tutte le sue stanze.
     * 
     * @param floorIndex Indice del piano da svuotare
     * @throws IndexOutOfBoundsException se l'indice non è valido
     */
    public void clearFloor(int floorIndex) {
        if (!isValidFloorIndex(floorIndex)) {
            throw new IndexOutOfBoundsException("Indice piano non valido: " + floorIndex);
        }
        
        List<Room> floor = allFloors.get(floorIndex);
        clearFloorConnections(floor);
        floor.clear();
    }

    /**
     * Restituisce una stanza dato il suo nome.
     * @param name Nome della stanza da cercare
     * @return La stanza trovata o null se non esiste
     */
    public Room getRoomByName(String name) {
        for (List<Room> floor : allFloors) {
            for (Room room : floor) {
                if (room.getName().equalsIgnoreCase(name)) {
                    return room;
                }
            }
        }
        return null;
    }

    /**
     * Cerca una stanza per ID in tutti i piani della mappa.
     * 
     * @param roomId Identificativo univoco della stanza
     * @return La stanza trovata o null se non esiste
     */
    public Room findRoomById(int roomId) {
        for (List<Room> floor : allFloors) {
            for (Room room : floor) {
                if (room.getId() == roomId) {
                    return room;
                }
            }
        }
        return null;
    }

    public List<List<Room>> getAllFloors() {
        return allFloors;
    }
    
    /**
     * Modifica la visualizzazione delle immagini di tutti gli NPC nella mappa.
     * Utilizza stream processing per applicare la trasformazione in modo efficiente.
     * 
     * @param obscure true per oscurare le immagini, false per mostrarle normalmente
     */
    public void alterateNPCImages(boolean obscure) {
        allFloors.stream()
            .flatMap(List::stream) // Flattena tutte le stanze in un unico stream
            .forEach(room -> room.getObjects().stream()
                .filter(obj -> obj instanceof AdvNPC)
                .forEach(npc -> ((AdvNPC) npc).setObscureImage(obscure))
            );
    }
}