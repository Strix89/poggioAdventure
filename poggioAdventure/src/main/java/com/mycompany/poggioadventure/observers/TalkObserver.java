package com.mycompany.poggioadventure.observers;

import java.util.List;

import com.mycompany.poggioadventure.core.GameContext;
import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.model.*;
import com.mycompany.poggioadventure.parser.*;
import java.io.Serializable;

/**
 * Classe che implementa l'interfaccia {@link GameObserver} per osservare e reagire a eventi di tipo "talk".
 * Questa classe gestisce la logica di dialogo con i personaggi non giocanti (NPC) nel gioco.
 */
public class TalkObserver implements GameObserver, Serializable {

    /**
     * Aggiorna lo stato del gioco in base al comando di tipo "talk".
     * Se il comando riguarda un NPC, mostra il dialogo dell'NPC e, se l'NPC non è stato ancora interagito,
     * fornisce oggetti al giocatore.
     * 
     * @param description La descrizione dello stato corrente del gioco.
     * @param parserOutput L'output del parser contenente il comando e gli oggetti associati.
     * @return Una stringa contenente il messaggio di interazione con l'NPC.
     */
    @Override
    public String update(GameDescription description, ParserOutput parserOutput, GameContext gameContext) {
        StringBuilder msg = new StringBuilder();

        // Verifica se il comando è di tipo TALK
        if (parserOutput.getCommand().getType() == CommandType.TALK) {
            List<AdvObject> npcs = parserOutput.getRoomObjects();

            // Se non ci sono oggetti NPC selezionati, chiedi con chi il giocatore vuole parlare
            if (npcs.isEmpty()) {
                msg.append("Con chi vorresti parlare?");
                return msg.toString();
            }

            // Itera su tutti gli NPC e gestisce la logica di dialogo
            for (AdvObject obj : npcs) {
                if (obj instanceof AdvNPC) {
                    AdvNPC npc = (AdvNPC) obj;
                    
                    // Aggiungi il percorso dell'immagine se presente
                    if (npc.getImagePath() != null && !npc.getImagePath().isEmpty() && !npc.isObscureImage()) {
                        msg.append("\nIMAGE:").append(npc.getImagePath()).append("\n");
                    } else {
                        msg.append("\n");
                    }

                    // Mostra il dialogo dell'NPC
                    for (String line : npc.getDialogue()) {
                        msg.append("[ORANGE]").append(npc.getName()).append("[/]").append(": \"").append(line).append("\"\n");
                    }

                    // Se l'NPC non è stato ancora interagito e ha oggetti da dare
                    if (!npc.hasInteracted() && !npc.getItemsToGive().isEmpty()) {
                        // Aggiungi gli oggetti all'inventario del giocatore
                        for (AdvObject item : npc.getItemsToGive()) {
                            description.getInventory().add(item);
                            msg.append("\n").append("[ORANGE]").append(npc.getName())
                               .append("[/]")
                               .append(" ti ha dato: ")
                               .append(item.getName());
                        }
                        // Segna l'NPC come interagito
                        npc.setHasInteracted(true);
                    }
                } else {
                    // Se l'oggetto non è un NPC, il giocatore non può parlare con esso
                    msg.append("Non puoi parlare con").append(obj.getName()).append("!\n");
                }
            }
        }
        return msg.toString();
    }
    
    
}
