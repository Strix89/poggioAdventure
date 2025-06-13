package com.mycompany.poggioadventure.observers;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.utils.GameContext;
import com.mycompany.poggioadventure.parser.ParserOutput;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.model.AdvObjectContainer;
import com.mycompany.poggioadventure.parser.CommandType;
import java.util.List;
import java.io.Serializable;

/**
 * Observer per gestione comando "apri" con supporto contenitori e comandi multipli.
 * 
 * <p>Gestisce apertura di oggetti apribili sia nell'inventario che nella stanza
 * corrente. Supporta contenitori con visualizzazione contenuto non invasiva,
 * lasciando al giocatore controllo esplicito su raccolta oggetti interni.
 * 
 * <p><b>Funzionalità principali:</b>
 * <ul>
 *   <li>Apertura multipla oggetti in singolo comando</li>
 *   <li>Validazione proprietà "openable" per sicurezza</li>
 *   <li>Gestione contenitori con ispezione contenuto</li>
 *   <li>Rimozione automatica etichette personalizzate post-apertura</li>
 *   <li>Feedback dettagliato per ogni oggetto processato</li>
 * </ul>
 * 
 * <p><b>Design decisions:</b>
 * <ul>
 *   <li>Contenitori mostrano contenuto senza svuotamento automatico</li>
 *   <li>Preserva controllo giocatore su raccolta oggetti</li>
 *   <li>Gestione robusta stati inconsistenti (già aperto)</li>
 * </ul>
 * 
 * <p><b>Pattern:</b> Observer per reazione comando OPEN, Strategy per 
 * handling differenziato oggetti standard vs contenitori.
 */
public class OpenObserver implements GameObserver, Serializable {
    
    /**
     * Gestisce comando OPEN con processing batch di oggetti target.
     * Unifica oggetti da stanza e inventario per maggiore flessibilità,
     * applica validazioni sequenziali e aggiorna stati appropriati.
     * 
     * @param description Stato mondo di gioco per accesso stanza corrente
     * @param parserOutput Comando parsato con oggetti target identificati
     * @param gameContext Contesto esecuzione (non utilizzato)
     * @return Messaggio aggregato risultati apertura
     */
    @Override
    public String update(GameDescription description, ParserOutput parserOutput, GameContext gameContext) {
        StringBuilder msg = new StringBuilder();

        if (parserOutput.getCommand().getType() == CommandType.OPEN) {
            boolean interacted = false;

            // Unificazione scope ricerca: stanza + inventario
            List<AdvObject> allObjects = parserOutput.getRoomObjects();
            allObjects.addAll(parserOutput.getInvObjects());

            if (allObjects.isEmpty()) {
                msg.append("Non c'è niente da [NEON_ORANGE]aprire[/] qui.");
                return msg.toString();
            }

            // Processing batch con validazione per oggetto
            for (AdvObject obj : allObjects) {
                if (obj == null) continue;

                // Validazione proprietà apertura
                if (!obj.isOpenable()) {
                    msg.append("Non puoi [NEON_ORANGE]aprire[/] questo oggetto: ").append(obj.getName()).append("\n");
                    continue;
                }

                // Controllo stato già aperto
                if (obj.isOpen()) {
                    msg.append(obj.getName()).append(" è già [NEON_ORANGE]aperto[/].\n");
                    continue;
                }

                // Aggiornamento stato e cleanup etichette
                obj.setOpen(true);
                description.getCurrentRoom().removeObjectLookLabel(obj.getId());
                msg.append("Hai [NEON_ORANGE]aperto[/]: ").append(obj.getName()).append("\n");

                // Gestione specializzata contenitori
                if (obj instanceof AdvObjectContainer) {
                    AdvObjectContainer container = (AdvObjectContainer) obj;

                    if (container.getList().isEmpty()) {
                        msg.append("Ma non contiene nulla.\n");
                    } else {
                        msg.append("[ITEM]"+ container.getName()).append("[/] contiene:\n");
                        for (AdvObject inner : container.getList()) {
                            msg.append(" - ").append("[ITEM]" + inner.getName()).append("[/]: ").append(inner.getDescription()).append("\n");
                        }
                    }
                }

                interacted = true;
            }

            // Fallback per nessuna apertura riuscita
            if (!interacted) {
                msg.append("Non ci sono oggetti che puoi [NEON_ORANGE]aprire[/].");
            }
        }

        return msg.toString();
    }
}
