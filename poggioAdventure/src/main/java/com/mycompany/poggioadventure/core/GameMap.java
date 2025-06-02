package com.mycompany.poggioadventure.core;

import com.mycompany.poggioadventure.model.AdvNPC;
import com.mycompany.poggioadventure.model.Room;
import com.mycompany.poggioadventure.parser.CommandType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author MikeRvsso
 */

public class GameMap implements Serializable{
    private final List<List<Room>> allFloors = new ArrayList<>(); // Lista di piani

    public GameMap() {
        allFloors.add(new ArrayList<>()); // Inizializza primo piano
        allFloors.add(new ArrayList<>()); // Inizializza secondo piano
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
     * Collega due stanze su piani diversi.
     * @param sourceRoom Stanza di partenza
     * @param targetRoom Stanza di arrivo
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

    public List<List<Room>> getAllFloors() {
        return allFloors;
    }
    
    /**
    * Rimuove le immagini da tutti gli NPC presenti nelle stanze del gioco.Utilizza espressioni lambda e stream per l'elaborazione.
     * @param obscure
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