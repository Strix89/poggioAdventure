package com.mycompany.poggioadventure.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.model.AdvObjectContainer;
import com.mycompany.poggioadventure.model.Room;

/**
 * Classe che raccoglie utility methods e costanti di sistema.
 * Fornisce funzionalità generiche riutilizzabili in tutto il progetto.
 * 
 * @author pierpaolo | Strix89
 */
public class Utils { 
    // Codici di uscita standard per l'applicazione
    public static final int EXIT_CODE_SUCCESS = 0;       // Uscita normale
    public static final int EXIT_CODE_CRITICAL = 1;      // Errore critico
    public static final int EXIT_CODE_SAVE_ERROR = 2;    // Errore nei salvataggi
    public static final int EXIT_CODE_RESOURCE_ERROR = 3; // Errore nel caricamento risorse
    public static final int EXIT_CODE_LOG_ERROR = 4;     // Errore nei log
    public static final int EXIT_CODE_INITIALIZATION_ERROR = 5; // Input non valido
    
    public static final int RESET_LEVEL_ID = 998; // ID per l'oggetto di reset del livello
    public static final int LOSE_GAME_ID = 999; // ID per l'oggetto di sconfitta del gioco
    public static final int NEXT_LEVEL_ID = 1000; // ID per l'oggetto di transizione al livello successivo
    public static final int WIN_GAME_ID = 1001; // ID per l'oggetto di vittoria del gioco

    public static final long LEVEL_1_TIME_LIMIT = 10 * 60 * 1000; // Limite di tempo per il livello 1 (5 minuti in millisecondi)
    public static final List<Integer> LEVEL_1_REQUIRED_OBJECTS = List.of(1, 2, 3); // ID oggetti richiesti per il livello 1
    public static final List<Integer> LEVEL_1_FORBIDDEN_OBJECTS = List.of(4, 5); // ID oggetti proibiti per il livello 2

    // ==================== ID OGGETTI E NPC ====================
    
    // === NPC ===
    public static final int NPC_GENERIC_ID = 0;  // NPC generico
    public static final int NPC_GUIDO_ID = 1;    // Guido - nano portinaio
    public static final int NPC_DONMATTEO_ID = 28; // Don Matteo - prete
    public static final int NPC_DIRETTOREGALILEO_ID = 29; // Direttore in Galileo
    public static final int NPC_TUTOR_ID = 30; // Tutor 
    
    // === OGGETTI PRINCIPALI ===
    public static final int OBJ_POST_IT_ID = 2;        // Post-it con istruzioni
    public static final int OBJ_PENNA_ID = 3;          // Penna di Lorenzo Burdo
    public static final int OBJ_FOTO_ID = 4;           // Foto di San Nicola astronauta
    public static final int OBJ_ARMADIO_HALL_ID = 5;   // Armadio hall
    public static final int OBJ_CAPPOTTO_ID = 6;       // Cappotto di pelle
    public static final int OBJ_PANTALONI_ID = 7;      // Pantaloni sporchi
    public static final int OBJ_BASTONE_ID = 8;        // Bastone di legno
    public static final int OBJ_VETRINA_ID = 9;        // Vetrina di legno
    public static final int OBJ_STATUETTA_ID = 10;     // Statuetta di San Josemaria
    public static final int OBJ_BIBBIA_ID = 11;        // Bibbia antica
    public static final int OBJ_FOGLIO_GUIDA_ID = 12;  // Foglio guida per confessione
    public static final int OBJ_MICROSD_ID = 13;       // MicroSD
    public static final int OBJ_CHIAVI_AUTO_ID = 14;   // Chiavi macchina direttore
    public static final int OBJ_FORBICI_ID = 15;       // Forbici
    public static final int OBJ_MARTELLO_ID = 16;      // Martello
    public static final int OBJ_SEGA_CIRCOLARE_ID = 17; // Sega circolare
    public static final int OBJ_CONTENITORE_ID = 18;   // Contenitore elettronica
    public static final int OBJ_CPU_ID = 18;           // CPU (stesso ID del contenitore - errore nel codice originale)
    public static final int OBJ_CAVO_HDMI_ID = 19;     // Cavo HDMI
    public static final int OBJ_MOUSE_ID = 20;         // Mouse usurato
    public static final int OBJ_TASTIERA_ID = 21;      // Tastiera vecchia
    public static final int OBJ_RACK_ID = 22;          // Armadio rack
    public static final int OBJ_CHIAVE_RACK_ID = 23;   // Chiave rack
    public static final int OBJ_PULSANTE_ID = 24;      // Pulsante rosso
    public static final int OBJ_SET_CACCIAVITI_ID = 25; // Set cacciaviti precisione
    public static final int OBJ_SALDATORE_ID = 26;     // Saldatore a mano
    public static final int OBJ_BOBINA_PLA_ID = 27;    // Bobina filamento PLA
    
    // === OGGETTI SPECIALI ===
    public static final int OBJ_FLIPPER_ZERO_ID = 50;      // FlipperZero tecnomagico
    public static final int OBJ_LEVEL1_COMPLETE_ID = 100;  // Completamento livello 1
    
    // === OGGETTI SISTEMA ===
    public static final int OBJ_RESET_LEVEL_ID = 998;      // Reset livello
    public static final int OBJ_LOSE_GAME_ID = 999;        // Perdita gioco
    public static final int OBJ_NEXT_LEVEL_ID = 1000;      // Prossimo livello
    public static final int OBJ_WIN_GAME_ID = 1001;        // Vittoria gioco

    /**
     * Parsa una stringa rimuovendo le stopwords.
     * Converte la stringa in minuscolo e divide in token.
     * 
     * @param string La stringa da parsare
     * @param stopwords Set di parole da escludere
     * @return Lista di token filtrati
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
     *
     * @param inventory
     * @param id
     * @return
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
     * Termina l'applicazione con il codice di stato specificato.
     * 
     * @param statusCode Codice di uscita (vedi costanti EXIT_CODE_*)
     */
    public static void exitApplication(int statusCode) {
        System.exit(statusCode);
    }
    
    /**
     * Termina l'applicazione con codice di successo (0).
     * Overload del metodo principale per uscite normali.
     */
    public static void exitApplication() {
        exitApplication(EXIT_CODE_SUCCESS);
    }

    /**
     * Crea un oggetto AdvObject per il reset del livello.
     * Questo oggetto non è visibile e serve solo come segnaposto.
     * 
     * @return Un oggetto AdvObject per il reset del livello
     */
    public static AdvObject buildResetObject() {
        AdvObject resetAdvObject = new AdvObject(OBJ_RESET_LEVEL_ID, "Reset Livello");
        resetAdvObject.setVisible(false);
        return resetAdvObject;
    }

    /**
     * Crea un oggetto AdvObject per la vittoria del gioco.
     * Questo oggetto non è visibile e serve solo come segnaposto.
     * 
     * @return Un oggetto AdvObject per la vittoria del gioco
     */
    public static AdvObject buildWinGameObject() {
        AdvObject winAdvObject = new AdvObject(OBJ_WIN_GAME_ID, "Vittoria di Gioco");
        winAdvObject.setVisible(false);
        return winAdvObject;
    }

    /**
     * Crea un oggetto AdvObject per la perdita del gioco.
     * Questo oggetto non è visibile e serve solo come segnaposto.
     * 
     * @return Un oggetto AdvObject per la perdita del gioco
     */
    public static AdvObject buildLoseGameObject() {
        AdvObject loseAdvObject = new AdvObject(OBJ_LOSE_GAME_ID, "Perdita di Gioco");
        loseAdvObject.setVisible(false);
        return loseAdvObject;
    }

    /**
     * Crea un oggetto AdvObject per il passaggio al livello successivo.
     * Questo oggetto non è visibile e serve solo come segnaposto.
     * 
     * @return Un oggetto AdvObject per il passaggio al livello successivo
     */
    public static AdvObject buildNextLevelObject() {
        AdvObject nextLevelAdvObject = new AdvObject(OBJ_NEXT_LEVEL_ID, "Prossimo Livello");
        nextLevelAdvObject.setVisible(false);
        return nextLevelAdvObject;
    }

    /**
     * Verifica se l'utente ha tutti gli oggetti necessari nell'inventario.
     * 
     * @param inventory Lista degli oggetti nell'inventario del giocatore
     * @param requiredObjectIds Lista degli ID degli oggetti richiesti (può essere null o vuota)
     * @return true se ha tutti gli oggetti richiesti o se non ci sono oggetti richiesti
     */
    public static boolean hasRequiredObjects(List<AdvObject> inventory, List<Integer> requiredObjectIds) {
        // Se non ci sono oggetti richiesti, il test può essere effettuato
        return requiredObjectIds == null || requiredObjectIds.isEmpty() ||
               requiredObjectIds.stream()
                   .allMatch(requiredId -> inventory.stream()
                       .anyMatch(obj -> obj.getId() == requiredId));
    }

    /**
     * Restituisce una lista degli oggetti mancanti dall'inventario.
     * 
     * @param inventory Lista degli oggetti nell'inventario del giocatore
     * @param requiredObjectIds Lista degli ID degli oggetti richiesti
     * @return Lista degli ID degli oggetti mancanti
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
}