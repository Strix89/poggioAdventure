package com.mycompany.poggioadventure.observers;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.utils.GameContext;
import com.mycompany.poggioadventure.parser.ParserOutput;
import com.mycompany.poggioadventure.parser.CommandType;
import java.io.Serializable;
import com.mycompany.poggioadventure.model.Room;

/**
 * Observer per gestione navigazione spaziale con supporto collegamenti multi-piano.
 * 
 * <p>Gestisce movimento del giocatore attraverso il mondo di gioco con validazione
 * di percorsi disponibili, controllo accessi e messaggi feedback appropriati.
 * Supporta sia collegamenti direzionali standard che collegamenti speciali tra piani.
 * 
 * <p><b>Funzionalità principali:</b>
 * <ul>
 *   <li>Validazione comandi direzionali (N/S/E/W)</li>
 *   <li>Gestione collegamenti standard tra stanze adiacenti</li>
 *   <li>Supporto collegamenti speciali (scale, ascensori)</li>
 *   <li>Controllo accessi per stanze bloccate/proibite</li>
 *   <li>Feedback contestuale per movimenti riusciti/falliti</li>
 * </ul>
 * 
 * <p><b>Pattern implementati:</b>
 * <ul>
 *   <li>Observer: reazione a comandi movimento</li>
 *   <li>Strategy: gestione differenziata collegamenti standard vs speciali</li>
 *   <li>Chain of Responsibility: priorità validazione collegamenti</li>
 * </ul>
 */
public class MoveObserver implements GameObserver, Serializable {

    /**
     * Gestisce comandi di movimento con validazione a cascata.
     * Priorità: collegamenti direzionali standard > collegamenti speciali.
     * Applica controlli accesso e aggiorna posizione giocatore.
     * 
     * @param description Stato mondo di gioco con posizione corrente
     * @param parserOutput Comando parsato con direzione richiesta
     * @param gameContext Contesto esecuzione (non utilizzato)
     * @return Messaggio feedback movimento (successo/fallimento)
     */
    @Override
    public String update(GameDescription description, ParserOutput parserOutput, GameContext gameContext) {
        CommandType direction = parserOutput.getCommand().getType();
        
        if (!direction.isDirection()) {
            return "";
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
                return "\nNon puoi andare li la stanza sembra essere [RED]bloccata[/] a chiave!";
            }
            description.setCurrentRoom(nextRoom);
            return "Hai cambiato piano! Sei ora in: " + currentRoom.getLinkedRoom().getName();
        }      
        return "\nNon puoi andare in quella direzione ([BRIGHT_YELLOW]" + parserOutput.getCommand().getName() + "[/])!\n[DARK_ORANGE]Soffri in silenzio...[/]";
    }

    /**
     * Risolve stanza adiacente per direzione specifica usando pattern matching.
     * Implementazione efficiente con switch expression per mappatura direzionale.
     * 
     * @param room Stanza corrente
     * @param dir Direzione richiesta
     * @return Stanza target o null se direzione non valida/collegamento assente
     */
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