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

        // Verifica se il comando è di tipo TALK
        if (parserOutput.getCommand().getType() == CommandType.TALK) {
            List<AdvObject> npcs = parserOutput.getRoomObjects(); // Oggetti nella stanza che corrispondono al target del comando "parla"

            // Se non ci sono oggetti NPC selezionati (o l'oggetto non è un NPC), chiedi con chi il giocatore vuole parlare
            if (npcs.isEmpty()) {
                msg.append("Con chi vorresti parlare? Con il [LAVENDER]muro[/]?");
                return msg.toString();
            }

            // Itera su tutti gli NPC e gestisce la logica di dialogo
            // Normalmente ci si aspetta un solo NPC per comando "parla X"
            for (AdvObject obj : npcs) {
                if (obj instanceof AdvNPC) {
                    AdvNPC npc = (AdvNPC) obj;
                    
                    // Aggiungi il percorso dell'immagine se presente e non oscura
                    if (npc.getImagePath() != null && !npc.getImagePath().isEmpty() && !npc.isObscureImage()) {
                        msg.append("\nIMAGE:").append(npc.getImagePath()).append("\n");
                    } else {
                        msg.append("\n"); // Spaziatura
                    }

                    // Mostra il dialogo dell'NPC
                    for (String line : npc.getDialogue()) {
                        msg.append("[ORANGE]").append(npc.getName()).append("[/]").append(": \"").append(line).append("\"\n");
                    }

                    // Se l'NPC non è stato ancora interagito e ha oggetti da dare (prima del test)
                    if (!npc.hasInteracted() && !npc.getItemsToGive().isEmpty()) {
                        msg.append("\n").append(npc.getName()).append(" ti dà:\n");
                        for (AdvObject item : npc.getItemsToGive()) {
                            description.getInventory().add(item); // Aggiungi all'inventario del giocatore
                            msg.append("- ").append(item.getName()).append("\n");
                        }
                        // npc.getItemsToGive().clear(); // Opzionale: svuota la lista se gli oggetti sono dati una sola volta
                    }
                    npc.setHasInteracted(true); // Segna l'NPC come interagito

                    // Gestione del test
                    if (npc.hasTest()){ // hasTest() controlla se c'è un test E non è stato completato
                        if (npc.isTestCompleted()){ // Controlla specificamente se è già stato completato con successo
                            msg.append(npc.getName()).append(": Hai già superato questo test! Non c'è bisogno di rifarlo.\n");
                        } else if (npc.hasActiveTestSession() && npc.getActiveTestSession().isFailed()) {
                             msg.append(npc.getName()).append(": Hai fallito il test precedente. Vuoi riprovare?\n");
                             // Qui si potrebbe resettare la sessione o permettere di rifarla.
                             // Per ora, se fallito, startTestSession() dovrebbe crearne una nuova.
                             npc.clearTestSession(); // Pulisce la vecchia sessione fallita per permetterne una nuova
                             if (npc.startTestSession()) { // Tenta di avviare una nuova sessione
                                // Scrivi il dialogo accumulato finora PRIMA di bloccare con executeTest
                                gameContext.getOutputHandler().writeln(msg.toString());
                                msg.setLength(0); // Svuota msg perché il suo contenuto è stato stampato

                                boolean testPassed = npc.getActiveTestSession().executeTest(
                                    gameContext.getOutputHandler(), 
                                    gameContext.getInputHandler());
                                
                                // Gestisci la ricompensa se il test è stato superato
                                if (testPassed && npc.hasRewardObject()) {
                                    description.getInventory().add(npc.getRewardObject());
                                    msg.append("\n🎁 ").append(npc.getName())
                                       .append(" ti consegna: ").append(npc.getRewardObject().getName()).append("!\n");
                                    msg.append("L'oggetto è stato aggiunto al tuo inventario.\n");
                                } else if (!testPassed && npc.getActiveTestSession().isFailed()) {
                                    msg.append(npc.getName()).append(": Peccato, non hai superato il test.\n");
                                } else if (!testPassed) { // Abbandonato
                                     msg.append(npc.getName()).append(": Hai deciso di non completare il test.\n");
                                }
                                // La sessione viene implicitamente pulita o gestita da TestSession o da una nuova chiamata a startTestSession
                                // npc.clearTestSession(); // Non necessario qui se startTestSession gestisce la sovrascrittura o TestSession si disattiva
                            }
                        } else if (npc.startTestSession()) { // Avvia il test se non completato e non fallito attivamente o se resettato
                            // Scrivi il dialogo accumulato finora PRIMA di bloccare con executeTest
                            gameContext.getOutputHandler().writeln(msg.toString());
                            msg.setLength(0); // Svuota msg perché il suo contenuto è stato stampato
                            
                            boolean testPassed = npc.getActiveTestSession().executeTest(
                                gameContext.getOutputHandler(), 
                                gameContext.getInputHandler());
                            
                            // Gestisci la ricompensa se il test è stato superato
                            if (testPassed && npc.hasRewardObject()) {
                                description.getInventory().add(npc.getRewardObject());
                                msg.append("\n🎁 ").append(npc.getName())
                                   .append(" ti consegna: ").append(npc.getRewardObject().getName()).append("!\n");
                                msg.append("L'oggetto è stato aggiunto al tuo inventario.\n");
                            } else if (!testPassed && npc.getActiveTestSession().isFailed()) {
                                msg.append(npc.getName()).append(": Peccato, non hai superato il test.\n");
                            } else if (!testPassed) { // Abbandonato
                                 msg.append(npc.getName()).append(": Hai deciso di non completare il test.\n");
                            }
                            // La sessione viene gestita da TestSession (isActive, isCompleted, isFailed)
                            // npc.clearTestSession(); // Non pulire qui, lo stato della sessione (es. fallito) è in activeTestSession
                        }
                    } // fine gestione test
                } else {
                    // Se l'oggetto non è un NPC, il giocatore non può parlare con esso
                    msg.append("Fai uso di [LIME]pita[/] per caso? \nNon puoi parlare con ").append(obj.getName()).append("!\n");
                }
            } // fine loop for AdvObject
        } // fine if CommandType.TALK
        return msg.toString(); // Restituisce i messaggi accumulati (es. dialogo, risultato test, ricompensa)
    }
}