package com.mycompany.poggioadventure.core;

import com.mycompany.poggioadventure.model.Room;
import com.mycompany.poggioadventure.parser.CommandType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
     * Restituisce la stanza iniziale (es. ingresso primo piano).
     */
    public Room getStartingRoom() {
        return allFloors.get(0).get(0); // Prima stanza del primo piano
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

    // Metodo per aggiungere tutte le stanze (esempio)
    public void addRoomsToGameDescription() {

        // Primo piano (indice 0)
        Room entry = new Room(0, "Ingresso", "Ti trovi nell'ingresso di Poggiolevante e vedi un nano difronte a te..");
        Room hall = new Room(1, "Soggiorno", "Ti trovi nel soggiorno.");
        Room reception = new Room(2, "Portineria", "Ti trovi nella portineria.");
        Room corridor = new Room(3, "Corridoio", "Ti trovi nel corridoio del primo piano.");
        Room galileo = new Room(4, "Galileo", "Sei nella stanza Galileo");
        Room office = new Room(5, "Direzione", "Sei in direzione");
        // Secondo piano (indice 1)
        Room hallway = new Room(6, "Disimpegno", "Ti trovi al 2° piano in un disimpegno.");
        Room craftRoom = new Room(7, "Stanza di Pino", "Sei nel laboratorio di Pino.");
        Room entryLab = new Room(8, "Ingresso Laboratorio", "Ti trovi nell'ingresso del laboratorio.");
        Room lab5 = new Room(9, "Laboratorio 5", "Sei nel laboratorio 5.");
        Room corridorLab = new Room(10, "Corridoio Laboratorio", "Sei nel corridoio del laboratorio.");
        Room lab3D = new Room(11, "Laboratorio 3D", "Sei nel laboratorio per stampe 3D.");
        Room electronicsLab = new Room(12, "Laboratorio Elettronica", "Sei nel laboratorio di elettronica.");
        
        // Collegamenti primo piano
        entry.setWest(reception);
        reception.setEast(entry);
        entry.setNorth(hall);
        hall.setSouth(entry);
        hall.setWest(corridor);
        hall.setEast(galileo);
        corridor.setEast(hall);
        corridor.setWest(office);
        office.setEast(corridor);
        // Collegamenti secondo piano
        galileo.setWest(hall);
        hallway.setEast(craftRoom);
        craftRoom.setWest(hallway);
        hallway.setNorth(entryLab);
        entryLab.setSouth(hallway);
        entryLab.setEast(lab5);
        lab5.setWest(entryLab);
        entryLab.setNorth(corridorLab);
        corridorLab.setSouth(entryLab);
        corridorLab.setEast(lab3D);
        lab3D.setWest(corridorLab);
        corridorLab.setNorth(electronicsLab);
        electronicsLab.setSouth(corridorLab);
        // Aggiunta stanze ai piani
        addRoom(entry, 0);
        addRoom(hall, 0);
        addRoom(reception, 0);
        addRoom(corridor, 0);
        addRoom(galileo, 0);
        addRoom(office, 0);

        addRoom(hallway, 1);
        addRoom(craftRoom, 1);
        addRoom(entryLab, 1);
        addRoom(lab5, 1);
        addRoom(corridorLab, 1);
        addRoom(lab3D, 1);
        addRoom(electronicsLab, 1);

        // Collegamento tra i piani
        linkFloors(corridor, hallway, CommandType.NORD);
    }

    public List<List<Room>> getAllFloors() {
        return allFloors;
    }
}