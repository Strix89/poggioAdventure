/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package di.uniba.map.b.adventure.impl;

import di.uniba.map.b.adventure.GameDescription;
import di.uniba.map.b.adventure.parser.ParserOutput;
import di.uniba.map.b.adventure.type.CommandType;
import di.uniba.map.b.adventure.GameObserver;

/**
 *
 * @author pierpaolo
 */
public class MoveObserver implements GameObserver {

    /**
     * Observer che verifica il movimento del giocatore all'interno della mappa
     *
     * @param description
     * @param parserOutput
     * @return
     */
    @Override
    public String update(GameDescription description, ParserOutput parserOutput) {
        if (parserOutput.getCommand().getType() == CommandType.NORD) {  // controlla il comando inserito
            if (description.getCurrentRoom().getNorth() != null) { // controlla se la stanza a nord esiste
                description.setCurrentRoom(description.getCurrentRoom().getNorth()); // se esiste, la stanza corrente diventa quella a nord , ugualmente per gli altri casi
            } else {
                return "Da quella parte non si può andare c'è un muro!\nNon hai ancora acquisito i poteri per oltrepassare i muri...";
            }
        } else if (parserOutput.getCommand().getType() == CommandType.SOUTH) {
            if (description.getCurrentRoom().getSouth() != null) {
                description.setCurrentRoom(description.getCurrentRoom().getSouth());
            } else {
                return "Da quella parte non si può andare c'è un muro!\nNon hai ancora acquisito i poteri per oltrepassare i muri...";
            }
        } else if (parserOutput.getCommand().getType() == CommandType.EAST) {
            if (description.getCurrentRoom().getEast() != null) {
                description.setCurrentRoom(description.getCurrentRoom().getEast());
            } else {
                return "Da quella parte non si può andare c'è un muro!\nNon hai ancora acquisito i poteri per oltrepassare i muri...";
            }
        } else if (parserOutput.getCommand().getType() == CommandType.WEST) {
            if (description.getCurrentRoom().getWest() != null) {
                description.setCurrentRoom(description.getCurrentRoom().getWest());
            } else {
                return "Da quella parte non si può andare c'è un muro!\nNon hai ancora acquisito i poteri per oltrepassare i muri...";
            }
        }
        return "";
    }

}
