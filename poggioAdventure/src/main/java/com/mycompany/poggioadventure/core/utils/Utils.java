package com.mycompany.poggioadventure.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mycompany.poggioadventure.model.AdvObject;

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
}