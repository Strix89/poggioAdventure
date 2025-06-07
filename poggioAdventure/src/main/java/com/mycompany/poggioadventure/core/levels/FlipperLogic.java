package com.mycompany.poggioadventure.core.levels;

import com.mycompany.poggioadventure.core.abstracts.IFlipperCommandProcessor;
import com.mycompany.poggioadventure.core.utils.FlipperResult;
import com.mycompany.poggioadventure.core.utils.GameContext;
import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.ui.gui.views.UI_Flipper;

/**
 * Facade per interfacce multiple del Flipper Zero virtuale.
 * 
 * <p>Fornisce modalità di interazione sia CLI che GUI per il dispositivo
 * Flipper Zero del gioco, coordinando command processor e presentazione risultati.
 * 
 * <p><b>Modalità supportate:</b>
 * <ul>
 *   <li><b>GUI Mode</b>: Finestra dedicata con interfaccia grafica</li>
 *   <li><b>CLI Mode</b>: Sessione interattiva a linea di comando</li>
 * </ul>
 * 
 * <p>Gestisce automaticamente effetti visivi, feedback colorato e logging
 * per entrambe le modalità operative.
 */
public class FlipperLogic {
    
    /** Contesto di gioco per accesso ai componenti del sistema */
    private final GameContext gameContext;
    
    /** Processore dei comandi per la business logic del Flipper */
    private final IFlipperCommandProcessor commandProcessor;
    
    /**
     * Inizializza la logica Flipper con contesto di gioco.
     * Crea automaticamente il command processor standard.
     * 
     * @param gameContext Contesto con tutti i riferimenti necessari
     */
    public FlipperLogic(GameContext gameContext) {
        this.gameContext = gameContext;
        this.commandProcessor = new FlipperCommandProcessor(gameContext);
    }
    
    /**
     * Apre interfaccia grafica del Flipper Zero in finestra dedicata.
     * 
     * @return Istanza UI_Flipper creata e resa visibile
     */
    public UI_Flipper openGUIInterface() {
        UI_Flipper guiInterface = new UI_Flipper(gameContext, commandProcessor);
        guiInterface.setVisible(true);
        return guiInterface;
    }
    
    /**
     * Avvia sessione interattiva CLI del Flipper Zero.
     * 
     * <p>Implementa loop interattivo con:
     * <ul>
     *   <li>ASCII art colorata del dispositivo</li>
     *   <li>Prompt dedicato "Flipper> "</li>
     *   <li>Feedback colorato per tipo di risultato</li>
     *   <li>Gestione comandi di uscita (exit/quit/q)</li>
     *   <li>Logging automatico nel buffer temporaneo</li>
     * </ul>
     * 
     * <p>La sessione termina automaticamente per completamento sfida
     * o comando esplicito di uscita.
     */
    public void startInteractiveCLISession() {
        var output = gameContext.getOutputHandler();
        var input = gameContext.getInputHandler();
        
        // Visualizzazione ASCII art e intestazione
        output.writeFormatted(getFlipperAsciiArt(), ColorText.ORANGE);
        output.writeln("", ColorText.RESET);
        
        output.writeln("=".repeat(60), ColorText.ORANGE);
        output.writeln("        FLIPPER ZERO - MODALITÀ INTERATTIVA", ColorText.LIGHT_ORANGE);
        output.writeln("=".repeat(60), ColorText.ORANGE);
        output.writeln("");
        
        output.writeln("🔸 Dispositivo attivato. Inserisci comandi.", ColorText.WHITE);
        output.writeln("🔸 Digita 'exit' o 'quit' per uscire", ColorText.GRAY);
        output.writeln("🔸 Formato: [frequenza] [comando]", ColorText.GRAY);
        output.writeln("", ColorText.RESET);
        output.writeln("=".repeat(60), ColorText.DARK_ORANGE);
        
        boolean sessionActive = true;
        
        // Loop principale della sessione
        while (sessionActive) {
            try {
                output.write("\nFlipper> ", ColorText.ORANGE);
                String userInput = input.getInput();
                
                // Logging nel buffer temporaneo
                if (gameContext.getTemplog() != null) {
                    gameContext.getTemplog().add("[FLIPPER]: " + userInput);
                }

                // Gestione comandi di uscita
                if (userInput.equalsIgnoreCase("exit") || 
                    userInput.equalsIgnoreCase("quit") || 
                    userInput.equalsIgnoreCase("q")) {
                    sessionActive = false;
                    output.writeln("Chiusura sessione Flipper Zero...", ColorText.CYAN);
                    break;
                }
                
                if (userInput.isEmpty()) {
                    output.writeln("Inserisci un comando o 'exit' per uscire", ColorText.GRAY);
                    continue;
                }
                
                // Elaborazione comando
                FlipperResult result = commandProcessor.processCommand(userInput);
                
                ColorText color = getColorForResult(result.getType());
                output.writeln(result.getMessage(), color);
                
                // Uscita automatica per completamento sfida
                if (result.isGameCompleted()) {
                    output.writeln("\n🎉 Missione completata! Uscita dalla sessione Flipper...", ColorText.GREEN);
                    sessionActive = false;
                }
                
            } catch (Exception e) {
                output.writeln("\nErrore durante l'input: " + e.getMessage(), ColorText.RED);
                output.writeln("Uscita dalla sessione Flipper...", ColorText.RED);
                sessionActive = false;
            }
        }
        
        output.writeln("Ritorno al gioco principale...", ColorText.WHITE);
    }
    
    /**
     * Converte tipo di risultato Flipper in colore appropriato per output.
     * 
     * @param type Tipo di risultato da convertire
     * @return ColorText corrispondente per visualizzazione
     */
    private ColorText getColorForResult(FlipperResult.ResultType type) {
        switch (type) {
            case SUCCESS: return ColorText.GREEN;
            case ERROR: return ColorText.RED;
            case WARNING: return ColorText.YELLOW;
            case INFO: return ColorText.CYAN;
            default: return ColorText.WHITE;
        }
    }
    
    /**
     * Restituisce ASCII art colorata del Flipper Zero per visualizzazione CLI.
     * Include caratteri katakana per rappresentare "Flipper" in giapponese.
     * 
     * @return Stringa con ASCII art e codici colore embedded
     */
    public static String getFlipperAsciiArt(){
        String flipperAscii =
            "[ORANGE]        _____________________________________________________[/]\n" +
            "[LIGHT_ORANGE]       /                                                     \\[/]\n" +
            "[ORANGE]      /    _________________________________________           \\[/]\n" +
            "[LIGHT_ORANGE]     /    |                                         |  /\\         \\[/]\n" +
            "[ORANGE]    |     |      /\\_/\\   ((  (o)  ))  フリッパー         | |  |        |[/]\n" +
            "[LIGHT_ORANGE]    |     |     ( o.o )      (  )                   | |  |        |[/]\n" +
            "[ORANGE]    |     |      > ^ <       `--'                   |  \\/         |[/]\n" +
            "[LIGHT_ORANGE]    |     |_________________________________________|             |[/]\n" +
            "[ORANGE]    |                                                             |[/]\n" +
            "[LIGHT_ORANGE]     \\                      FLIPPER                     ,-----.   /[/]\n" +
            "[ORANGE]      \\________________________________________________(       )_/[/]\n" +
            "[LIGHT_ORANGE]                                                        `-----'[/]";
        return flipperAscii;
    }
}