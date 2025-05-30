package com.mycompany.poggioadventure.core.levels;

import com.mycompany.poggioadventure.core.abstracts.IFlipperCommandProcessor;
import com.mycompany.poggioadventure.core.utils.FlipperResult;
import com.mycompany.poggioadventure.core.utils.GameContext;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Implementazione concreta della logica di elaborazione comandi per il Flipper Zero.
 * 
 * <p>Questa classe implementa la business logic per processare i comandi
 * inviati al dispositivo Flipper Zero virtuale nel gioco. Gestisce la validazione
 * dei comandi, la verifica delle frequenze radio e l'esecuzione delle operazioni.
 * 
 * <p><b>Responsabilità principali:</b>
 * <ul>
 *   <li>Parsing e validazione della sintassi dei comandi</li>
 *   <li>Verifica della corrispondenza frequenza-comando</li>
 *   <li>Esecuzione della logica di business per ogni comando</li>
 *   <li>Generazione di risultati tipizzati con effetti sul gioco</li>
 *   <li>Integrazione con il contesto di gioco per gli effetti</li>
 * </ul>
 * 
 * <p><b>Comandi supportati:</b>
 * <ul>
 *   <li><code>GoToRecharge (433.92 MHz)</code>: Invia robot alla ricarica - completa la sfida</li>
 *   <li><code>Override (868.0 MHz)</code>: Sovrascrive controlli - penalità -30s</li>
 *   <li><code>Stop (915.0 MHz)</code>: Ferma robot temporaneamente - bonus +60s</li>
 * </ul>
 * 
 * <p><b>Formato comandi:</b>
 * <pre>
 * [frequenza] [comando]
 * Esempio: 433.92 GoToRecharge
 * </pre>
 * 
 * <p><b>Pattern utilizzati:</b>
 * <ul>
 *   <li>Strategy: implementa IFlipperCommandProcessor</li>
 *   <li>Command: ogni comando ha logica di esecuzione specifica</li>
 *   <li>Factory Method: usa FlipperResult factory methods</li>
 * </ul>
 * 
 * @author Strix89
 * @version 1.2
 * @since 1.0
 */
public class FlipperCommandProcessor implements IFlipperCommandProcessor {
    
    /**
     * Mappa delle frequenze valide associate ai rispettivi comandi.
     * 
     * <p>Ogni comando del Flipper Zero deve essere inviato sulla frequenza
     * radio corretta per funzionare. Questa mappa definisce gli abbinamenti
     * validi comando-frequenza.
     */
    private static final Map<String, Double> VALID_FREQUENCIES = new HashMap<>();
    static {
        VALID_FREQUENCIES.put("GoToRecharge", 433.92);  // Comando principale - completa sfida
        VALID_FREQUENCIES.put("Override", 868.0);       // Comando rischioso - penalità
        VALID_FREQUENCIES.put("Stop", 915.0);           // Comando utilità - bonus
    }
    
    /**
     * Pattern regex per validare la sintassi dei comandi.
     * 
     * <p>Formato richiesto: [numero_con_decimali] [comando_alfanumerico]
     * <p>Esempi validi:
     * <ul>
     *   <li>433.92 GoToRecharge</li>
     *   <li>868 Override</li>
     *   <li>915.0 Stop</li>
     * </ul>
     */
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^(\\d+\\.?\\d*)\\s+(\\w+)$");
    
    /** Riferimento al contesto di gioco per applicare gli effetti */
    private final GameContext gameContext;
    
    /**
     * Costruttore che inizializza il processore con il contesto di gioco.
     * 
     * @param gameContext Contesto del gioco per applicare effetti e modifiche
     * @throws IllegalArgumentException se gameContext è null
     */
    public FlipperCommandProcessor(GameContext gameContext) {
        this.gameContext = gameContext;
    }
    
    /**
     * Processa un comando Flipper Zero completo dalla stringa di input.
     * 
     * <p><b>Flusso di elaborazione:</b>
     * <ol>
     *   <li>Validazione input non vuoto</li>
     *   <li>Parsing della sintassi con regex</li>
     *   <li>Estrazione e validazione della frequenza</li>
     *   <li>Validazione del comando</li>
     *   <li>Verifica corrispondenza frequenza-comando</li>
     *   <li>Esecuzione del comando specifico</li>
     * </ol>
     * 
     * @param input Stringa contenente il comando nel formato "[frequenza] [comando]"
     * @return FlipperResult contenente l'esito dell'operazione
     * 
     * @example
     * <pre>{@code
     * FlipperResult result = processor.processCommand("433.92 GoToRecharge");
     * if (result.getType() == FlipperResult.ResultType.SUCCESS) {
     *     // Gestisci successo
     *     if (result.isGameCompleted()) {
     *         // Sfida completata!
     *     }
     * }
     * }</pre>
     */
    @Override
    public FlipperResult processCommand(String input) {
        // Validazione input
        if (input == null || input.trim().isEmpty()) {
            return FlipperResult.error("Input vuoto. Formato richiesto: [frequenza] [comando]");
        }
        
        // Parsing sintassi con regex
        var matcher = COMMAND_PATTERN.matcher(input.trim());
        if (!matcher.matches()) {
            return FlipperResult.error("Formato non valido. Usa: [frequenza] [comando]\nEsempio: 433.92 GoToRecharge");
        }
        
        // Estrazione e validazione frequenza
        double frequency;
        try {
            frequency = Double.parseDouble(matcher.group(1));
        } catch (NumberFormatException e) {
            return FlipperResult.error("Frequenza non valida. Inserisci un numero decimale.");
        }
        
        // Estrazione comando
        String command = matcher.group(2);
        
        // Validazione comando esistente
        if (!VALID_FREQUENCIES.containsKey(command)) {
            return FlipperResult.error("Comando sconosciuto '" + command + "'.\nComandi disponibili: GoToRecharge, Override, Stop");
        }
        
        // Verifica corrispondenza frequenza-comando
        double expectedFreq = VALID_FREQUENCIES.get(command);
        if (Math.abs(frequency - expectedFreq) > 0.1) {
            return FlipperResult.error("Frequenza errata per '" + command + "'.\nFrequenza richiesta: " + expectedFreq + " MHz");
        }
        
        // Esecuzione comando validato
        return executeCommand(command);
    }
    
    /**
     * Esegue la logica specifica per un comando Flipper validato.
     * 
     * <p>Ogni comando ha effetti diversi sul gioco:
     * <ul>
     *   <li><b>GoToRecharge</b>: Completa la sfida, nessuna modifica timer</li>
     *   <li><b>Override</b>: Eseguito ma penalità di 30 secondi</li>
     *   <li><b>Stop</b>: Utilità con bonus di 60 secondi</li>
     * </ul>
     * 
     * @param command Nome del comando validato da eseguire
     * @return FlipperResult con l'esito specifico del comando
     */
    private FlipperResult executeCommand(String command) {
        switch (command) {
            case "GoToRecharge":
                // Comando principale - completa la sfida del Flipper
                notifyGameEngine("GoToRecharge");
                return FlipperResult.success(
                    "SUCCESSO: Robot reindirizzati alle stazioni di ricarica!\nI robot si stanno spegnendo... Prova completata!",
                    true, 0
                );
                
            case "Override":
                // Comando rischioso - funziona ma con penalità
                return FlipperResult.warning(
                    "ATTENZIONE: Override eseguito ma ha causato instabilità!\nPenalità: -30 secondi dal timer rimanente.",
                    -30
                );
                
            case "Stop":
                // Comando utilità - aiuta il giocatore
                return FlipperResult.info(
                    "INFO: Robot fermati temporaneamente.\nBonus: +1 minuto aggiunto al timer.",
                    60
                );
                
            default:
                // Fallback per comandi non implementati
                return FlipperResult.error("Comando non implementato.");
        }
    }
    
    /**
     * Notifica il motore di gioco per comandi che hanno effetti globali.
     * 
     * <p>Alcuni comandi del Flipper hanno effetti che vanno oltre il semplice
     * messaggio di risultato e devono essere comunicati al motore di gioco.
     * 
     * @param successfulCommand Nome del comando che ha avuto successo
     */
    private void notifyGameEngine(String successfulCommand) {
        if ("GoToRecharge".equals(successfulCommand)) {
            // Notifica completamento sfida al motore di gioco
            // Implementazione futura: integrazione con Engine
            // if (gameContext != null && gameContext.getEngine() != null) {
            //     gameContext.getEngine().completeChallenge3();
            // }
        }
    }
    
    /**
     * Restituisce informazioni di aiuto sui comandi disponibili.
     * 
     * <p>Genera una stringa formattata con il manuale d'uso del Flipper Zero,
     * includendo tutti i comandi disponibili, le loro frequenze e il formato richiesto.
     * 
     * @return Stringa con il manuale d'uso completo
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