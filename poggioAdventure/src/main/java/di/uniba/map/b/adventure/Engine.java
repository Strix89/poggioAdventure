package di.uniba.map.b.adventure;

import static com.mycompany.poggioadventure.ui.UI_Config.getExitDefaultOp;
import di.uniba.map.b.adventure.impl.ConsoleOutput;
import di.uniba.map.b.adventure.impl.PoggioAdventureGame;
import di.uniba.map.b.adventure.parser.*;
import di.uniba.map.b.adventure.type.AdvObject;
import di.uniba.map.b.adventure.type.CommandType;
import di.uniba.map.b.adventure.type.Room;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    /**
     * Costruttore dell'Engine.
     *
     * @param game Istanza della classe che implementa GameDescription.
     */
    public Engine(GameDescription game, String playerName, FlowOutput output) {
        
        this.game = game;
        this.playerName = playerName;
        this.output = output;
        try {
            this.game.init();
        } catch (Exception ex) {
            System.err.println(ex);
        }

        try {
            Set<String> stopWords = ResourceLoader.loadFileListInSet(new File(ResourceLoader.STOPWORDS_PATH));
            parser = new Parser(stopWords);
            
        } catch (Exception ex) {
            // Se si verifica un errore durante il caricamento delle risorse, registra l'errore e termina il programma
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, 
                    "Errore caricamento STOPWORDS", ex);
            getExitDefaultOp(); // Termina il programma con codice di uscita 1
        }
        // Inizializza TimeManager utilizzando i valori di default
        timeManager = new TimeManager();
    }

    /**
     * Metodo che avvia l'esecuzione del gioco.
     */
    public void execute() {
        if (parser == null) {
            System.err.println("Errore critico: il parser non è stato inizializzato correttamente. Impossibile eseguire il gioco.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        
        output.writeln();
        output.writeln("================================");
        output.writeln("* Adventure v. 0.4 - 2023-2024 *");
        output.writeln("*         developed by         *");
        output.writeln("*       Pierpaolo Basile       *");
        output.writeln("================================");
        output.writeln();

        LoggerInput logger = new LoggerInput(playerName); 

        // Mostra i salvataggi disponibili
        //SaveGame.listSaves();

        // Chiedere se si vuole caricare una partita salvata
        output.write("\nVuoi caricare una partita salvata? (si/no): ");
        String choice = scanner.nextLine().trim().toLowerCase();

        if (choice.equals("si")) {
            output.write("Inserisci il nome del file di salvataggio: ");
            String fileName = scanner.nextLine().trim();

            GameState loadedState = SaveGame.loadGame(fileName);
            if (loadedState != null) {
                game.setCurrentRoom(loadedState.getCurrentRoom());
                game.getInventory().clear();
                game.getInventory().addAll(loadedState.getInventory());
                output.write("\nPartita caricata con successo!");
            } else {
                output.write("\nCaricamento fallito. Inizio una nuova partita.");
            }
        }

        // Avvio del gioco
        output.writeln("\n" + game.getWelcomeMsg());
        output.writeln("Ti trovi qui: " + game.getCurrentRoom().getName());
        output.writeln();
        output.writeln(game.getCurrentRoom().getDescription());
        output.writeln();
        output.write("?> ");
        //timeManager.start();

        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();
            
            // Salvataggio dello stato precedente del gioco
            Room previousRoom = game.getCurrentRoom();
            List<AdvObject> previousInventory = new ArrayList<>(game.getInventory());
            List<AdvObject> previousObjInRoom = new ArrayList<>(game.getCurrentRoom().getObjects());
        
            ParserOutput p = parser.parse(command, game.getCommands(), game.getCurrentRoom().getObjects(), game.getInventory());
        
            if (p == null || p.getCommand() == null) {
                output.writeln("Non capisco quello che mi vuoi dire.");
            } else {
                // Esegue il comando
                game.nextMove(p, output);
        
                // Controlla se lo stato del gioco è cambiato
                boolean roomChanged = !previousRoom.equals(game.getCurrentRoom()); // Se la stanza è cambiata
                boolean inventoryChanged = !previousInventory.equals(game.getInventory()); // Se l'inventario è cambiato
                boolean objectsInRoomChanged = !previousObjInRoom.equals(game.getCurrentRoom().getObjects()); // Se gli oggetti nella stanza sono cambiati
                boolean isLookAtCommand = p.getCommand().getType() == CommandType.LOOK_AT; // Se il comando è di tipo "osserva"
        
                // Registra il comando solo se ha prodotto un cambiamento di stato
                if (roomChanged || inventoryChanged || objectsInRoomChanged || isLookAtCommand) {
                    logger.logInput(command);
                }
        
                if (p.getCommand().getType() == CommandType.END) {
                    output.writeln("Sei un fifone, addio!");
                    scanner.close();
                    break;
                } else if (p.getCommand().getType() == CommandType.SAVE) {
                    saveGame();
                    output.writeln("Ci rivediamo presto per continuare la tua avventura!");
                    scanner.close();
                    System.exit(0);
                } else if (game.getCurrentRoom() == null) {
                    output.writeln("La tua avventura termina qui! Complimenti!");
                    System.exit(0);
                }
            }
            output.write("?> ");
        }
        timeManager.stop();
    }

    /**
     * Metodo per salvare il gioco.
     */
    private void saveGame() {
        String chapter = "Capitolo1"; // Quando metteremo più capitoli, questo valore dovrà essere calcolato
        SaveGame.saveGame(playerName, chapter, game.getCurrentRoom(), game.getInventory());
    }

    /**
     * Metodo main per avviare il gioco.
     */
    public static void main(String[] args) {
        Engine engine = new Engine(new PoggioAdventureGame(), "NONE", new ConsoleOutput());
        engine.execute();
    }
}
