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
     */
    public GameMap() {
        allFloors.add(new ArrayList<>());
        allFloors.add(new ArrayList<>());
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