package com.mycompany.poggioadventure.core.levels;

import com.mycompany.poggioadventure.core.abstracts.IFlipperCommandProcessor;
import com.mycompany.poggioadventure.core.utils.FlipperResult;
import com.mycompany.poggioadventure.core.utils.GameContext;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Processore di comandi per dispositivo Flipper Zero virtuale nel gioco.
 * 
 * <p>Gestisce parsing, validazione ed esecuzione di comandi radio per il controllo
 * di robot tramite frequenze specifiche. Ogni comando deve essere inviato sulla
 * frequenza corretta per essere eseguito con successo.
 * 
 * <p><b>Comandi supportati:</b>
 * <ul>
 *   <li><code>GoToRecharge (433.92 MHz)</code>: Completa la sfida</li>
 *   <li><code>Override (868.0 MHz)</code>: Penalità -30s</li>
 *   <li><code>Stop (915.0 MHz)</code>: Bonus +60s</li>
 * </ul>
 * 
 * <p>Formato richiesto: {@code [frequenza] [comando]}
 */
public class FlipperCommandProcessor implements IFlipperCommandProcessor {
    
    /** Mapping comando -> frequenza richiesta per validazione */
    private static final Map<String, Double> VALID_FREQUENCIES = new HashMap<>();
    static {
        VALID_FREQUENCIES.put("GoToRecharge", 433.92);
        VALID_FREQUENCIES.put("Override", 868.0);
        VALID_FREQUENCIES.put("Stop", 915.0);
    }
    
    /** Pattern per validazione formato: [numero] [comando] */
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^(\\d+\\.?\\d*)\\s+(\\w+)$");
    
    /** Contesto di gioco per applicazione effetti e modifiche di stato */
    private final GameContext gameContext;
    
    /**
     * Inizializza il processore con contesto di gioco per effetti globali.
     * 
     * @param gameContext Contesto per applicare modifiche timer e stato
     */
    public FlipperCommandProcessor(GameContext gameContext) {
        this.gameContext = gameContext;
    }
    
    /**
     * Elabora comando Flipper con validazione completa frequenza-comando.
     * 
     * @param input Comando nel formato "[frequenza] [comando]"
     * @return Risultato tipizzato con effetti sul gioco
     */
    @Override
    public FlipperResult processCommand(String input) {
        if (input == null || input.trim().isEmpty()) {
            return FlipperResult.error("Input vuoto. Formato richiesto: [frequenza] [comando]");
        }
        
        var matcher = COMMAND_PATTERN.matcher(input.trim());
        if (!matcher.matches()) {
            return FlipperResult.error("Formato non valido. Usa: [frequenza] [comando]\nEsempio: 433.92 GoToRecharge");
        }
        
        double frequency;
        try {
            frequency = Double.parseDouble(matcher.group(1));
        } catch (NumberFormatException e) {
            return FlipperResult.error("Frequenza non valida. Inserisci un numero decimale.");
        }
        
        String command = matcher.group(2);
        
        if (!VALID_FREQUENCIES.containsKey(command)) {
            return FlipperResult.error("Comando sconosciuto '" + command + "'.\nComandi disponibili: GoToRecharge, Override, Stop");
        }
        
        // Verifica corrispondenza frequenza-comando con tolleranza 0.1 MHz
        double expectedFreq = VALID_FREQUENCIES.get(command);
        if (Math.abs(frequency - expectedFreq) > 0.1) {
            return FlipperResult.error("Frequenza errata per '" + command + "'.\nFrequenza richiesta: " + expectedFreq + " MHz");
        }
        
        return executeCommand(command);
    }
    
    /**
     * Esegue logica specifica del comando validato con effetti differenziati.
     * Ogni comando ha conseguenze diverse: completamento, penalità o bonus.
     * 
     * @param command Nome del comando validato da eseguire
     * @return Risultato con messaggio e modifiche timer appropriate
     */
    private FlipperResult executeCommand(String command) {
        switch (command) {
            case "GoToRecharge":
                notifyGameEngine("GoToRecharge");
                return FlipperResult.success(
                    "SUCCESSO: Robot reindirizzati alle stazioni di ricarica!\nI robot si stanno spegnendo... Prova completata!",
                    true, 0
                );
                
            case "Override":
                return FlipperResult.warning(
                    "ATTENZIONE: Override eseguito ma ha causato instabilità!\nPenalità: -30 secondi dal timer rimanente.",
                    -30
                );
                
            case "Stop":
                return FlipperResult.info(
                    "INFO: Robot fermati temporaneamente.\nBonus: +1 minuto aggiunto al timer.",
                    60
                );
                
            default:
                return FlipperResult.error("Comando non implementato.");
        }
    }
    
    /** Notifica eventi globali al motore di gioco per comandi critici */
    private void notifyGameEngine(String successfulCommand) {
        if ("GoToRecharge".equals(successfulCommand)) {
            // TODO: Integrazione con Engine per completamento sfida
        }
    }
    
    /**
     * Restituisce documentazione completa dei comandi Flipper disponibili.
     * Include frequenze, effetti e formato richiesto per l'input.
     * 
     * @return Manuale d'uso formattato per display utente
     */
    @Override
    public String getManualInfo() {
        return "=== MANUALE FLIPPER ZERO ===\n\n" +
               "COMANDI DISPONIBILI:\n" +
               "• GoToRecharge (433.92 MHz) - Ricarica robot\n" +
               "• Override (868.0 MHz) - Sovrascrive controlli\n" +
               "• Stop (915.0 MHz) - Ferma temporaneamente\n\n" +
               "FORMATO: [frequenza] [comando]\n" +
               "ESEMPIO: 433.92 GoToRecharge\n";
    }
}