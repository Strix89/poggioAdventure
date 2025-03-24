package di.uniba.map.b.adventure;

import static com.mycompany.poggioadventure.ui.UI_Config.getExitDefaultOp;
import di.uniba.map.b.adventure.impl.ConsoleOutput;
import di.uniba.map.b.adventure.impl.PoggioAdventure;
import di.uniba.map.b.adventure.parser.*;
import di.uniba.map.b.adventure.type.AdvObject;
import di.uniba.map.b.adventure.type.CommandType;
import di.uniba.map.b.adventure.type.Room;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * L'Engine gestisce l'interazione con l'utente e il flusso di gioco.
 * Supporta caricamento e salvataggio delle partite.
 */
public class Engine {

    private final GameDescription game;
    private Parser parser;
    private String playerName;
    private TimeManager timeManager;
    private final FlowOutput output;
    private boolean guiMode;

    /**
     * Costruttore dell'Engine.
     *
     * @param game Istanza della classe che implementa GameDescription.
     */
    public Engine(GameDescription game, String playerName, FlowOutput output, boolean guiMode) {
        this.guiMode = guiMode;
        this.game = game;
        this.playerName = playerName;
        this.output = output;
        try {
            this.game.init();
        } catch (Exception ex) {
            if (guiMode) {
                JOptionPane.showMessageDialog(null,
                    "Errore critico nel caricamento dell'Engine: " + ex.getMessage(),
                    "Errore", JOptionPane.ERROR_MESSAGE);  // Mostra un messaggio di errore
            } else {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, 
                    "Errore critico caricamento dell'Engine", ex);
            }
        }

        try {
            Set<String> stopWords = ResourceLoader.loadFileListInSet(new File(ResourceLoader.STOPWORDS_PATH));
            parser = new Parser(stopWords);
            
        } catch (IOException ex) {
            // Se si verifica un errore durante il caricamento delle risorse, registra l'errore e termina il programma
            if (guiMode) {
                JOptionPane.showMessageDialog(null,
                    "Errore critico nel caricamento delle STOPWORDS: " + ex.getMessage(),
                    "Errore", JOptionPane.ERROR_MESSAGE);  // Mostra un messaggio di errore
            } else {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, 
                    "Errore critico caricamento STOPWORDS", ex);
            }
            getExitDefaultOp(); // Termina il programma con codice di uscita 1
        }
        output.writeln(game.getGameVersion(), ColorText.RED);
        output.writeln(game.getWelcomeMsg(), ColorText.WHITE);
        output.write("\nTi trovi qui: ", ColorText.WHITE);
        output.writeln(game.getCurrentRoom().getName(), ColorText.BRIGHT_YELLOW);
        output.writeln(game.getCurrentRoom().getDescription(), ColorText.WHITE);
        printCursor();
        // Inizializza TimeManager utilizzando i valori di default
        timeManager = new TimeManager();
    }

    /**
     * Metodo che avvia l'esecuzione del gioco.
     * @param command
     */
    public void processCommand(String command) {
        Room previousRoom = game.getCurrentRoom();
        List<AdvObject> previousInventory = new ArrayList<>(game.getInventory());
        List<AdvObject> previousObjInRoom = new ArrayList<>(game.getCurrentRoom().getObjects());

        ParserOutput p = parser.parse(command, game.getCommands(), 
            game.getCurrentRoom().getObjects(), game.getInventory());

        if (p == null || p.getCommand() == null) {
            output.writeln("Non capisco quello che mi vuoi dire.", ColorText.BRIGHT_RED);
        } else {
            game.nextMove(p, output);

            // Gestione comandi speciali
            if (p.getCommand().getType() == CommandType.END) {
                output.writeln("Sei un fifone, addio!", ColorText.RED);
                System.exit(0);
            } else if (game.getCurrentRoom() == null) {
                output.writeln("La tua avventura termina qui! Complimenti!", ColorText.YELLOW);
                System.exit(0);
            }
        }
        printCursor();
    }

    // Modifica execute()
    public void execute() {
        if (parser == null) {
            System.err.println("Errore critico: parser non inizializzato");
            return;
        }

        if(!guiMode) {
            try (Scanner scanner = new Scanner(System.in)) {
                cliGameLoop(scanner);
            }
        }
    }
    
    private void cliGameLoop(Scanner scanner) {
        // Implementazione originale del loop per CLI
        while (true) {
            String command = scanner.nextLine().trim();
            processCommand(command);
            if (game.getCurrentRoom() == null) break;
        }
    }
    /**
     * Metodo per salvare il gioco.
     */
    private void saveGame() {
        String chapter = "Capitolo1"; // Quando metteremo più capitoli, questo valore dovrà essere calcolato
        SaveGame.saveGame(playerName, chapter, game.getCurrentRoom(), game.getInventory());
    }

    public FlowOutput getOutput() {
        return output;
    }

    /**
     * Metodo main per avviare il gioco.
     * @param args
     */
    public static void main(String[] args) {
        Engine consoleEngine = new Engine(new PoggioAdventure(), "NONE", new ConsoleOutput(), false);
        consoleEngine.execute();
    }
    
    public GameDescription getGame() {
        return game;
    }
    
    private void printCursor(){
        if (this.guiMode) output.write("", ColorText.WHITE);
        else output.write("\n?> ", ColorText.WHITE);
    }
    
}
