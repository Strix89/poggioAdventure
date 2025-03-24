package di.uniba.map.b.adventure;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

public class SaveGame {
    private static final String SAVE_DIR = ResourceLoader.SAVES_DIRECTORY;

    // Metodo per salvare l'Engine
    public static void saveGame(Engine engine, FlowOutput output) {
        try {
            Files.createDirectories(Paths.get(SAVE_DIR));

            DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH-mm-ss");
            String fileName = engine.getPlayerName() + "_" + dt.format(LocalDateTime.now()) + ".dat";
            File saveFile = new File(SAVE_DIR + fileName);

            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(saveFile))) {
                out.writeObject(engine);
            }

            // Usa FlowOutput invece di System.out
            output.writeln("\nGioco salvato con successo: " + fileName + "\n", ColorText.GREEN);
            
        } catch (IOException e) {
            output.writeln("\nErrore durante il salvataggio: " + e.getMessage() + "\n", ColorText.ERROR);
        }
    }

    // Metodo per caricare l'Engine
    public static Engine loadGame(String playerName) {
        try {
            File folder = new File(SAVE_DIR);
            if (!folder.exists()) {
                System.out.println("Nessun salvataggio trovato per il giocatore: " + playerName);
                return null;
            }

            // Filtra i file con estensione .dat e che iniziano con il nome del giocatore
            File[] saveFiles = folder.listFiles((dir, name) -> name.startsWith(playerName) && name.endsWith(".dat"));
            if (saveFiles == null || saveFiles.length == 0) {
                System.out.println("Salvataggio non trovato.");
                return null;
            }

            // Carica l'ultimo salvataggio disponibile per quel giocatore
            File latestSaveFile = saveFiles[saveFiles.length - 1]; // Prendi l'ultimo salvataggio

            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(latestSaveFile))) {
                Engine engine = (Engine) in.readObject();  // Carica l'Engine dal file
                return engine;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Errore durante il caricamento del gioco: " + e.getMessage());
            return null;
        }
    }

    // Metodo per elencare i salvataggi disponibili
    public static void listSaves() {
        File folder = new File(SAVE_DIR);
        if (!folder.exists() || folder.listFiles() == null) {
            System.out.println("Nessun salvataggio trovato.");
            return;
        }

        // Filtra i file con estensione .dat
        File[] saveFiles = folder.listFiles((FilenameFilter) (dir, name) -> name.endsWith(".dat"));
        if (saveFiles == null || saveFiles.length == 0) {
            System.out.println("Nessun salvataggio trovato.");
            return;
        }

        // Mostra tutti i salvataggi disponibili
        System.out.println("\nSalvataggi disponibili:");
        for (File file : saveFiles) {
            // Estrai il nome del file (senza la parte di percorso) e visualizza la data di creazione
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String formattedDate = dateFormat.format(file.lastModified());
            System.out.println("- " + file.getName() + " (Salvato il: " + formattedDate + ")");
        }
    }
}

