package com.mycompany.poggioadventure.core.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mycompany.poggioadventure.model.AdvObject;

/**
 * Libreria di utility centralizzata e costanti di sistema per PoggioAdventure.
 * 
 * <p>Raccoglie funzionalità generiche riutilizzabili, gestione risorse,
 * operazioni su collezioni e cloning profondo. Centralizza tutti gli
 * identificatori numerici per oggetti, NPC, stanze e stati di gioco.
 * 
 * <p><b>Responsabilità:</b>
 * <ul>
 *   <li>Definizione costanti ID sistema (oggetti, NPC, stanze)</li>
 *   <li>Configurazione livelli (tempi, oggetti richiesti/vietati)</li>
 *   <li>Utility parsing e validazione</li>
 *   <li>Gestione terminazione applicazione</li>
 *   <li>Cloning profondo per serializzazione</li>
 * </ul>
 */
public class Utils { 
    // ==================== CODICI USCITA APPLICAZIONE ====================
    
    /** Terminazione normale */
    public static final int EXIT_CODE_SUCCESS = 0;
    
    /** Errore critico non recuperabile */
    public static final int EXIT_CODE_CRITICAL = 1;
    
    /** Errore operazioni salvataggio */
    public static final int EXIT_CODE_SAVE_ERROR = 2;
    
    /** Errore caricamento risorse */
    public static final int EXIT_CODE_RESOURCE_ERROR = 3;
    
    /** Errore sistema logging */
    public static final int EXIT_CODE_LOG_ERROR = 4;
    
    /** Errore inizializzazione sistema */
    public static final int EXIT_CODE_INITIALIZATION_ERROR = 5;
    
    // ==================== ID OGGETTI SPECIALI SISTEMA ====================
    
    /** ID oggetto reset livello */
    public static final int RESET_LEVEL_ID = 998;
    
    /** ID oggetto sconfitta gioco */
    public static final int LOSE_GAME_ID = 999;
    
    /** ID oggetto transizione livello successivo */
    public static final int NEXT_LEVEL_ID = 1000;
    
    /** ID oggetto vittoria gioco */
    public static final int WIN_GAME_ID = 1001;

    /** ID oggetto perdita (alias per compatibilità) */
    public static final int OBJ_LOSE_GAME_ID = 999;

    // ==================== CONFIGURAZIONE LIVELLI ====================
    
    /** Limite tempo Livello 1: --> 10 min*/ 
    public static final long LEVEL_1_TIME_LIMIT = 20 * 60 * 1000;
    
    /** ID completamento Livello 1 */
    public static final int OBJ_LEVEL1_COMPLETE_ID = 100;
    
    /** Oggetti richiesti per superare Livello 1 */
    public static final List<Integer> LEVEL_1_REQUIRED_OBJECTS = List.of(OBJ_LEVEL1_COMPLETE_ID);
    
    /** Oggetti che causano fallimento Livello 1 */
    public static final List<Integer> LEVEL_1_FORBIDDEN_OBJECTS = List.of(OBJ_LOSE_GAME_ID);

    /** Limite tempo Livello 2: --> 15 min*/
    public static final long LEVEL_2_TIME_LIMIT = 20 * 60 * 1000;
    
    /** ID completamento Livello 2 */
    public static final int OBJ_LEVEL2_COMPLETE_ID = 200;
    
    /** Oggetti richiesti per superare Livello 2 */
    public static final List<Integer> LEVEL_2_REQUIRED_OBJECTS = List.of(OBJ_LEVEL2_COMPLETE_ID);
    
    /** Oggetti che causano fallimento Livello 2 */
    public static final List<Integer> LEVEL_2_FORBIDDEN_OBJECTS = List.of(OBJ_LOSE_GAME_ID);

    /** Limite tempo Livello 3: 10 minuti */
    public static final long LEVEL_3_TIME_LIMIT = 10 * 60 * 1000;
    
    /** ID completamento Livello 3 */
    public static final int OBJ_LEVEL3_COMPLETE_ID = 300;
    
    /** Oggetti richiesti per superare Livello 3 */
    public static final List<Integer> LEVEL_3_REQUIRED_OBJECTS = List.of(OBJ_LEVEL3_COMPLETE_ID);
    
    /** Oggetti che causano fallimento Livello 3 */
    public static final List<Integer> LEVEL_3_FORBIDDEN_OBJECTS = List.of(OBJ_LOSE_GAME_ID);

    // ==================== ID NPC ====================
    
    public static final int NPC_GENERIC_ID = 0;
    public static final int NPC_GUIDO_ID = 1;
    public static final int NPC_DONMATTEO_ID = 28;
    public static final int NPC_DIRETTOREGALILEO_ID = 29;
    public static final int NPC_TUTOR_ID = 30;
    public static final int NPC_PINO_ID = 31;
    public static final int NPC_LUIGI_ID = 32;
    public static final int NPC_DIRETTORE_LAB_ID = 42;
    public static final int NPC_LORENZO_ID = 44;
    
    // ==================== ID OGGETTI MONDO ====================
    
    public static final int OBJ_POST_IT_ID = 2;
    public static final int OBJ_PENNA_ID = 3;
    public static final int OBJ_FOTO_ID = 4;
    public static final int OBJ_ARMADIO_HALL_ID = 5;
    public static final int OBJ_CAPPOTTO_ID = 6;
    public static final int OBJ_PANTALONI_ID = 7;
    public static final int OBJ_BASTONE_ID = 8;
    public static final int OBJ_VETRINA_ID = 9;
    public static final int OBJ_STATUETTA_ID = 10;
    public static final int OBJ_BIBBIA_ID = 11;
    public static final int OBJ_FOGLIO_GUIDA_ID = 12;
    public static final int OBJ_MICROSD_ID = 13;
    public static final int OBJ_CHIAVI_AUTO_ID = 14;
    public static final int OBJ_FORBICI_ID = 15;
    public static final int OBJ_MARTELLO_ID = 16;
    public static final int OBJ_SEGA_CIRCOLARE_ID = 17;
    public static final int OBJ_CONTENITORE_ID = 18;
    public static final int OBJ_CAVO_HDMI_ID = 19;
    public static final int OBJ_MOUSE_ID = 20;
    public static final int OBJ_TASTIERA_ID = 21;
    public static final int OBJ_RACK_ID = 22;
    public static final int OBJ_CHIAVE_RACK_ID = 23;
    public static final int OBJ_PULSANTE_ID = 24;
    public static final int OBJ_SET_CACCIAVITI_ID = 25;
    public static final int OBJ_SALDATORE_ID = 26;
    public static final int OBJ_BOBINA_PLA_ID = 27;

    public static final int OBJ_CASE_PC_ID = 33;
    public static final int OBJ_CPU_ID = 34;
    public static final int OBJ_RAM_ID = 35;
    public static final int OBJ_SCHEDA_MADRE_ID = 36;
    public static final int OBJ_ALIMENTATORE_ID = 37;
    public static final int OBJ_PASTA_TERMICA_ID = 38;
    public static final int OBJ_DISSIPATORE_ID = 39;
    public static final int OBJ_GPU_ID = 40;
    public static final int OBJ_SSD_ID = 41;
    public static final int OBJ_POSTER_ID = 43;
    
    // ==================== ID OGGETTI SPECIALI ====================
    
    public static final int OBJ_FLIPPER_ZERO_ID = 50;

    // ==================== ID STANZE ====================
    
    // Piano Terra
    public static final int ROOM_ENTRY_ID = 0;
    public static final int ROOM_HALL_ID = 1;
    public static final int ROOM_RECEPTION_ID = 2;
    public static final int ROOM_CORRIDOR_ID = 3;
    public static final int ROOM_GALILEO_ID = 4;
    public static final int ROOM_OFFICE_ID = 5;
    
    // Primo Piano
    public static final int ROOM_HALLWAY_ID = 6;
    public static final int ROOM_CRAFT_ROOM_ID = 7;
    public static final int ROOM_ENTRY_LAB_ID = 8;
    public static final int ROOM_LAB5_ID = 9;
    public static final int ROOM_CORRIDOR_LAB_ID = 10;
    public static final int ROOM_LAB3D_ID = 11;
    public static final int ROOM_ELECTRONICS_LAB_ID = 12;

    /**
     * Parsing testuale con rimozione stopwords per comando naturale.
     * Normalizza input utente convertendo in minuscolo e filtrando parole comuni.
     * 
     * @param string Testo da analizzare
     * @param stopwords Set di parole da escludere
     * @return Lista token significativi
     */
    public static List<String> parseString(String string, Set<String> stopwords) {
        List<String> tokens = new ArrayList<>();
        String[] split = string.toLowerCase().split("\\s+");
        
        for (String t : split) {
            if (!stopwords.contains(t)) {
                tokens.add(t);
            }
        }
        return tokens;
    }

    /**
     * Ricerca oggetto nell'inventario per ID specifico.
     * 
     * @param inventory Lista oggetti inventario
     * @param id ID oggetto cercato
     * @return Oggetto trovato o null se assente
     */
    public static AdvObject getObjectFromInventory(List<AdvObject> inventory, int id) {
        for (AdvObject o : inventory) {
            if (o.getId() == id) {
                return o;
            }
        }
        return null;
    }

    /**
     * Terminazione controllata applicazione con codice stato specifico.
     * 
     * @param statusCode Codice uscita (vedi costanti EXIT_CODE_*)
     */
    public static void exitApplication(int statusCode) {
        System.exit(statusCode);
    }
    
    /** Terminazione normale con codice successo */
    public static void exitApplication() {
        exitApplication(EXIT_CODE_SUCCESS);
    }

    /**
     * Factory per oggetto perdita gioco generico.
     * Oggetto non visibile utilizzato internamente per gestione stati.
     * 
     * @return Oggetto AdvObject per perdita gioco
     */
    public static AdvObject buildLoseGameObject() {
        AdvObject loseAdvObject = new AdvObject(OBJ_LOSE_GAME_ID, "Perdita di Gioco");
        loseAdvObject.setVisible(false);
        return loseAdvObject;
    }

    /**
     * Validazione possesso oggetti richiesti per test/livelli.
     * Verifica che inventario contenga tutti gli ID necessari.
     * 
     * @param inventory Oggetti posseduti dal giocatore
     * @param requiredObjectIds Lista ID oggetti necessari (null = nessun requisito)
     * @return true se tutti i requisiti sono soddisfatti
     */
    public static boolean hasRequiredObjects(List<AdvObject> inventory, List<Integer> requiredObjectIds) {
        return requiredObjectIds == null || requiredObjectIds.isEmpty() ||
               requiredObjectIds.stream()
                   .allMatch(requiredId -> inventory.stream()
                       .anyMatch(obj -> obj.getId() == requiredId));
    }

    /**
     * Analisi gap oggetti mancanti per diagnostica.
     * Identifica quali oggetti richiesti non sono presenti nell'inventario.
     * 
     * @param inventory Oggetti posseduti
     * @param requiredObjectIds Oggetti necessari
     * @return Lista ID oggetti mancanti
     */
    public static List<Integer> getMissingObjects(List<AdvObject> inventory, List<Integer> requiredObjectIds) {
        if (requiredObjectIds == null || requiredObjectIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return requiredObjectIds.stream()
            .filter(requiredId -> inventory.stream()
                .noneMatch(obj -> obj.getId() == requiredId))
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Factory per oggetto perdita con nome personalizzato.
     * 
     * @param name Nome descrittivo per l'oggetto perdita
     * @return Oggetto AdvObject perdita configurato
     */
    public static AdvObject buildLoseGameObject(String name){
        AdvObject loseAdvObject = new AdvObject(OBJ_LOSE_GAME_ID, name);
        loseAdvObject.setVisible(false);
        return loseAdvObject;
    }

    /**
     * Cloning profondo via serializzazione Java per oggetti complessi.
     * Garantisce indipendenza completa tra oggetto originale e copia.
     * 
     * @param <T> Tipo oggetto (deve implementare Serializable)
     * @param obj Oggetto da clonare
     * @return Deep clone indipendente
     * @throws RuntimeException se serializzazione fallisce
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deepClone(T obj) {
        if (obj == null) return null;
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            
            oos.writeObject(obj);
            oos.flush();
            
            try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                
                return (T) ois.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Errore durante il cloning dell'oggetto: " + e.getMessage(), e);
        }
    }

    /**
     * Cloning profondo di liste con preservazione tipo.
     * 
     * @param <T> Tipo elementi lista
     * @param list Lista da clonare
     * @return Deep clone della lista
     */
    public static <T extends Serializable> List<T> cloneList(List<T> list) {
        if (list == null) return null;
        return deepClone(new ArrayList<>(list));
    }

    /**
     * Cloning profondo di mappe con preservazione tipi chiave-valore.
     * 
     * @param <K> Tipo chiavi
     * @param <V> Tipo valori
     * @param map Mappa da clonare
     * @return Deep clone della mappa
     */
    public static <K extends Serializable, V extends Serializable> Map<K, V> cloneMap(Map<K, V> map) {
        if (map == null) return null;
        return deepClone(new HashMap<>(map));
    }
}