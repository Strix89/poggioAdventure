package com.mycompany.poggioadventure.model;

import com.mycompany.poggioadventure.core.GameContext;
import com.mycompany.poggioadventure.core.abstracts.IFlipperCommandProcessor;
import com.mycompany.poggioadventure.core.FlipperCommandProcessor;
import com.mycompany.poggioadventure.core.utils.FlipperResult;
import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.ui.gui.views.UI_Flipper;

/**
 * Adapter/Facade per l'interfaccia del Flipper Zero che gestisce sia 
 * la modalità CLI interattiva che l'interfaccia GUI.
 * 
 * <p>Questa classe funge da ponte tra il sistema di gioco e le diverse
 * modalità di interazione con il Flipper Zero virtuale. Coordina l'utilizzo
 * del command processor e gestisce la presentazione dei risultati.
 * 
 * <p><b>Responsabilità principali:</b>
 * <ul>
 *   <li>Creazione e gestione dell'interfaccia GUI del Flipper</li>
 *   <li>Implementazione della sessione CLI interattiva</li>
 *   <li>Coordinamento tra UI e business logic</li>
 *   <li>Gestione degli effetti visivi e del feedback utente</li>
 *   <li>Applicazione degli effetti del Flipper al gioco</li>
 * </ul>
 * 
 * <p><b>Modalità operative:</b>
 * <ul>
 *   <li><b>GUI Mode</b>: Interfaccia grafica con finestra dedicata</li>
 *   <li><b>CLI Mode</b>: Sessione interattiva a linea di comando</li>
 * </ul>
 * 
 * <p><b>Pattern utilizzati:</b>
 * <ul>
 *   <li>Adapter: adatta il command processor alle diverse UI</li>
 *   <li>Facade: fornisce interfaccia semplificata per il Flipper</li>
 *   <li>Strategy: usa strategie diverse per CLI vs GUI</li>
 * </ul>
 * 
 * @author Strix89
 * @version 1.1
 * @since 1.0
 */
public class FlipperLogic {
    
    /** Contesto di gioco per accedere ai componenti del sistema */
    private final GameContext gameContext;
    
    /** Processore dei comandi Flipper per la business logic */
    private final IFlipperCommandProcessor commandProcessor;
    
    /**
     * Costruttore che inizializza la logica del Flipper con il contesto di gioco.
     * 
     * <p>Crea automaticamente un'istanza del command processor standard
     * per gestire l'elaborazione dei comandi.
     * 
     * @param gameContext Contesto del gioco contenente tutti i riferimenti necessari
     * @throws IllegalArgumentException se gameContext è null
     */
    public FlipperLogic(GameContext gameContext) {
        this.gameContext = gameContext;
        this.commandProcessor = new FlipperCommandProcessor(gameContext);
    }
    
    /**
     * Apre l'interfaccia grafica del Flipper Zero in una finestra dedicata.
     * 
     * <p>Crea una nuova istanza di UI_Flipper configurata con il command processor
     * e la rende immediatamente visibile all'utente. La finestra è indipendente
     * dalla finestra principale del gioco.
     * 
     * <p><b>Caratteristiche dell'interfaccia GUI:</b>
     * <ul>
     *   <li>Visualizzazione ASCII art del Flipper Zero</li>
     *   <li>Campo di input per comandi con validazione</li>
     *   <li>Feedback visivo con dialoghi per i risultati</li>
     *   <li>Gestione automatica degli effetti sul timer</li>
     * </ul>
     * 
     * @return L'istanza di UI_Flipper creata e resa visibile
     * 
     * @example
     * <pre>{@code
     * FlipperLogic flipper = new FlipperLogic(gameContext);
     * UI_Flipper window = flipper.openGUIInterface();
     * // La finestra è ora visibile e pronta per l'input
     * }</pre>
     */
    public UI_Flipper openGUIInterface() {
        UI_Flipper guiInterface = new UI_Flipper(gameContext, commandProcessor);
        guiInterface.setVisible(true);
        return guiInterface;
    }
    
    /**
     * Avvia una sessione interattiva CLI del Flipper Zero.
     * 
     * <p>Implementa un loop interattivo che permette all'utente di inserire
     * comandi direttamente nella console di gioco. La sessione continua fino
     * a quando l'utente non esce esplicitamente o completa la sfida.
     * 
     * <p><b>Caratteristiche della sessione CLI:</b>
     * <ul>
     *   <li>ASCII art colorata del Flipper Zero</li>
     *   <li>Prompt dedicato "Flipper> "</li>
     *   <li>Comandi di uscita: exit, quit, q</li>
     *   <li>Feedback colorato basato sul tipo di risultato</li>
     *   <li>Gestione automatica degli effetti sul timer</li>
     *   <li>Logging di tutti i comandi inseriti</li>
     * </ul>
     * 
     * <p><b>Flusso della sessione:</b>
     * <ol>
     *   <li>Visualizzazione ASCII art e intestazione</li>
     *   <li>Loop di input-elaborazione-output</li>
     *   <li>Gestione comandi di uscita</li>
     *   <li>Applicazione effetti al gioco</li>
     *   <li>Uscita automatica per completamento sfida</li>
     * </ol>
     * 
     * <p><b>Gestione errori:</b>
     * La sessione gestisce gracefully gli errori di input e continua
     * il loop, permettendo all'utente di riprovare. In caso di errori
     * gravi, termina la sessione e torna al gioco principale.
     * 
     * @throws RuntimeException se si verificano errori critici nell'input handler
     * 
     * @example
     * <pre>{@code
     * FlipperLogic flipper = new FlipperLogic(gameContext);
     * flipper.startInteractiveCLISession();
     * // Blocca fino alla fine della sessione
     * }</pre>
     */
    public void startInteractiveCLISession() {
        var output = gameContext.getOutputHandler();
        var input = gameContext.getInputHandler();
        
        // Mostra ASCII art del Flipper con colori
        output.writeFormatted(getFlipperAsciiArt(), ColorText.ORANGE);
        output.writeln("", ColorText.RESET);
        
        // Intestazione colorata
        output.writeln("=".repeat(60), ColorText.ORANGE);
        output.writeln("        FLIPPER ZERO - MODALITÀ INTERATTIVA", ColorText.LIGHT_ORANGE);
        output.writeln("=".repeat(60), ColorText.ORANGE);
        output.writeln("");
        
        // Informazioni operative
        output.writeln("🔸 Dispositivo attivato. Inserisci comandi.", ColorText.WHITE);
        output.writeln("🔸 Digita 'exit' o 'quit' per uscire", ColorText.GRAY);
        output.writeln("🔸 Formato: [frequenza] [comando]", ColorText.GRAY);
        output.writeln("", ColorText.RESET);
        output.writeln("=".repeat(60), ColorText.DARK_ORANGE);
        
        boolean sessionActive = true;
        
        // Loop principale della sessione interattiva
        while (sessionActive) {
            try {
                // Prompt del Flipper
                output.write("\nFlipper> ", ColorText.ORANGE);
                String userInput = input.getInput(); // USA INPUT HANDLER INVECE DI SCANNER
                
                // Logging del comando nel buffer temporaneo
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
                
                // Gestione comando vuoto
                if (userInput.isEmpty()) {
                    output.writeln("Inserisci un comando o 'exit' per uscire", ColorText.GRAY);
                    continue;
                }
                
                // Processa il comando del Flipper
                FlipperResult result = commandProcessor.processCommand(userInput);
                
                // Mostra risultato con colori appropriati
                ColorText color = getColorForResult(result.getType());
                output.writeln(result.getMessage(), color);
                
                // Se è un comando di successo che completa il gioco, esci
                if (result.isGameCompleted()) {
                    output.writeln("\n🎉 Missione completata! Uscita dalla sessione Flipper...", ColorText.GREEN);
                    sessionActive = false;
                }
                
            } catch (Exception e) {
                // Gestione errori con più dettagli
                output.writeln("\nErrore durante l'input: " + e.getMessage(), ColorText.RED);
                output.writeln("Uscita dalla sessione Flipper...", ColorText.RED);
                sessionActive = false;
            }
        }
        
        // Messaggio di chiusura
        output.writeln("Ritorno al gioco principale...", ColorText.WHITE);
    }
    
    /**
     * Converte il tipo di risultato Flipper in un colore appropriato per l'output.
     * 
     * <p>Mapping dei colori:
     * <ul>
     *   <li>SUCCESS → Verde (operazione riuscita)</li>
     *   <li>ERROR → Rosso (errore)</li>
     *   <li>WARNING → Giallo (avviso/penalità)</li>
     *   <li>INFO → Ciano (informazione/bonus)</li>
     *   <li>Default → Bianco (fallback)</li>
     * </ul>
     * 
     * @param type Tipo di risultato da convertire
     * @return ColorText corrispondente per la visualizzazione
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
     * Restituisce l'ASCII art colorata del Flipper Zero per la visualizzazione CLI.
     * 
     * <p>Genera una rappresentazione artistica del dispositivo Flipper Zero
     * con codici colore integrati per la visualizzazione nella console.
     * Utilizza diverse tonalità di arancione per creare un effetto visivo
     * accattivante.
     * 
     * @return Stringa contenente ASCII art con codici colore embedded
     * 
     * @apiNote L'ASCII art include caratteri giapponesi (フリッパー) per 
     *          rappresentare "Flipper" in katakana
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