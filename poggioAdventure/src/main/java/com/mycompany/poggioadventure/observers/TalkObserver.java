package com.mycompany.poggioadventure.observers;

import java.util.List;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.utils.GameContext;
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
                msg.append("Con chi vorresti parlare? Con il [LAVENDER]muro[/]?");
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
                        msg.append("\n").append(npc.getName()).append(" ti dà:\n");
                        for (AdvObject item : npc.getItemsToGive()) {
                            msg.append("- ").append(item.getName()).append("\n");
                        }
                    }
                    npc.setHasInteracted(true);

                    if (npc.hasTest()){
                        if (npc.isTestCompleted()){
                            msg.append(npc.getName()).append(": Hai già superato test! Non c'è bisogno di rifarlo.\n");
                        } else if (npc.startTestSession()) {
                            gameContext.getOutputHandler().writeln(msg.toString());
                            msg.setLength(0);
                            boolean testPassed = npc.getActiveTestSession().executeTest(
                                gameContext.getOutputHandler(), 
                                gameContext.getInputHandler());
                            
                            // Gestisci la ricompensa se il test è stato superato
                            if (testPassed && npc.hasRewardObject()) {
                                description.getInventory().add(npc.getRewardObject());
                                msg.append("\n🎁 ").append(npc.getName())
                                .append(" ti consegna: ").append(npc.getRewardObject().getName()).append("!\n");
                                msg.append("L'oggetto è stato aggiunto al tuo inventario.\n");
                            }
                            // Pulisci la sessione
                            npc.clearTestSession();
                        }
                    }
                } else {
                    // Se l'oggetto non è un NPC, il giocatore non può parlare con esso
                    msg.append("Fai uso di [LIME]pita[/] per caso? \nNon puoi parlare con").append(obj.getName()).append("!\n");
                }
            }
        }
        return msg.toString();
    }
}