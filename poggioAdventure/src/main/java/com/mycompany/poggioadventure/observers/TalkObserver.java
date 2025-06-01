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
     * Se l'NPC ha un test, lo avvia.
     * 
     * @param description La descrizione dello stato corrente del gioco.
     * @param parserOutput L'output del parser contenente il comando e gli oggetti associati.
     * @param gameContext Il contesto di gioco che fornisce accesso agli handler di I/O.
     * @return Una stringa contenente il messaggio di interazione con l'NPC o i risultati del test.
     */
    @Override
    public String update(GameDescription description, ParserOutput parserOutput, GameContext gameContext) {
        StringBuilder msg = new StringBuilder();

        if (parserOutput.getCommand().getType() == CommandType.TALK) {
            List<AdvObject> npcs = parserOutput.getRoomObjects();

            if (npcs.isEmpty()) {
                msg.append("\nCon chi vorresti parlare? Con il [LIME]muro[/]?");
                return msg.toString();
            }

            for (AdvObject obj : npcs) {
                if (obj instanceof AdvNPC) {
                    AdvNPC npc = (AdvNPC) obj;

                    if (npc.getImagePath() != null && !npc.getImagePath().isEmpty() && !npc.isObscureImage() && npc.getDialogue().size() != 0) {
                        msg.append("\nIMAGE:").append(npc.getImagePath()).append("\n");
                    } else {
                        msg.append("\n");
                    }

                    for (String line : npc.getDialogue()) {
                        msg.append("[NPC]").append(npc.getName()).append("[/]").append(": \"").append(line).append("\"\n");
                    }

                    if (!npc.hasInteracted() && !npc.getItemsToGive().isEmpty()) {
                        msg.append("\n[NPC]").append(npc.getName()).append("[/] ti dà:\n");
                        for (AdvObject item : npc.getItemsToGive()) {
                            description.getInventory().add(item);
                            msg.append("- ").append(item.getName()).append("\n");
                        }
                    }
                    npc.setHasInteracted(true);

                    if (npc.getTest() != null) {
                        if (npc.isTestCompleted()) {
                            msg.append("[NPC]" + npc.getName() + "[/]").append(": \"Hai già superato questo test!\"\n");
                        } else {
                            // Controlla se c'è una sessione fallita (anche se non più attiva)
                            if (npc.getActiveTestSession() != null && npc.getActiveTestSession().isFailed()) {
                                msg.append("[NPC]" + npc.getName() + "[/]").append(": \"Vuoi riprovare il test?\"\n");
                            }
                            
                            if (npc.startTestSession()) {
                                gameContext.getOutputHandler().writeln(msg.toString());
                                msg.setLength(0);

                                boolean testPassed = npc.getActiveTestSession().executeTest(
                                    gameContext.getOutputHandler(), 
                                    gameContext.getInputHandler());
                                
                                if (testPassed && npc.hasRewardObject()) {
                                    description.getInventory().add(npc.getRewardObject());
                                }
                            }
                        }
                    }
                } else {
                    msg.append("Fai uso di [LIME]pita[/] per caso? \nNon puoi parlare con [ITEM]").append(obj.getName()).append("[/]!\n");
                }
            }
        }
        return msg.toString();
    }
}