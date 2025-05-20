package com.mycompany.poggioadventure.observers;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.parser.ParserOutput;
import com.mycompany.poggioadventure.parser.CommandType;
import java.io.Serializable;
import com.mycompany.poggioadventure.model.Room;
import com.mycompany.poggioadventure.ui.OutputHandler;

/**
 *
 * @author pierpaolo
 */
public class MoveObserver implements GameObserver, Serializable {

    /**
     * Observer che verifica il movimento del giocatore all'interno della mappa
     *
     * @param description
     * @param parserOutput
     * @return
     */
    @Override
    public String update(GameDescription description, ParserOutput parserOutput, OutputHandler output) {
        CommandType direction = parserOutput.getCommand().getType();
        // Controlla se il comando è effettivamente una direzione
        if (!direction.isDirection()) {
            return ""; // Ignora comandi non di movimento
        }
        Room currentRoom = description.getCurrentRoom();
        
        // 1. Controlla prima le direzioni normali
        Room nextRoom = getRoomInDirection(currentRoom, direction);

        if(nextRoom != null) {
            description.setCurrentRoom(nextRoom);
            return "Ti sei spostato a " + nextRoom.getName() + ".";
        }
        
        // 2. Controlla i collegamenti tra piani SOLO se:
        //    - È una direzione
        //    - C'è un collegamento
        //    - La direzione corrisponde
        if(direction.isDirection() && 
           currentRoom.getLinkedRoom() != null && 
           currentRoom.getLinkedDirection() == direction) {
            
            description.setCurrentRoom(currentRoom.getLinkedRoom());
            return "Hai cambiato piano! Sei ora in: " + currentRoom.getLinkedRoom().getName();
        }      
        return "\nNon puoi andare in quella direzione (" + parserOutput.getCommand().getName() + ")!\nSoffri in silenzio...";
    }

    private Room getRoomInDirection(Room room, CommandType dir) {
        if (!dir.isDirection()) return null;
        
        return switch (dir) {
            case NORD -> room.getNorth();
            case SOUTH -> room.getSouth();
            case EAST -> room.getEast();
            case WEST -> room.getWest();
            default -> null;
        };
    }
}