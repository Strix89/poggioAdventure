package com.mycompany.poggioadventure.observers;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.utils.GameContext;
import com.mycompany.poggioadventure.parser.ParserOutput;
import com.mycompany.poggioadventure.parser.CommandType;
import java.io.Serializable;

/**
 * Observer specializzato per gestione comando "guarda" con tracking esplorazione.
 * 
 * <p>Intercetta comandi LOOK_AT per fornire descrizioni dettagliate della stanza
 * corrente. Aggiorna automaticamente lo stato di esplorazione per abilitare
 * funzionalità dipendenti dalla conoscenza delle aree visitate.
 * 
 * <p><b>Responsabilità:</b>
 * <ul>
 *   <li>Generazione descrizioni dinamiche stanza corrente</li>
 *   <li>Tracking stato esplorazione per navigazione consapevole</li>
 *   <li>Fallback su messaggi standard per stanze non configurate</li>
 * </ul>
 * 
 * <p><b>Pattern:</b> Observer per reazione a comando specifico, State per
 * tracking esplorazione persistente.
 */
public class LookAtObserver implements GameObserver, Serializable {

    /**
     * Gestisce comando LOOK_AT fornendo descrizione dettagliata stanza corrente.
     * Marca la stanza come esplorata per abilitare riferimenti futuri nella
     * navigazione e aggiorna la mappa mentale del giocatore.
     * 
     * @param description Stato corrente mondo di gioco
     * @param parserOutput Comando parsato con tipo LOOK_AT
     * @param gameContext Contesto esecuzione (non utilizzato in questa implementazione)
     * @return Descrizione formattata della stanza o messaggio fallback
     */
    @Override
    public String update(GameDescription description, ParserOutput parserOutput, GameContext gameContext) {
        StringBuilder msg = new StringBuilder();
        if (parserOutput.getCommand().getType() == CommandType.LOOK_AT) {
            if (description.getCurrentRoom().getLook() != null) {
                msg.append(description.getCurrentRoom().getLook());
            } else {
                msg.append("Non c'è niente di interessante qui.");
            }
            // Marca stanza come esplorata per riferimenti futuri in navigazione
            description.getCurrentRoom().setHasBeenObserved(true);
        }
        return msg.toString();
    }
}
