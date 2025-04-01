package com.mycompany.poggioadventure.core;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.utils.TimeManager;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * L'Engine gestisce l'interazione con l'utente e il flusso di gioco.
 * Supporta caricamento e salvataggio delle partite e l'esecuzione di comandi multipli.
 */
public class Engine {

    private final GameDescription game;
    private Parser parser;
    private String playerName;
    private OutputHandler output;
    private InputHandler inputHandler;
    private ErrorHandler errorHandler;
    private TimeManager timeManager;
    private LoggerInput logger;
    private final StopWatch gameTime;
    private List<String> logTemp;
    
    /**
     * Costruttore dell'Engine.
     * @param game
     * @param playerName
     * @param output
     * @param input
     * @param errorHandler
     * @param logger
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
        output.writeln(game.getWelcomeMsg(), ColorText.WHITE);
        output.write("\nTi trovi qui: ", ColorText.WHITE);
        output.writeln(game.getCurrentRoom().getName(), ColorText.BRIGHT_YELLOW);
        output.writeln(game.getCurrentRoom().getDescription(), ColorText.WHITE);
        printCursor();  // Chiamato dopo il set up iniziale
        timeManager = new TimeManager();
        gameTime = StopWatch.getInstance();
        gameTime.start();
    }

    /**
     * Metodo per processare il comando ricevuto.
     * @param command
     */
public void processCommand(String command) {
        logTemp.add(command);
        Room previousRoom = game.getCurrentRoom();
        List<AdvObject> previousInventory = new ArrayList<>(game.getInventory());
        List<AdvObject> previousObjInRoom = new ArrayList<>(game.getCurrentRoom().getObjects());

        // Modifica: supporto per comandi multipli
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
                    game.nextMove(List.of(p), output);
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

            // Logging avanzato da origin/main
            boolean roomChanged = !previousRoom.equals(game.getCurrentRoom());
            boolean inventoryChanged = !previousInventory.equals(game.getInventory());
            boolean objectsChanged = !previousObjInRoom.equals(game.getCurrentRoom().getObjects());
            boolean isLookCommand = outputs.stream()
                .anyMatch(p -> p.getCommand() != null && p.getCommand().getType() == CommandType.LOOK_AT);
        }
        printCursor();
    }

    /**
     * Ciclo di gioco generico che si occupa di ricevere input
     * e passarlo al metodo processCommand per l'elaborazione.
     */
    public void startGameLoop() {
        while (game.getCurrentRoom() != null) {
            String command = inputHandler.getInput();  // Ottieni l'input tramite l'input handler
            processCommand(command);
        }
    }

    public OutputHandler getOutput() {
        return output;
    }

    /**
     * Metodo per stampare il cursore.
     * Stampiamo il cursore solo se siamo in modalità CLI (cioè se inputHandler è un CLIInputHandler).
     */
    private void printCursor(){
        // Verifica se l'inputHandler è un'istanza di CLIInputHandler
        if (inputHandler instanceof CLIInputHandler) {
            output.write("\n?> ", ColorText.WHITE);  // Mostra il cursore solo in modalità CLI
        }
    }

    private void getGameColoredVersion() {
        String version = game.getGameVersion();

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

     public void saveGame() {
        gameTime.stop();
        try {
            // 1. Scrittura atomica dei log
            logger.logInput(logTemp); 

            // 2. Salvataggio stato gioco
            SaveGame.saveGame(this, output);

            // 3. Pulizia buffer
            logTemp.clear();

        } catch (IOException ex) {
            errorHandler.handleRecoverableError("Salvataggio log fallito: " + ex);
        } finally {
            gameTime.start();
        }
    }

    public Parser getParser() {
        return parser;
    }

    public String getPlayerName() {
        return playerName;
    }
    
    public GameDescription getGame(){
        return game;
    }
    
    public void setTimeManager(TimeManager t){
        this.timeManager = t;
    }
    
    public TimeManager getTimeManager(){
        return timeManager;
    }
    
    public void setOutputHandler(OutputHandler output) {
        this.output = output;
    }
    
    public void setInputHandler(InputHandler input) {
        this.inputHandler = input;
    }
    
    public void setErrorHandler(ErrorHandler error) {
        this.errorHandler = error;
    }

    public LoggerInput getLogger() {
        return logger;
    }
    
    public void setGameTime(long t){
        this.gameTime.startFrom(t);
    }
    
    public String getFormattedGameTime(){
        return this.gameTime.getFormattedTime();
    }
    
    public long getLongGameTime(){
        return this.gameTime.getElapsedSeconds();
    }
}
