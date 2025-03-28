package di.uniba.map.b.adventure;

import di.uniba.map.b.adventure.impl.CLIInputHandler;
import di.uniba.map.b.adventure.parser.*;
import di.uniba.map.b.adventure.type.AdvObject;
import di.uniba.map.b.adventure.type.CommandType;
import di.uniba.map.b.adventure.type.Room;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * L'Engine gestisce l'interazione con l'utente e il flusso di gioco.
 * Supporta caricamento e salvataggio delle partite.
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
        logger.logInput(command);
        Room previousRoom = game.getCurrentRoom();
        List<AdvObject> previousInventory = new ArrayList<>(game.getInventory());
        List<AdvObject> previousObjInRoom = new ArrayList<>(game.getCurrentRoom().getObjects());

        ParserOutput p = parser.parse(command, game.getCommands(), 
            game.getCurrentRoom().getObjects(), game.getInventory());

        if (p == null || p.getCommand() == null) {
            output.writeln("Non capisco quello che mi vuoi dire.", ColorText.ERROR);
        } else {
            if (p.getCommand().getType() == CommandType.SAVE) {
                // Salva il gioco
                saveGame();
            } else {
                // Gestione del movimento o altre azioni
                game.nextMove(p, output);

                // Gestione dei comandi speciali (esempio, fine gioco)
                if (p.getCommand().getType() == CommandType.END) {
                    output.writeln("Sei un fifone, addio!", ColorText.ERROR);
                    Utils.exitApplication();
                } else if (game.getCurrentRoom() == null) {
                    output.writeln("La tua avventura termina qui! Complimenti!", ColorText.NEON_ORANGE);
                    Utils.exitApplication();
                }
            }
        }
        printCursor();  // Stampiamo il cursore se necessario
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
        this.gameTime.stop();
        SaveGame.saveGame(this, output);
        this.gameTime.start();
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

