/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.map.b.adventure;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author pierpaolo
 */
public class Utils {   
    // Codici di uscita standard
    public static final int EXIT_CODE_SUCCESS = 0;
    public static final int EXIT_CODE_CRITICAL = 1;
    public static final int EXIT_CODE_SAVE_ERROR = 2;
    public static final int EXIT_CODE_RESOURCE_ERROR = 3;
    /**
     *
     * @param string
     * @param stopwords
     * @return
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

   // Metodo centralizzato per la terminazione
    public static void exitApplication(int statusCode) {
        System.exit(statusCode);
    }
    
    // Metodo overload per uscita normale
    public static void exitApplication() {
        exitApplication(EXIT_CODE_SUCCESS);
    }
}
