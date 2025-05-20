package com.mycompany.poggioadventure.observers;

import java.util.List;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.parser.ParserOutput;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.parser.CommandType;
import com.mycompany.poggioadventure.ui.OutputHandler;
import java.io.Serializable;

/**
 *
 * @author pierpaolo
 */
public class PickUpObserver implements GameObserver, Serializable {

    /**
     * Observer che verifica il comando di raccoglimento di un oggetto
     * verifica del Comando: Controlla se il comando ricevuto è di tipo PICK_UP (raccogli).
     * Controllo Oggetto: Se l'oggetto è presente nella stanza e può essere raccolto, viene aggiunto all'inventario del giocatore e rimosso dalla stanza.
     * Aggiornamento della Descrizione della Stanza: Se l'oggetto raccolto era nella cucina o nella camera da letto, aggiorna la descrizione della stanza corrente.
     * Messaggi di Feedback: Restituisce messaggi informativi per indicare se l'oggetto è stato raccolto con successo o se non era possibile raccoglierlo.
     * 
     * @param description
     * @param parserOutput
     * @return
     */
    @Override
    public String update(GameDescription description, ParserOutput parserOutput, OutputHandler output) {
        StringBuilder msg = new StringBuilder();

        if (parserOutput.getCommand().getType() == CommandType.PICK_UP) {
            List<AdvObject> objectsToPick = parserOutput.getObjects();

            if (objectsToPick == null || objectsToPick.isEmpty()) {
                msg.append("Non c'è niente da raccogliere qui.");
            } else {
                for (AdvObject obj : objectsToPick) {
                    if (obj.isPickupable()) {
                        description.getInventory().add(obj);
                        description.getCurrentRoom().getObjects().remove(obj);
                        msg.append("Hai raccolto: ").append(obj.getDescription()).append("\n");

                        if (description.getCurrentRoom().getId() == 2) {
                            description.getCurrentRoom().setBaseLookDescription("La solita cucina...");
                        } else if (description.getCurrentRoom().getId() == 3) {
                            description.getCurrentRoom().setBaseLookDescription("Non c'è nulla di interessante qui.");
                        }
                    } else {
                        msg.append("Non puoi raccogliere questo oggetto: ").append(obj.getName()).append("\n");
                    }
                }
            }
        }

        return msg.toString();
    }

}
