package com.mycompany.poggioadventure.observers;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.parser.ParserOutput;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.model.AdvObjectContainer;
import com.mycompany.poggioadventure.parser.CommandType;
import java.util.List;
import java.io.Serializable;

/**
 *
 * @author pierpaolo
 */
public class OpenObserver implements GameObserver, Serializable {
    /**
     * La classe OpenObserver gestisce l'apertura di oggetti, supportando:
     * - Più oggetti contemporaneamente (comandi multipli)
     * - Oggetti contenitori (AdvObjectContainer)
     *
     * MIGLIORAMENTO:
     * In questa versione, gli oggetti contenuti nei contenitori NON vengono più
     * rilasciati automaticamente nella stanza o nell'inventario.
     * Viene solo mostrato il contenuto, lasciando al giocatore la scelta se
     * raccoglierli esplicitamente tramite "prendi".
     *
     * Questo evita comportamenti indesiderati e rende il gameplay più controllato.
     */
    @Override
    public String update(GameDescription description, ParserOutput parserOutput) {
        StringBuilder msg = new StringBuilder();

        if (parserOutput.getCommand().getType() == CommandType.OPEN) {
            boolean interacted = false;

            // Unione degli oggetti della stanza e dell'inventario
            List<AdvObject> allObjects = parserOutput.getObjects();
            allObjects.addAll(parserOutput.getInvObjects());

            // Se non ci sono oggetti validi
            if (allObjects.isEmpty()) {
                msg.append("Non c'è niente da aprire qui.");
                return msg.toString();
            }

            // Itera su tutti gli oggetti individuati nel comando
            for (AdvObject obj : allObjects) {
                if (obj == null) continue;

                if (!obj.isOpenable()) {
                    msg.append("Non puoi aprire questo oggetto: ").append(obj.getName()).append("\n");
                    continue;
                }

                if (obj.isOpen()) {
                    msg.append(obj.getName()).append(" è già aperto.\n");
                    continue;
                }

                obj.setOpen(true);
                msg.append("Hai aperto: ").append(obj.getName()).append("\n");

                // Gestione dei contenitori
                if (obj instanceof AdvObjectContainer) {
                    AdvObjectContainer container = (AdvObjectContainer) obj;

                    // Mostra il contenuto del contenitore SENZA svuotarlo
                    if (container.getList().isEmpty()) {
                        msg.append("Ma non contiene nulla.\n");
                    } else {
                        msg.append(container.getName()).append(" contiene:\n");
                        for (AdvObject inner : container.getList()) {
                            msg.append(" - ").append(inner.getName()).append(": ").append(inner.getDescription()).append("\n");
                        }
                        msg.append("Puoi ora raccogliere questi oggetti se lo desideri.\n");
                    }
                }

                interacted = true;
            }

            // Se nessun oggetto è stato effettivamente aperto
            if (!interacted) {
                msg.append("Non ci sono oggetti che puoi aprire.");
            }
        }

        return msg.toString();
    }
}
