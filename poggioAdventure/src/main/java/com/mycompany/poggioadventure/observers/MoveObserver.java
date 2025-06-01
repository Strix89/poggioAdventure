package com.mycompany.poggioadventure.observers;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.utils.GameContext;
import com.mycompany.poggioadventure.parser.ParserOutput;
import com.mycompany.poggioadventure.parser.CommandType;
import java.io.Serializable;
import com.mycompany.poggioadventure.model.Room;

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
    public String update(GameDescription description, ParserOutput parserOutput, GameContext gameContext) {
        CommandType direction = parserOutput.getCommand().getType();
        // Controlla se il comando è effettivamente una direzione
        if (!direction.isDirection()) {
            return ""; // Ignora comandi non di movimento
        }
        Room currentRoom = description.getCurrentRoom();
        
        // 1. Controlla prima le direzioni normali
        Room nextRoom = getRoomInDirection(currentRoom, direction);

        if(nextRoom != null && !nextRoom.isForbidden()) {
            description.setCurrentRoom(nextRoom);
            return "Ti sei spostato a " + nextRoom.getName() + ".";
        } else if (nextRoom != null && nextRoom.isForbidden()) {
            return "Non puoi andare li la stanza sembra essere bloccata a chiave!";
        }
        
        // 2. Controlla i collegamenti tra piani SOLO se:
        //    - È una direzione
        //    - C'è un collegamento
        //    - La direzione corrisponde
        if(direction.isDirection() && 
           currentRoom.getLinkedRoom() != null && 
           currentRoom.getLinkedDirection() == direction) {
            nextRoom = currentRoom.getLinkedRoom();

            if (nextRoom.isForbidden()) {
                return "Non puoi andare li la stanza sembra essere bloccata a chiave!";
            }
            description.setCurrentRoom(nextRoom);
            return "Hai cambiato piano! Sei ora in: " + currentRoom.getLinkedRoom().getName();
        }      
        return "\nNon puoi andare in quella direzione ([BRIGHT_YELLOW]" + parserOutput.getCommand().getName() + "[/])!\n[DARK_ORANGE]Soffri in silenzio...[/]";
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