package di.uniba.map.b.adventure;

import di.uniba.map.b.adventure.impl.FireHouseGame;
import di.uniba.map.b.adventure.parser.*;
import di.uniba.map.b.adventure.type.AdvObject;
import di.uniba.map.b.adventure.type.CommandType;
import di.uniba.map.b.adventure.type.Room;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * L'Engine gestisce l'interazione con l'utente e il flusso di gioco.
 * Supporta caricamento e salvataggio delle partite e l'esecuzione di comandi multipli.
 */
public class Engine {

    private final GameDescription game;
    private Parser parser;
    private String playerName;
    private TimeManager timeManager;

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
        // Inizializza TimeManager utilizzando i valori di default
        //timeManager = new TimeManager();
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
        
        // Chiedere all'utente il nome giocatore
        System.out.print("Inserisci il tuo nome: ");
        playerName = scanner.nextLine().trim();

        LoggerInput logger = new LoggerInput(playerName); 

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
        //timeManager.start();

        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();
            
            //utilizzo del parser per ottenere una lista di comandi
            List<ParserOutput> outputs = parser.parseMultiple(command, game.getCommands(), 
                                                         game.getCurrentRoom().getObjects(), 
                                                         game.getInventory());
            
            // Salvataggio dello stato precedente del gioco
            Room previousRoom = game.getCurrentRoom();
            List<AdvObject> previousInventory = new ArrayList<>(game.getInventory());
            List<AdvObject> previousObjInRoom = new ArrayList<>(game.getCurrentRoom().getObjects());
            
            boolean commandExecuted = false;
            boolean shouldExit = false;
            
            if (outputs.isEmpty()) {
                System.out.println("Non capisco quello che mi vuoi dire.");
            } else {
                // Esegue tutti i comandi in sequenza
                for (ParserOutput p : outputs) {
                    if (p != null && p.getCommand() != null) {
                        commandExecuted = true;
                        
                        // Esegue il comando
                        game.nextMove(List.of(p), System.out);
                        
                        // Controlla se il gioco deve terminare
                        if (p.getCommand().getType() == CommandType.END) {
                            System.out.println("Sei un fifone, addio!");
                            shouldExit = true;
                            break;
                        } else if (p.getCommand().getType() == CommandType.SAVE) {
                            saveGame();
                            System.out.println("Ci rivediamo presto per continuare la tua avventura!");
                            scanner.close();
                            System.exit(0);
                        } else if (game.getCurrentRoom() == null) {
                            System.out.println("La tua avventura termina qui! Complimenti!");
                            System.exit(0);
                        }
                    }
                }
                
                if (!commandExecuted) {
                    System.out.println("Non capisco quello che mi vuoi dire.");
                }
                
                if (shouldExit) {
                    scanner.close();
                    break;
                }
                
                // Controlla se lo stato del gioco è cambiato
                boolean roomChanged = !previousRoom.equals(game.getCurrentRoom()); // Se la stanza è cambiata
                boolean inventoryChanged = !previousInventory.equals(game.getInventory()); // Se l'inventario è cambiato
                boolean objectsInRoomChanged = !previousObjInRoom.equals(game.getCurrentRoom().getObjects()); // Se gli oggetti nella stanza sono cambiati
                boolean isLookAtCommand = false;
                
                // Verifica se uno dei comandi è di tipo LOOK_AT
                for (ParserOutput p : outputs) {
                    if (p.getCommand() != null && p.getCommand().getType() == CommandType.LOOK_AT) {
                        isLookAtCommand = true;
                        break;
                    }
                }
                
                // Registra il comando solo se ha prodotto un cambiamento di stato
                if (roomChanged || inventoryChanged || objectsInRoomChanged || isLookAtCommand) {
                    logger.logInput(command);
                }
            }
            
            System.out.print("?> ");
        }
        //timeManager.stop();
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