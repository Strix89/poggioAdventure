package di.uniba.map.b.adventure;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.time.*;
import java.time.format.DateTimeFormatter;

import di.uniba.map.b.adventure.type.AdvObject;
import di.uniba.map.b.adventure.type.Room;

public class SaveGame {

    private static final String SAVE_DIR = "./poggioAdventure/save/";

    public static void saveGame(String playerName, String chapter, Room currentRoom, List<AdvObject> inventory) {
        try {
            Files.createDirectories(Paths.get(SAVE_DIR));

            DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH-mm-ss");
            LocalDateTime timestamp = LocalDateTime.now();

            String fileName = playerName + "_" + chapter + "_" + dt.format(timestamp) + ".dat";
            File saveFile = new File(SAVE_DIR + fileName);

            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(saveFile))) {
                GameState gameState = new GameState(playerName, chapter, currentRoom, inventory);
                out.writeObject(gameState);
            }

            System.out.println("Il gioco è stato salvato con successo: " + fileName);
        } catch (IOException e) {
            System.err.println("Errore durante il salvataggio: " + e.getMessage());
        }
    }

    public static GameState loadGame(String fileName) {
        try {
            File file = new File(SAVE_DIR + fileName);
            if (!file.exists()) {
                System.out.println("File salvataggio non trovato: " + fileName);
                return null;
            }

            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                return (GameState) in.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Errore durante il salvataggio" + e.getMessage());
            return null;
        }
    }

    public static void listSaves() {
        File folder = new File(SAVE_DIR);
        if (!folder.exists() || folder.listFiles() == null) {
            System.out.println("Nessun salvataggio trovato.");
            return;
        }

        File[] saveFiles = folder.listFiles((dir, name) -> name.endsWith(".dat"));
        if (saveFiles == null || saveFiles.length == 0) {
            System.out.println("Nessun salvataggio trovato.");
            return;
        }

        System.out.println("\nSalvataggi disponibili:");
        for (File file : saveFiles) {
            System.out.println("- " + file.getName());
        }
    }
}
