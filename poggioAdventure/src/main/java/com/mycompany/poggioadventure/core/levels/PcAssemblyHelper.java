package com.mycompany.poggioadventure.core.levels;

import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.model.AdvObjectContainer;
import java.util.List;

/**
 * Helper per gestione assemblaggio PC con validazione ordine componenti.
 * 
 * <p>Centralizza la logica di assemblaggio del computer desktop utilizzata
 * sia per l'inserimento componenti (PutObserver) che per la verifica 
 * completamento (UseObserver e Level2State).
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
public class PcAssemblyHelper {

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

    /**
     * Verifica se il PC è assemblato correttamente con tutti i componenti nell'ordine giusto.
     * 
     * @param casePc Il contenitore del case PC da verificare
     * @return true se il PC è assemblato correttamente, false altrimenti
     */
    public static boolean isPcCorrectlyAssembled(AdvObjectContainer casePc) {
        if (casePc == null || casePc.getList().size() != CORRECT_ASSEMBLY_ORDER.length) {
            return false;
        }

        List<AdvObject> assembledComponents = casePc.getList();
        for (int i = 0; i < CORRECT_ASSEMBLY_ORDER.length; i++) {
            if (assembledComponents.get(i).getId() != CORRECT_ASSEMBLY_ORDER[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Verifica se l'assemblaggio è completato (tutti i componenti presenti).
     * 
     * @param casePc Il contenitore del case PC da verificare
     * @return true se tutti i componenti sono stati inseriti
     */
    public static boolean isAssemblyComplete(AdvObjectContainer casePc) {
        return casePc != null && casePc.getList().size() == CORRECT_ASSEMBLY_ORDER.length;
    }

    /**
     * Ottiene il componente atteso per la prossima fase di assemblaggio.
     * 
     * @param casePc Il contenitore del case PC
     * @return ID del componente atteso, o -1 se assemblaggio completo
     */
    public static int getNextExpectedComponentId(AdvObjectContainer casePc) {
        if (casePc == null) {
            return CORRECT_ASSEMBLY_ORDER[0];
        }

        int nextIndex = casePc.getList().size();
        if (nextIndex >= CORRECT_ASSEMBLY_ORDER.length) {
            return -1; // Assemblaggio completo
        }

        return CORRECT_ASSEMBLY_ORDER[nextIndex];
    }

    /**
     * Verifica se un componente è il prossimo atteso nell'ordine di assemblaggio.
     * 
     * @param componentId ID del componente da verificare
     * @param casePc Il contenitore del case PC
     * @return true se il componente è quello atteso
     */
    public static boolean isCorrectNextComponent(int componentId, AdvObjectContainer casePc) {
        return componentId == getNextExpectedComponentId(casePc);
    }

    /**
     * Ottiene nome user-friendly del componente.
     * 
     * @param componentId ID del componente
     * @return Nome leggibile del componente
     */
    public static String getComponentName(int componentId) {
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

    /**
     * Ottiene il numero totale di componenti necessari per l'assemblaggio.
     * 
     * @return Numero totale componenti
     */
    public static int getTotalComponentsCount() {
        return CORRECT_ASSEMBLY_ORDER.length;
    }

    /**
     * Ottiene l'ordine completo di assemblaggio come array.
     * 
     * @return Array con l'ordine corretto degli ID componenti
     */
    public static int[] getAssemblyOrder() {
        return CORRECT_ASSEMBLY_ORDER.clone();
    }
}
