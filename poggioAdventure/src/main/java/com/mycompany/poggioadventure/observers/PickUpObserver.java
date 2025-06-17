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
 * Observer per gestione comando "raccogli" con ricerca gerarchica in contenitori.
 * 
 * <p>Implementa logica di raccolta oggetti con priorità stanza diretta seguita
 * da ricerca in contenitori aperti. Valida proprietà "pickupable" e gestisce
 * trasferimento oggetti tra mondo di gioco e inventario giocatore.
 * 
 * <p><b>Algoritmo ricerca:</b>
 * <ul>
 *   <li>1. Ricerca diretta nella stanza corrente</li>
 *   <li>2. Ricerca ricorsiva in contenitori aperti</li>
 *   <li>3. Validazione proprietà raccoglibilità</li>
 *   <li>4. Trasferimento atomico oggetto trovato</li>
 * </ul>
 * 
 * <p><b>Funzionalità:</b>
 * <ul>
 *   <li>Supporto raccolta multipla in singolo comando</li>
 *   <li>Gestione contenitori annidati aperti</li>
 *   <li>Validazione e feedback granulare per oggetto</li>
 *   <li>Messaggi debug per troubleshooting ricerca</li>
 * </ul>
 * 
 */
public class PickUpObserver implements GameObserver, Serializable {

    /**
     * Gestisce comando PICK_UP con algoritmo ricerca gerarchica.
     * Implementa strategia ricerca prioritaria: oggetti diretti stanza prima,
     * poi ricerca in contenitori aperti con validazione proprietà raccoglibilità.
     * 
     * @param description Stato mondo per accesso stanza e inventario
     * @param parserOutput Comando parsato con oggetti target identificati
     * @param gameContext Contesto esecuzione (non utilizzato)
     * @return Messaggio aggregato risultati raccolta
     */
    @Override
    public String update(GameDescription description, ParserOutput parserOutput, GameContext gameContext) {
        StringBuilder msg = new StringBuilder();

        if (parserOutput.getCommand().getType() == CommandType.PICK_UP) {
            List<AdvObject> objectsToPick = parserOutput.getRoomObjects();

            if (objectsToPick == null || objectsToPick.isEmpty()) {
                msg.append("Non c'è niente da [NEON_ORANGE]raccogliere[/] con quel nome, [PINK]Cerebroleso[/].");
            } else {
                for (AdvObject obj : objectsToPick) {
                    boolean foundAndPicked = false;

                    // Priorità 1: ricerca diretta nella stanza corrente
                    if (description.getCurrentRoom().getObjects().contains(obj)) {
                        if (obj.isPickupable()) {
                            description.getCurrentRoom().getObjects().remove(obj);
                            description.getInventory().add(obj);
                            msg.append("Hai [NEON_ORANGE]raccolto[/]: ").append(obj.getDescription()).append("\n");
                            foundAndPicked = true;
                        } else {
                            msg.append("Non puoi [NEON_ORANGE]raccogliere[/] questo oggetto: [ITEM]").append(obj.getName()).append("[/]\n");
                            foundAndPicked = true;
                        }
                    } else {
                        // Priorità 2: ricerca in contenitori aperti della stanza
                        for (AdvObject containerObj : description.getCurrentRoom().getObjects()) {
                            if (containerObj instanceof AdvObjectContainer) {
                                msg.append("[NEON_ORANGE]_[/]Controllo in [ITEM]").append(containerObj.getName()).append("[/]\n");
                                AdvObjectContainer container = (AdvObjectContainer) containerObj;
                                
                                // Verifica contenitore aperto e oggetto presente
                                if (container.isOpen() && container.getList().contains(obj)) {
                                    if (obj.isPickupable()) {
                                        container.getList().remove(obj);
                                        description.getInventory().add(obj);
                                        msg.append("Hai [NEON_ORANGE]raccolto[/]: ").append(obj.getDescription()).append("\n");
                                        foundAndPicked = true;
                                        break;
                                    } else {
                                        msg.append("Non puoi [NEON_ORANGE]raccogliere[/] questo oggetto: [ITEM]").append(obj.getName()).append("\n");
                                        foundAndPicked = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    // Fallback per oggetto non trovato in nessuna locazione
                    if (!foundAndPicked) {
                        msg.append("Non vedo [ITEM]").append(obj.getName()).append("[/] qui.\n");
                    }
                }
            }
        }
        return msg.toString();
    }
}
