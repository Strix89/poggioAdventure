package com.mycompany.poggioadventure.observers;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.utils.GameContext;
import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.parser.ParserOutput;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.model.AdvObjectContainer;
import com.mycompany.poggioadventure.parser.CommandType;
import java.io.Serializable;
import java.util.List;

/**
 * Observer per gestione comando PUT con logica assemblaggio PC.
 * 
 * <p>Gestisce l'inserimento di oggetti in contenitori con validazione
 * dell'ordine corretto per l'assemblaggio del PC nel Level2.
 * 
 * <p><b>Ordine assemblaggio richiesto:</b>
 * <ol>
 *   <li>Scheda madre</li>
 *   <li>RAM</li>
 *   <li>SSD</li>
 *   <li>CPU</li>
 *   <li>Pasta termica</li>
 *   <li>Dissipatore</li>
 *   <li>GPU</li>
 *   <li>Alimentatore</li>
 * </ol>
 */
public class PutObserver implements GameObserver, Serializable {

    /** Ordine corretto di assemblaggio PC */
    private static final int[] CORRECT_ASSEMBLY_ORDER = {
        Utils.OBJ_SCHEDA_MADRE_ID,
        Utils.OBJ_RAM_ID,
        Utils.OBJ_SSD_ID,
        Utils.OBJ_CPU_ID,
        Utils.OBJ_PASTA_TERMICA_ID,
        Utils.OBJ_DISSIPATORE_ID,
        Utils.OBJ_GPU_ID,
        Utils.OBJ_ALIMENTATORE_ID
    };

    @Override
    public String update(GameDescription description, ParserOutput parserOutput, GameContext gameContext) {
        StringBuilder msg = new StringBuilder();

        if (parserOutput.getCommand().getType() == CommandType.PUT) {
            List<AdvObject> inventoryObjects = parserOutput.getInvObjects();
            List<AdvObject> roomObjects = parserOutput.getRoomObjects();

            if (inventoryObjects.isEmpty()) {
                msg.append("Non hai nulla da mettere nell'inventario.");
                return msg.toString();
            }

            // Cerca contenitori nella stanza
            AdvObjectContainer targetContainer = findTargetContainer(roomObjects, description);
            
            if (targetContainer == null) {
                msg.append("Non vedo alcun contenitore dove mettere gli oggetti.");
                return msg.toString();
            }

            if (!targetContainer.isOpen()) {
                msg.append("Devi prima aprire ").append(targetContainer.getName()).append(".");
                return msg.toString();
            }

            boolean interacted = false;

            // Processa ogni oggetto da inserire
            for (AdvObject objToInsert : inventoryObjects) {
                if (objToInsert == null) continue;

                // Caso speciale: assemblaggio PC
                if (targetContainer.getId() == Utils.OBJ_CASE_PC_ID) {
                    String result = handlePcAssembly(objToInsert, targetContainer, description);
                    msg.append(result);
                    interacted = true;
                } else {
                    // Inserimento generico in altri contenitori
                    String result = handleGenericInsertion(objToInsert, targetContainer, description);
                    msg.append(result);
                    interacted = true;
                }
            }

            if (!interacted) {
                msg.append("Non puoi mettere questi oggetti qui.");
            }
        }

        return msg.toString();
    }

    /**
     * Gestisce assemblaggio PC con controllo ordine corretto
     */
    private String handlePcAssembly(AdvObject component, AdvObjectContainer casePC, GameDescription description) {
        List<AdvObject> currentComponents = casePC.getList();
        int nextExpectedIndex = currentComponents.size();

        // Controlla se abbiamo già tutti i componenti
        if (nextExpectedIndex >= CORRECT_ASSEMBLY_ORDER.length) {
            return "Il PC è già completamente assemblato!\n";
        }

        int expectedComponentId = CORRECT_ASSEMBLY_ORDER[nextExpectedIndex];

        if (component.getId() == expectedComponentId) {
            // Ordine corretto - inserisci componente
            description.getInventory().remove(component);
            casePC.add(component);
            
            String componentName = getComponentName(component.getId());
            StringBuilder result = new StringBuilder();
            result.append("Hai installato correttamente: ").append(componentName).append("\n");
            
            // Messaggio di progresso
            result.append("Componenti installati: ").append(nextExpectedIndex + 1)
                  .append("/").append(CORRECT_ASSEMBLY_ORDER.length).append("\n");
            
            // Controlla se assemblaggio è completo
            if (nextExpectedIndex + 1 == CORRECT_ASSEMBLY_ORDER.length) {
                result.append("\n[GREEN]ASSEMBLAGGIO COMPLETATO![/]\n");
                result.append("Hai assemblato con successo il computer desktop!\n");
            }
            
            return result.toString();
        } else {
            // Ordine sbagliato - resetta assemblaggio
            resetAssembly(casePC, description);
            return "❌ ORDINE ERRATO! L'assemblaggio è stato resettato.\n" +
                   "Devi seguire l'ordine corretto di installazione dei componenti.\n";
        }
    }

    /**
     * Gestisce inserimento generico in contenitori normali
     */
    private String handleGenericInsertion(AdvObject obj, AdvObjectContainer container, GameDescription description) {
        description.getInventory().remove(obj);
        container.add(obj);
        return "Hai messo " + obj.getName() + " in " + container.getName() + ".\n";
    }

    /**
     * Trova il contenitore target nella stanza
     */
    private AdvObjectContainer findTargetContainer(List<AdvObject> roomObjects, GameDescription description) {
        // Cerca prima negli oggetti specificati dal parser
        for (AdvObject obj : roomObjects) {
            if (obj instanceof AdvObjectContainer) {
                return (AdvObjectContainer) obj;
            }
        }
        
        // Fallback: cerca nella stanza corrente
        for (AdvObject obj : description.getCurrentRoom().getObjects()) {
            if (obj instanceof AdvObjectContainer container && container.isOpen()) {
                return container;
            }
        }
        
        return null;
    }

    /**
     * Resetta l'assemblaggio rimettendo tutti i componenti nell'inventario
     */
    private void resetAssembly(AdvObjectContainer casePC, GameDescription description) {
        List<AdvObject> components = casePC.getList();
        for (AdvObject component : components) {
            description.getInventory().add(component);
        }
        casePC.getList().clear();
    }

    /**
     * Ottiene nome user-friendly del componente
     */
    private String getComponentName(int componentId) {
        return switch (componentId) {
            case Utils.OBJ_SCHEDA_MADRE_ID -> "Scheda madre";
            case Utils.OBJ_RAM_ID -> "RAM";
            case Utils.OBJ_SSD_ID -> "SSD";
            case Utils.OBJ_CPU_ID -> "CPU";
            case Utils.OBJ_PASTA_TERMICA_ID -> "Pasta termica";
            case Utils.OBJ_DISSIPATORE_ID -> "Dissipatore";
            case Utils.OBJ_GPU_ID -> "GPU";
            case Utils.OBJ_ALIMENTATORE_ID -> "Alimentatore";
            default -> "Componente sconosciuto";
        };
    }
}