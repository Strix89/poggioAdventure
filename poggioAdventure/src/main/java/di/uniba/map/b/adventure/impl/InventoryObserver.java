/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package di.uniba.map.b.adventure.impl;

import di.uniba.map.b.adventure.GameDescription;
import di.uniba.map.b.adventure.parser.ParserOutput;
import di.uniba.map.b.adventure.type.AdvObject;
import di.uniba.map.b.adventure.type.CommandType;
import di.uniba.map.b.adventure.GameObserver;
import java.io.Serializable;

/**
 *
 * @author pierpaolo
 */
public class InventoryObserver implements GameObserver, Serializable {

    /**
     * Metodo che permette di visualizzare l'inventario del giocatore
     * Observer verifica che il comando sia di tipo INVENTORY e restituisce la lista degli oggetti presenti nell'inventario
     * Implementa GameObserver e aggiorna la descrizione del gioco
     * 
     * @param description
     * @param parserOutput
     * @return
     */
    @Override
    public String update(GameDescription description, ParserOutput parserOutput) {
        StringBuilder msg = new StringBuilder();
        if (parserOutput.getCommand().getType() == CommandType.INVENTORY) {
            if (description.getInventory().isEmpty()) {
                msg.append("Il tuo inventario è vuoto!");
            } else {
                
                msg.append("Nel tuo inventario ci sono:\n");
                for (AdvObject o : description.getInventory()) { //for each utilizzato per scorrere la lista degli oggetti presenti nell'inventario
                    msg.append(o.getName()).append(": ").append(o.getDescription()).append("\n"); //aggiunge alla stringa il nome e la descrizione dell'oggetto
                }
            }
        }
        return msg.toString();
    }

}
