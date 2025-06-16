package com.mycompany.poggioadventure.observers;

import java.util.List;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.utils.GameContext;
import com.mycompany.poggioadventure.model.*;
import com.mycompany.poggioadventure.parser.*;

import java.io.Serializable;

/**
 * Observer per gestione dialoghi NPC con sistema test integrato e validazione requisiti.
 * 
 * <p>Gestisce interazioni sociali complete con personaggi non giocanti includendo
 * dialoghi contestuali, donazione oggetti, test interattivi e sistema ricompense.
 * Implementa validazione prerequisiti per test e gestione stato conversazioni.
 * 
 * <p><b>Funzionalità principali:</b>
 * <ul>
 *   <li>Sistema dialoghi con differenziazione primo incontro vs successivi</li>
 *   <li>Donazione automatica oggetti al primo incontro</li>
 *   <li>Validazione prerequisiti per test con feedback dettagliato</li>
 *   <li>Gestione sessioni test con retry automatico</li>
 *   <li>Sistema ricompense per test completati</li>
 *   <li>Supporto immagini NPC con controlli visibilità</li>
 * </ul>
 * 
 */
public class TalkObserver implements GameObserver, Serializable {

    /**
     * Gestisce comando TALK con flow completo dialogo-test-ricompensa.
     * Implementa logica sequenziale: dialogo > donazione oggetti (primo incontro) >
     * validazione prerequisiti test > esecuzione test > assegnazione ricompense.
     * 
     * @param description Stato mondo per accesso inventario e aggiornamenti
     * @param parserOutput Comando parsato con NPC target identificati
     * @param gameContext Contesto per handler I/O test interattivi
     * @return Messaggio dialogo aggregato con risultati interazioni
     */
    @Override
    public String update(GameDescription description, ParserOutput parserOutput, GameContext gameContext) {
        StringBuilder msg = new StringBuilder();

        if (parserOutput.getCommand().getType() == CommandType.TALK) {
            List<AdvObject> npcs = parserOutput.getRoomObjects();

            if (npcs.isEmpty()) {
                msg.append("\nCon chi vorresti parlare? Con il [LIME]muro[/]?" );
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
                            msg.append("- [ITEM]").append(item.getName()).append("[/]\n");
                        }
                    }
                    npc.setHasInteracted(true);

                    // Gestione sistema test con validazione prerequisiti
                    if (npc.getTest() != null) {
                        if (npc.isTestCompleted()) {
                            msg.append("[NPC]" + npc.getName() + "[/]").append(": \"Hai già [GREEN]superato[/] questo test!\"\n");
                        } else {
                            List<AdvObject> requiredObjects = npc.getTest().getRequiredObjects();
                            if (!hasAllRequiredObjects(description.getInventory(), requiredObjects)) {
                                // Feedback dettagliato oggetti mancanti
                                List<AdvObject> missingObjects = getMissingObjects(description.getInventory(), requiredObjects);
                                msg.append("[NPC]" + npc.getName() + "[/]").append(": \"Non puoi affrontare questo test senza gli [ITEM]oggetti[/] necessari!\"\n");
                                
                                if (!missingObjects.isEmpty()) {
                                    msg.append("Ti servono ancora: ");
                                    String formattedList = missingObjects.stream()
                                        .map(reqObject -> "[ITEM]" + reqObject.getName() + "[/]")
                                        .collect(java.util.stream.Collectors.joining(", "));
                                    msg.append(formattedList).append("\n");
                                } else {
                                    msg.append("Controlla il tuo [NEON_ORANGE]inventario[/].\n");
                                }
                            } else {
                                // Supporto retry per test falliti
                                if (npc.getActiveTestSession() != null && npc.getActiveTestSession().isFailed()) {
                                    msg.append("[NPC]" + npc.getName() + "[/]").append(": \"Vuoi [OLIVE]riprovare[/] il test?\"\n");
                                }
                                
                                // Esecuzione test con gestione ricompense
                                if (npc.startTestSession()) {
                                    gameContext.getOutputHandler().writeln(msg.toString());
                                    msg.setLength(0);

                                    boolean testPassed = npc.getActiveTestSession().executeTest(
                                        gameContext.getOutputHandler(), 
                                        gameContext.getInputHandler(),
                                        npc.getName());
                                    
                                    if (testPassed && npc.hasRewardObject()) {
                                        description.getInventory().add(npc.getRewardObject());
                                        npc.setTestCompleted(testPassed);
                                    }
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

    /**
     * Identifica oggetti mancanti per test tramite confronto ID con Stream API.
     * Utilizzato per feedback dettagliato prerequisiti non soddisfatti.
     * 
     * @param inventory Oggetti posseduti dal giocatore
     * @param requiredObjects Oggetti richiesti dal test
     * @return Lista oggetti mancanti con metadati completi
     */
    private List<AdvObject> getMissingObjects(List<AdvObject> inventory, List<AdvObject> requiredObjects) {
        if (requiredObjects == null || requiredObjects.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        return requiredObjects.stream()
            .filter(required -> inventory.stream()
                .noneMatch(invObj -> invObj.getId() == required.getId()))
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Validazione prerequisiti test tramite matching ID oggetti.
     * Implementazione efficiente con Stream API per controllo completezza.
     * 
     * @param inventory Oggetti posseduti dal giocatore
     * @param requiredObjects Oggetti richiesti per test
     * @return true se tutti i prerequisiti sono soddisfatti
     */
    private boolean hasAllRequiredObjects(List<AdvObject> inventory, List<AdvObject> requiredObjects) {
        if (requiredObjects == null || requiredObjects.isEmpty()) {
            return true;
        }
        
        return requiredObjects.stream()
            .allMatch(required -> inventory.stream()
                .anyMatch(invObj -> invObj.getId() == required.getId()));
    }
}