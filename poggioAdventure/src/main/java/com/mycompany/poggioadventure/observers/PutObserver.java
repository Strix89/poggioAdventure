package com.mycompany.poggioadventure.observers;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.utils.GameContext;
import com.mycompany.poggioadventure.core.levels.PcAssemblyHelper;
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
 * Utilizza {@link PcAssemblyHelper} per la logica condivisa di assemblaggio.
 */
public class PutObserver implements GameObserver, Serializable {

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
     * Gestisce assemblaggio PC con controllo ordine corretto utilizzando PcAssemblyHelper.
     */
    private String handlePcAssembly(AdvObject component, AdvObjectContainer casePC, GameDescription description) {
        // Controlla se assemblaggio è già completo
        if (PcAssemblyHelper.isAssemblyComplete(casePC)) {
            return "Il PC è già completamente assemblato!\n";
        }

        // Verifica se il componente è quello atteso
        if (PcAssemblyHelper.isCorrectNextComponent(component.getId(), casePC)) {
            // Ordine corretto - inserisci componente
            description.getInventory().remove(component);
            casePC.add(component);
            
            String componentName = PcAssemblyHelper.getComponentName(component.getId());
            StringBuilder result = new StringBuilder();
            result.append("Hai installato correttamente: ").append(componentName).append("\n");
            
            // Messaggio di progresso
            int currentCount = casePC.getList().size();
            int totalCount = PcAssemblyHelper.getTotalComponentsCount();
            result.append("Componenti installati: ").append(currentCount)
                  .append("/").append(totalCount).append("\n");
                    
            return result.toString();
            
        } else {
            // Ordine sbagliato - resetta assemblaggio
            resetAssembly(casePC, description);
            return "[RED]ORDINE ERRATO![/] L'assemblaggio è stato resettato.\n" +
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
}