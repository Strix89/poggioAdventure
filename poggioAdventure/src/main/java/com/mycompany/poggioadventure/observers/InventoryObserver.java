package com.mycompany.poggioadventure.observers;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.parser.ParserOutput;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.parser.CommandType;
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
