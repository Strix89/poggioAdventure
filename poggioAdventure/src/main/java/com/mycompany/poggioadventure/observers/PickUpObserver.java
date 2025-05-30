package com.mycompany.poggioadventure.observers;

import java.util.List;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.utils.GameContext;
import com.mycompany.poggioadventure.parser.ParserOutput;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.model.AdvObjectContainer;
import com.mycompany.poggioadventure.parser.CommandType;
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
    public String update(GameDescription description, ParserOutput parserOutput, GameContext gameContext) {
        StringBuilder msg = new StringBuilder();

        if (parserOutput.getCommand().getType() == CommandType.PICK_UP) {
            List<AdvObject> objectsToPick = parserOutput.getRoomObjects();

            if (objectsToPick == null || objectsToPick.isEmpty()) {
                msg.append("Non c'è niente da raccogliere qui.");
            } else {
                for (AdvObject obj : objectsToPick) {
                    boolean foundAndPicked = false;

                    // Provo prima a prendere l'oggetto direttamente dalla stanza
                    if (description.getCurrentRoom().getObjects().contains(obj)) {
                        if (obj.isPickupable()) {
                            description.getCurrentRoom().getObjects().remove(obj);
                            description.getInventory().add(obj);
                            msg.append("Hai raccolto: ").append(obj.getDescription()).append("\n");
                            foundAndPicked = true;
                        } else {
                            msg.append("Non puoi raccogliere questo oggetto: ").append(obj.getName()).append("\n");
                            foundAndPicked = true;
                        }
                    } else {
                        // Se non è in stanza, cerco dentro contenitori aperti
                        for (AdvObject containerObj : description.getCurrentRoom().getObjects()) {
                            if (containerObj instanceof AdvObjectContainer) {
                                msg.append("Controllo in ").append(containerObj.getName()).append("\n"); //DEBUG
                                AdvObjectContainer container = (AdvObjectContainer) containerObj;
                                if (container.isOpen() && container.getList().contains(obj)) {
                                    if (obj.isPickupable()) {
                                        container.getList().remove(obj);
                                        description.getInventory().add(obj);
                                        msg.append("Hai raccolto: ").append(obj.getDescription()).append("\n");
                                        foundAndPicked = true;
                                        break;
                                    } else {
                                        msg.append("Non puoi raccogliere questo oggetto: ").append(obj.getName()).append("\n");
                                        foundAndPicked = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (!foundAndPicked) {
                        msg.append("Non vedo ").append(obj.getName()).append(" qui.\n");
                    }
                }
            }
        }

        return msg.toString();
    }


}
