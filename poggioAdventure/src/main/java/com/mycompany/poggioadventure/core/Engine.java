package com.mycompany.poggioadventure.core;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.utils.TimeManager;
import com.mycompany.poggioadventure.core.utils.GameContext;
import com.mycompany.poggioadventure.core.utils.StopWatch;
import com.mycompany.poggioadventure.persistence.LoggerInput;
import com.mycompany.poggioadventure.parser.Parser;
import com.mycompany.poggioadventure.parser.ParserOutput;
import com.mycompany.poggioadventure.ui.InputHandler;
import com.mycompany.poggioadventure.ui.OutputHandler;
import com.mycompany.poggioadventure.ui.cli.CLIInputHandler;
import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.ui.ErrorHandler;
import com.mycompany.poggioadventure.persistence.ResourceLoader;
import com.mycompany.poggioadventure.persistence.SaveGame;
import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.parser.CommandType;
import com.mycompany.poggioadventure.model.Room;
import com.mycompany.poggioadventure.ui.gui.GUIOutputHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Classe core dell'applicazione che gestisce il ciclo di vita del gioco.
 *
 * <p><b>Responsabilità principali:</b>
 * <ul>
 *   <li>Gestione del ciclo principale di gioco</li>
 *   <li>Elaborazione dei comandi giocatore</li>
 *   <li>Coordinamento tra modello, view e controller</li>
 *   <li>Gestione del tempo di gioco</li>
 *   <li>Salvataggio e caricamento stato gioco</li>
 * </ul>
 *
 * <p><b>Design Pattern utilizzati:</b>
 * <ul>
 *   <li>Facade Pattern: fornisce un'interfaccia semplificata ai sottosistemi</li>
 *   <li>Observer Pattern: notifica cambiamenti di stato alla view</li>
 *   <li>Singleton Pattern: gestione del cronometro di gioco</li>
 * </ul>
 *
 * @version 1.3
 * @author [Autore originale non specificato]
 */
public class Engine {
    /**
     * Istanza del modello di gioco contenente lo stato attuale
     */
    private final GameDescription game;
    
    /**
     * Parser per interpretare i comandi testuali
     */
    private Parser parser;
    
    /**
     * Nome del giocatore corrente
     */
    private String playerName;
    
    /**
     * Gestore dell'output grafico/testuale
     */
    private OutputHandler output;
    
    /**
     * Gestore dell'input da utente
     */
    private InputHandler inputHandler;
    
    /**
     * Gestore centralizzato degli errori
     */
    private ErrorHandler errorHandler;
    
    /**
     * Gestore del tempo di gioco (ore/giorni/stagioni)
     */
    private TimeManager timeManager;
    
    /**
     * Logger per registrare l'attività di gioco
     */
    private LoggerInput logger;
    
    /**
     * Cronometro per il tempo di sessione
     */
    private final StopWatch gameTime;
    
    /**
     * Buffer temporaneo per i comandi da loggare
     */
    private List<String> logTemp;

    private GameContext gameContext;
    
    /**
     * Costruttore principale dell'Engine.
     *
     * <p><b>Operazioni eseguite:</b>
     * <ol>
     *   <li>Inizializza i componenti fondamentali</li>
     *   <li>Carica le stopwords per il parser</li>
     *   <li>Visualizza il messaggio di benvenuto</li>
     *   <li>Avvia il cronometro di gioco</li>
     * </ol>
     *
     * @param game Il modello di gioco da gestire
     * @param playerName Nome del giocatore
     * @param output Gestore dell'output
     * @param input Gestore dell'input
     * @param errorHandler Gestore degli errori
     * @param logger Logger per l'attività di gioco
     */
    public Engine(GameDescription game, String playerName, OutputHandler output, InputHandler input, ErrorHandler errorHandler, LoggerInput logger) {
        this.game = game;
        this.playerName = playerName;
        this.output = output;
        this.errorHandler = errorHandler;
        this.inputHandler = input;
        this.logger = logger; 
        this.logTemp = Collections.synchronizedList(new ArrayList<>());
        try {
            Set<String> stopWords = ResourceLoader.loadFileListInSet(new File(ResourceLoader.STOPWORDS_PATH.toString()));
            parser = new Parser(stopWords);
        } catch (IOException ex) {
            this.errorHandler.handleFatalError("Caricamento risorse fallito", ex);
            Utils.exitApplication(Utils.EXIT_CODE_RESOURCE_ERROR);
        }
        getGameColoredVersion();
        output.writeln(this.output instanceof GUIOutputHandler ? game.getGUIWelcomeMsg() : game.getCLIWelcomeMsg(), ColorText.WHITE);
        output.write(" \nTi trovi qui: ", ColorText.WHITE);
        output.writeln(game.getCurrentRoom().getName(), ColorText.BRIGHT_YELLOW);
        output.writeln(game.getCurrentRoom().getDescription(), ColorText.WHITE);
        printCursor();
        timeManager = new TimeManager();
        gameTime = StopWatch.getInstance();
        gameTime.start();
        gameContext = new GameContext(game, input, output, errorHandler, logTemp, gameTime);
    }

    /**
     * Elabora un comando ricevuto dal giocatore.
     *
     * <p><b>Flusso operativo:</b>
     * <ol>
     *   <li>Registra il comando nel buffer dei log</li>
     *   <li>Analizza il comando con il parser</li>
     *   <li>Esegue l'azione corrispondente</li>
     *   <li>Gestisce condizioni di vittoria/fine gioco</li>
     *   <li>Aggiorna l'interfaccia utente</li>
     * </ol>
     *
     * @param command Stringa contenente il comando da elaborare
     */
    public void processCommand(String command) {
        logTemp.add(command);
        Room previousRoom = game.getCurrentRoom();
        List<AdvObject> previousInventory = new ArrayList<>(game.getInventory());
        List<AdvObject> previousObjInRoom = new ArrayList<>(game.getCurrentRoom().getObjects());

        // Supporto per comandi multipli separati da punti e virgola
        List<ParserOutput> outputs = parser.parseMultiple(command, game.getCommands(), 
            game.getCurrentRoom().getObjects(), game.getInventory());

        if (outputs.isEmpty()) {
            output.writeln("Non capisco quello che mi vuoi dire.", ColorText.ERROR);
        } else {
            boolean commandExecuted = false;
            for (ParserOutput p : outputs) {
                if (p == null || p.getCommand() == null) continue;

                commandExecuted = true;

                if (p.getCommand().getType() == CommandType.SAVE) {
                    saveGame();
                } else {
                    game.nextMove(List.of(p), gameContext);
                }

                // Gestione uscita dal gioco
                if (p.getCommand().getType() == CommandType.END) {
                    output.writeln("Sei un fifone, addio!", ColorText.ERROR);
                    Utils.exitApplication(Utils.EXIT_CODE_SUCCESS);
                } else if (game.getCurrentRoom() == null) {
                    output.writeln("La tua avventura termina qui! Complimenti!", ColorText.NEON_ORANGE);
                    Utils.exitApplication(Utils.EXIT_CODE_SUCCESS);
                }
            }

            if (!commandExecuted) {
                output.writeln("Non capisco quello che mi vuoi dire.", ColorText.ERROR);
            }

            // Logging avanzato delle modifiche di stato
            boolean roomChanged = !previousRoom.equals(game.getCurrentRoom());
            boolean inventoryChanged = !previousInventory.equals(game.getInventory());
            boolean objectsChanged = !previousObjInRoom.equals(game.getCurrentRoom().getObjects());
            boolean isLookCommand = outputs.stream()
                .anyMatch(p -> p.getCommand() != null && p.getCommand().getType() == CommandType.LOOK_AT);
        }
        printCursor();
    }

    /**
     * Avvia il ciclo principale di gioco.
     *
     * <p>Continua a elaborare comandi finché:
     * <ul>
     *   <li>Il giocatore non esce volontariamente</li>
     *   <li>Non viene raggiunta una condizione di vittoria</li>
     *   <li>Non si verifica un errore irreversibile</li>
     * </ul>
     */
    public void startGameLoop() {
        while (game.getCurrentRoom() != null) {
            String command = inputHandler.getInput();
            processCommand(command);
        }
    }

    /**
     * Restituisce il gestore dell'output corrente.
     * @return Istanza di OutputHandler
     */
    public OutputHandler getOutput() {
        return output;
    }

    /**
     * Stampa il prompt dei comandi nell'interfaccia CLI.
     *
     * <p>Viene visualizzato solo quando l'input handler è di tipo CLI.
     */
    private void printCursor(){
        if (inputHandler instanceof CLIInputHandler) {
            output.write("\n?> ", ColorText.WHITE);
        }
    }

    /**
     * Visualizza la versione colorata del titolo del gioco.
     *
     * <p>Applica uno schema di colori predefinito:
     * <ul>
     *   <li>Prima e ultima riga: blu navy</li>
     *   <li>Seconda riga: rosso</li>
     *   <li>Altre righe: giallo</li>
     * </ul>
     */
    private void getGameColoredVersion() {
        String version = this.output instanceof GUIOutputHandler ? game.getGUIGameVersion() : game.getCLIGameVersion();

        String[] lines = Arrays.stream(version.split("\n"))
                               .filter(line -> !line.trim().isEmpty())
                               .toArray(String[]::new);

        IntStream.range(0, lines.length).forEach(idx -> {
            String line = lines[idx];
            ColorText color;

            if (idx == 0 || idx == lines.length - 1) {
                color = ColorText.NAVY;
            } else if (idx == 1) {
                color = ColorText.RED;
            } else {
                color = ColorText.YELLOW;
            }

            output.writeln(line, color);
        });
        output.writeln();
    }

    /**
     * Salva lo stato corrente del gioco.
     *
     * <p><b>Operazioni eseguite:</b>
     * <ol>
     *   <li>Ferma temporaneamente il cronometro</li>
     *   <li>Scrive i log pendenti</li>
     *   <li>Serializza lo stato di gioco</li>
     *   <li>Pulisce il buffer dei log</li>
     *   <li>Riavvia il cronometro</li>
     * </ol>
     */
    public void saveGame() {
        gameTime.stop();
        try {
            if (logger != null) {
                logger.logInput(logTemp);
            } 

            SaveGame.saveGame(this, output);

        } catch (IOException ex) {
            errorHandler.handleRecoverableError("Salvataggio log fallito: " + ex);
        } finally {
            logTemp.clear();
            gameTime.start();
        }
    }

    // Metodi di accesso e modifica (getters/setters) seguono...
    // [Documentazione simile per i restanti metodi...]
    
    /**
     * Restituisce il parser dei comandi.
     * @return Istanza del parser
     */
    public Parser getParser() {
        return parser;
    }

    /**
     * Restituisce il nome del giocatore corrente.
     * @return Nome del giocatore
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * Restituisce il modello di gioco corrente.
     * @return Istanza di GameDescription
     */
    public GameDescription getGame(){
        return game;
    }
    
    /**
     * Imposta il gestore del tempo di gioco.
     * @param t Nuova istanza di TimeManager
     */
    public void setTimeManager(TimeManager t){
        this.timeManager = t;
    }
    
    /**
     * Restituisce il gestore del tempo di gioco.
     * @return Istanza di TimeManager
     */
    public TimeManager getTimeManager(){
        return timeManager;
    }
    
    /**
     * Imposta il gestore dell'output.
     * @param output Nuova istanza di OutputHandler
     */
    public void setOutputHandler(OutputHandler output) {
        this.output = output;
    }
    
    /**
     * Imposta il gestore dell'input.
     * @param input Nuova istanza di InputHandler
     */
    public void setInputHandler(InputHandler input) {
        this.inputHandler = input;
    }
    
    /**
     * Imposta il gestore degli errori.
     * @param error Nuova istanza di ErrorHandler
     */
    public void setErrorHandler(ErrorHandler error) {
        this.errorHandler = error;
    }

    /**
     * Restituisce il logger di gioco.
     * @return Istanza di LoggerInput
     */
    public LoggerInput getLogger() {
        return logger;
    }
    
    /**
     * Imposta il tempo di gioco iniziale.
     * @param t Tempo iniziale in millisecondi
     */
    public void setGameTime(long t){
        this.gameTime.startFrom(t);
    }
    
    /**
     * Restituisce il tempo di gioco formattato.
     * @return Stringa formattata (hh:mm:ss)
     */
    public String getFormattedGameTime(){
        return this.gameTime.getFormattedTime();
    }
    
    /**
     * Restituisce il tempo di gioco in secondi.
     * @return Tempo trascorso in secondi
     */
    public long getLongGameTime(){
        return this.gameTime.getElapsedSeconds();
    }
}
