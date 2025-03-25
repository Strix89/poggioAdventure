package di.uniba.map.b.adventure.impl;

import di.uniba.map.b.adventure.GameDescription;
import di.uniba.map.b.adventure.parser.ParserOutput;
import di.uniba.map.b.adventure.type.CommandType;
import di.uniba.map.b.adventure.GameObserver;
import di.uniba.map.b.adventure.type.Room;

public class MoveObserver implements GameObserver {
    @Override
    public String update(GameDescription description, ParserOutput parserOutput) {
        CommandType direction = parserOutput.getCommand().getType();
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

        return "Non puoi andare in quella direzione!";
    }

    private Room getRoomInDirection(Room room, CommandType dir) {
        if (!dir.isDirection()) return null;
        
        switch(dir) {
            case NORD: return room.getNorth();
            case SOUTH: return room.getSouth();
            case EAST: return room.getEast();
            case WEST: return room.getWest();
            default: return null;
        }
    }
}