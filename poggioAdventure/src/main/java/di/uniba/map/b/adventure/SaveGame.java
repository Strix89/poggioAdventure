package di.uniba.map.b.adventure;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SaveGame {
    private static final Path SAVE_DIR = ResourceLoader.SAVES_DIRECTORY;
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("dd_MM_yyyy_HH-mm-ss");
    
    // Restituisce una lista di nomi file (senza estensione)
    public static List<String> getSaveList() {
        try {
            Files.createDirectories(SAVE_DIR);
            return Files.list(SAVE_DIR) // Lista tutti i file nella directory
                .filter(p -> p.toString().endsWith(".dat")) // Filtra solo .dat
                .map(p -> p.getFileName().toString().replace(".dat", "")) // Rimuovi estensione
                .sorted(Comparator.reverseOrder()) // Ordina dal più recente
                .collect(Collectors.toList());
        } catch (IOException ex) {
            return Collections.emptyList();
        }
    }

    // Metodo per salvare l'Engine
    public static void saveGame(Engine engine, OutputHandler output) {
        try {
            // Crea il nome del file con timestamp
            String fileName = String.format("%s_%s.dat", 
                engine.getPlayerName(), 
                DATE_FORMATTER.format(LocalDateTime.now())
            );
            
            // Combina directory e nome file
            Path savePath = SAVE_DIR.resolve(fileName);
            
            try (ObjectOutputStream out = new ObjectOutputStream(
                Files.newOutputStream(savePath, StandardOpenOption.CREATE)
            )) {
                out.writeObject(engine.getPlayerName());
                out.writeObject(engine.getGame());
                out.writeObject(engine.getTimeManager());
            }
            
            output.writeln("\nGioco salvato con successo!", ColorText.GREEN);
        } catch (IOException e) {
            output.writeln("\nErrore salvataggio: " + e.getMessage(), ColorText.ERROR);
        }
    }

    public static void loadSave(
        String saveName, 
        Consumer<Engine> onSuccess, 
        Consumer<String> onError, 
        ErrorHandler errorHandler,
        InputHandler input,
        OutputHandler output
    ) {
        // Costruisci il percorso completo
        Path savePath = SAVE_DIR.resolve(saveName + ".dat");
        
        if (!Files.exists(savePath)) {
            onError.accept("Salvataggio non trovato: " + saveName);
            return;
        }
        
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(savePath))) {
            String playerName = (String) in.readObject();
            GameDescription game = (GameDescription) in.readObject();
            TimeManager timeManager = (TimeManager) in.readObject();
            
            Engine engine = EngineFactory.createFromSave(game, playerName, output, input, errorHandler);
            engine.setTimeManager(timeManager);
            onSuccess.accept(engine);
        } catch (IOException | ClassNotFoundException ex) {
            onError.accept("Errore caricamento: " + ex.getMessage());
        }
    }
    
    public static boolean deleteSave(String saveName) {
        Path savePath = SAVE_DIR.resolve(saveName + ".dat");
        try {
            return Files.deleteIfExists(savePath);
        } catch (IOException ex) {
            return false;
        }
    }
}

