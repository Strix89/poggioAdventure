package di.uniba.map.b.adventure;

import di.uniba.map.b.adventure.impl.FireHouseGame;
import di.uniba.map.b.adventure.parser.Parser;
import di.uniba.map.b.adventure.parser.ParserOutput;
import di.uniba.map.b.adventure.type.AdvObject;
import di.uniba.map.b.adventure.type.CommandType;
import di.uniba.map.b.adventure.type.Room;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * L'Engine gestisce l'interazione con l'utente e il flusso di gioco.
 * Supporta caricamento e salvataggio delle partite.
 */
public class Engine {

    private final GameDescription game;
    private Parser parser;
    private String playerName;

    /**
     * Costruttore dell'Engine.
     *
     * @param game Istanza della classe che implementa GameDescription.
     */
    public Engine(GameDescription game) {
        
        this.game = game;
        
        try {
            this.game.init();
        } catch (Exception ex) {
            System.err.println(ex);
        }

        try {
            Set<String> stopWords = Utils.loadFileListInSet(new File("./poggioAdventure/resources/stopwords"));
            parser = new Parser(stopWords);
            
        } catch (Exception ex) {
            System.err.println(ex);
        }

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
        
        System.out.println();
        System.out.println("================================");
        System.out.println("* Adventure v. 0.4 - 2023-2024 *");
        System.out.println("*         developed by         *");
        System.out.println("*       Pierpaolo Basile       *");
        System.out.println("================================");
        System.out.println();
        
        // Chiedere all'utente il nome giocatore
        System.out.print("Inserisci il tuo nome: ");
        playerName = scanner.nextLine().trim();

        // Mostra i salvataggi disponibili
        SaveGame.listSaves();

        // Chiedere se si vuole caricare una partita salvata
        System.out.print("\nVuoi caricare una partita salvata? (si/no): ");
        String choice = scanner.nextLine().trim().toLowerCase();

        if (choice.equals("si")) {
            System.out.print("Inserisci il nome del file di salvataggio: ");
            String fileName = scanner.nextLine().trim();

            GameState loadedState = SaveGame.loadGame(fileName);
            if (loadedState != null) {
                game.setCurrentRoom(loadedState.getCurrentRoom());
                game.getInventory().clear();
                game.getInventory().addAll(loadedState.getInventory());
                System.out.println("\nPartita caricata con successo!");
            } else {
                System.out.println("\nCaricamento fallito. Inizio una nuova partita.");
            }
        }

        // Avvio del gioco
        System.out.println("\n" + game.getWelcomeMsg());
        System.out.println("Ti trovi qui: " + game.getCurrentRoom().getName());
        System.out.println();
        System.out.println(game.getCurrentRoom().getDescription());
        System.out.println();
        System.out.print("?> ");

        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            ParserOutput p = parser.parse(command, game.getCommands(), game.getCurrentRoom().getObjects(), game.getInventory());

            if (p == null || p.getCommand() == null) {
                System.out.println("Non capisco quello che mi vuoi dire.");
            } else if (p.getCommand().getType() == CommandType.END) {
                System.out.println("Sei un fifone, addio!");
                break;
            } else if (p.getCommand().getType() == CommandType.SAVE) {
                saveGame();
                System.out.println("Ci rivediamo presto per continuare la tua avventura!");
                System.exit(0);
            } else {
                game.nextMove(p, System.out);
                if (game.getCurrentRoom() == null) {
                    System.out.println("La tua avventura termina qui! Complimenti!");
                    System.exit(0);
                }
            }
            System.out.print("?> ");
        }
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
        Engine engine = new Engine(new FireHouseGame());
        engine.execute();
    }
}
