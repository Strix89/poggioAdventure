package com.mycompany.poggioadventure.persistence;

import com.mycompany.poggioadventure.core.utils.TimeManager;
import com.mycompany.poggioadventure.core.Engine;
import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.utils.EngineFactory;
import com.mycompany.poggioadventure.ui.InputHandler;
import com.mycompany.poggioadventure.ui.OutputHandler;
import com.mycompany.poggioadventure.ui.ColorText;
import di.uniba.map.b.adventure.ErrorHandler;
import com.mycompany.poggioadventure.persistence.LoggerInput;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Classe per la gestione dei salvataggi del gioco.
 * Offre funzionalità per salvare, caricare, elencare ed eliminare salvataggi.
 * I salvataggi vengono gestiti con un sistema di timestamp e pulizia automatica
 * dei salvataggi vecchi nella stessa finestra temporale.
 * 
 * @author Strix89 | Elia-Valenza26
 */
public class SaveGame {
    // Directory dove vengono salvati i file di salvataggio
    private static final Path SAVE_DIR = ResourceLoader.SAVES_DIRECTORY;
    
    // Formattatore per la data/ora nei nomi dei file di salvataggio
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("dd_MM_yyyy_HH-mm-ss");
    
    // Finestra temporale in ore entro cui eliminare i salvataggi precedenti
    protected static final int SAVE_WINDOW_HOURS = 2;

    /**
     * Restituisce la lista dei salvataggi disponibili.
     * I file sono ordinati dal più recente al più vecchio.
     * 
     * @return Lista di nomi di file senza estensione
     */
    public static List<String> getSaveList() {
        try {
            // Crea la directory se non esiste
            Files.createDirectories(SAVE_DIR);
            
            return Files.list(SAVE_DIR) // Lista tutti i file nella directory
                .filter(p -> p.toString().endsWith(".dat")) // Filtra solo file .dat
                .map(p -> p.getFileName().toString().replace(".dat", "")) // Rimuovi estensione
                .sorted(Comparator.reverseOrder()) // Ordina dal più recente
                .collect(Collectors.toList());
        } catch (IOException ex) {
            // In caso di errore, restituisce una lista vuota
            return Collections.emptyList();
        }
    }

    /**
     * Salva lo stato corrente del gioco.
     * Elimina eventuali salvataggi precedenti nella stessa finestra temporale.
     * 
     * @param engine Istanza del motore di gioco da salvare
     * @param output Handler per l'output dei messaggi
     */
    public static void saveGame(Engine engine, OutputHandler output) {
        try {
            String playerName = engine.getPlayerName();
            LocalDateTime now = LocalDateTime.now();
            
            // Elimina salvataggi precedenti entro la finestra temporale
            try {
                List<Path> existingSaves = Files.list(SAVE_DIR)
                        .filter(p -> {
                            String fileName = p.getFileName().toString();
                            return fileName.startsWith(playerName + "_") && fileName.endsWith(".dat");
                        })
                        .collect(Collectors.toList());
                
                for (Path saveFile : existingSaves) {
                    String fileName = saveFile.getFileName().toString();
                    int startIndex = playerName.length() + 1;
                    int endIndex = fileName.length() - 4; // Rimuovi .dat
                    
                    if (startIndex >= endIndex) continue;
                    
                    String datePart = fileName.substring(startIndex, endIndex);
                    try {
                        LocalDateTime saveDate = LocalDateTime.parse(datePart, DATE_FORMATTER);
                        Duration duration = Duration.between(saveDate, now);
                        
                        // Elimina se il salvataggio è nella finestra temporale
                        if (duration.toHours() <= SAVE_WINDOW_HOURS) {
                            Files.delete(saveFile);
                        }
                    } catch (DateTimeParseException e) {
                        // Ignora file con formato data non valido
                    } catch (IOException e) {
                        output.writeln("\nErrore cancellazione salvataggio: " + e.getMessage(), ColorText.ERROR);
                    }
                }
            } catch (IOException e) {
                output.writeln("\nErrore accesso salvataggi: " + e.getMessage(), ColorText.ERROR);
            }
            
            // Crea nome file con formato: nomeGiocatore_dataOra.dat
            String fileName = String.format("%s_%s.dat", 
                playerName, 
                DATE_FORMATTER.format(now)
            );
            
            Path savePath = SAVE_DIR.resolve(fileName);
            
            // Serializza i dati del gioco
            try (ObjectOutputStream out = new ObjectOutputStream(
                Files.newOutputStream(savePath, StandardOpenOption.CREATE)
            )) {
                out.writeObject(engine.getPlayerName());
                out.writeObject(engine.getGame());
                out.writeObject(engine.getTimeManager());
                out.writeObject(engine.getLongGameTime());
                out.writeObject(engine.getLogger().getFileName());
            }
            
            output.writeln("\nGioco salvato con successo!", ColorText.GREEN);
        } catch (IOException e) {
            output.writeln("\nErrore salvataggio: " + e.getMessage(), ColorText.ERROR);
        }
    }

    /**
     * Carica un salvataggio esistente.
     * 
     * @param saveName Nome del salvataggio da caricare (senza estensione)
     * @param onSuccess Callback chiamata in caso di successo
     * @param onError Callback chiamata in caso di errore
     * @param errorHandler Gestore degli errori
     * @param input Handler per l'input
     * @param output Handler per l'output
     */
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
            // Deserializza i dati del gioco
            String playerName = (String) in.readObject();
            GameDescription game = (GameDescription) in.readObject();
            TimeManager timeManager = (TimeManager) in.readObject();
            long gameTime = (long) in.readObject();
            String logFileName = (String) in.readObject();
            
            if (!LoggerInput.checkLog(logFileName)){
                throw new IOException("Il file di log associato al salvataggio non esiste");
            }
            
            // Ricrea l'istanza del motore di gioco
            Engine engine = EngineFactory.createFromSave(
                game, playerName, output, input, 
                errorHandler, new LoggerInput(errorHandler, logFileName), 
                timeManager, gameTime
            );
            
            onSuccess.accept(engine);
        } catch (IOException | ClassNotFoundException ex) {
            onError.accept("Errore caricamento: " + ex.getMessage());
        }
    }
    
    /**
    * Elimina un salvataggio esistente e il relativo file di log associato.
    * 
    * @param saveName Nome del salvataggio da eliminare (senza estensione)
    * @return true se il salvataggio è stato eliminato, false altrimenti
    */
   public static boolean deleteSave(String saveName) {
       Path savePath = SAVE_DIR.resolve(saveName + ".dat");
       try {
           String logFileName = null;
           // Legge il file di salvataggio per ottenere il nome del log
           if (Files.exists(savePath)) {
               try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(savePath))) {
                   in.readObject(); // Player name
                   in.readObject(); // Game
                   in.readObject(); // TimeManager
                   in.readObject(); // Game time
                   logFileName = (String) in.readObject(); // Log file name
               } catch (IOException | ClassNotFoundException | ClassCastException e) {
                   // Ignora errori di lettura e procedi
               }
           }

           // Elimina il file di log associato se presente
           if (logFileName != null) {
               LoggerInput.deleteLogFile(logFileName);
           }

           // Elimina il file di salvataggio
           return Files.deleteIfExists(savePath);
       } catch (IOException ex) {
           return false;
       }
   }
}