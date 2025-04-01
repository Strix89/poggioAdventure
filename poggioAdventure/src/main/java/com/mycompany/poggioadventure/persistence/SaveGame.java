package com.mycompany.poggioadventure.persistence;

import com.mycompany.poggioadventure.core.utils.TimeManager;
import com.mycompany.poggioadventure.core.Engine;
import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.utils.EngineFactory;
import com.mycompany.poggioadventure.ui.InputHandler;
import com.mycompany.poggioadventure.ui.OutputHandler;
import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.ui.ErrorHandler;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Gestisce il ciclo di vita completo dei salvataggi del gioco.
 * 
 * <p>Responsabilità principali:
 * <ul>
 *   <li>Serializzazione/deserializzazione dello stato di gioco</li>
 *   <li>Gestione dei file di salvataggio (.dat) e log associati</li>
 *   <li>Pulizia automatica dei salvataggi duplicati</li>
 *   <li>Validazione dell'integrità dei salvataggi</li>
 * </ul>
 * 
 * <p>Caratteristiche:
 * <ul>
 *   <li>Salvataggi basati su timestamp con formato: [username]_[data_ora].dat</li>
 *   <li>Associazione 1:1 tra salvataggi e file di log</li>
 *   <li>Gestione thread-safe delle operazioni I/O</li>
 *   <li>Supporto per callback di successo/errore</li>
 * </ul>
 * 
 * <p>Pattern utilizzati:
 * <ul>
 *   <li>Factory Method per la ricostruzione dell'engine</li>
 *   <li>Callback per gestione asincrona</li>
 *   <li>Serializzazione custom per ottimizzazione spazio</li>
 * </ul>
 * 
 * @author Strix89 | Elia-Valenza26
 * @version 1.2
 */
public class SaveGame {
    /**
     * Directory base per il salvataggio dei file di gioco.
     * <p>Percorso assoluto definito in {@link ResourceLoader#SAVES_DIRECTORY}
     */
    private static final Path SAVE_DIR = ResourceLoader.SAVES_DIRECTORY;
    
    /**
     * Formattatore per i timestamp nei nomi file.
     * <p>Formato: giorno_mese_anno_ora-minuti-secondi
     */
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("dd_MM_yyyy_HH-mm-ss");
    
    /**
     * Recupera la lista dei salvataggi disponibili ordinati per data.
     * 
     * <p>Operazioni eseguite:
     * <ol>
     *   <li>Crea la directory se non esiste</li>
     *   <li>Filtra solo i file con estensione .dat</li>
     *   <li>Rimuove l'estensione dai nomi file</li>
     *   <li>Ordina in ordine cronologico inverso (dal più recente)</li>
     * </ol>
     * 
     * @return Lista non modificabile di nomi file senza estensione
     *         Lista vuota in caso di errore I/O
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
     * Serializza lo stato corrente del gioco in un file.
     * 
     * <p>Operazioni eseguite:
     * <ol>
     *   <li>Elimina eventuali salvataggi precedenti dello stesso giocatore</li>
     *   <li>Genera nome file con username e timestamp</li>
     *   <li>Serializza i componenti critici dell'engine</li>
     * </ol>
     * 
     * <p>Componenti salvati:
     * <ul>
     *   <li>Nome giocatore</li>
     *   <li>Stato del gioco (GameDescription)</li>
     *   <li>Gestore del tempo (TimeManager)</li>
     *   <li>Tempo di gioco totale</li>
     *   <li>Percorso del file di log associato</li>
     * </ul>
     * 
     * @param engine Istanza del motore di gioco da serializzare
     * @param output Handler per la visualizzazione dei messaggi
     */
    public static void saveGame(Engine engine, OutputHandler output) {
        try {
            String playerName = engine.getPlayerName();
            LocalDateTime now = LocalDateTime.now();
            
            // Elimina tutti i salvataggi precedenti con lo stesso username
            try {
                List<Path> existingSaves = Files.list(SAVE_DIR)
                        .filter(p -> {
                            String fileName = p.getFileName().toString();
                            return fileName.startsWith(playerName + "_") && fileName.endsWith(".dat");
                        })
                        .collect(Collectors.toList());
                
                for (Path saveFile : existingSaves) {
                    try {
                        Files.delete(saveFile);
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
     * Deserializza uno stato di gioco da file.
     * 
     * <p>Flusso operativo:
     * <ol>
     *   <li>Verifica esistenza file</li>
     *   <li>Deserializza i componenti in ordine prestabilito</li>
     *   <li>Verifica integrità del log associato</li>
     *   <li>Ricostruisce l'engine tramite EngineFactory</li>
     * </ol>
     * 
     * @param saveName Nome del salvataggio (senza estensione)
     * @param onSuccess Callback per gestione successo (riceve l'engine ricostruito)
     * @param onError Callback per gestione errori (riceve messaggio di errore)
     * @param errorHandler Gestore centralizzato degli errori
     * @param input Handler per l'input del gioco
     * @param output Handler per l'output del gioco
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
            // Deserializza i dati del gioco (ordine CRITICO)
            String playerName = (String) in.readObject();
            GameDescription game = (GameDescription) in.readObject();
            TimeManager timeManager = (TimeManager) in.readObject();
            long gameTime = (long) in.readObject();
            String logFileName = (String) in.readObject();
            
            // Crea il logger PRIMA di creare l'engine
            LoggerInput logger = new LoggerInput(errorHandler, logFileName);

            if (!LoggerInput.checkLog(logFileName)) {
                onError.accept("File di log corrotto");
                return;
            }
            
            // Ricrea l'istanza del motore di gioco
            Engine engine = EngineFactory.createFromSave(
                game, playerName, output, input, 
                errorHandler, logger, 
                timeManager, gameTime
            );
            
            onSuccess.accept(engine);
        } catch (IOException | ClassNotFoundException ex) {
            onError.accept("Errore caricamento: " + ex.getMessage());
        }
    }
    
    /**
     * Elimina un salvataggio e il relativo file di log.
     * 
     * <p>Strategia di eliminazione:
     * <ol>
     *   <li>Recupera il percorso del log associato</li>
     *   <li>Elimina il log (se esiste)</li>
     *   <li>Elimina il salvataggio</li>
     *   <li>Considera l'operazione riuscita se almeno una delle due eliminazioni ha successo</li>
     * </ol>
     * 
     * @param saveName Nome del salvataggio (senza estensione)
     * @param err Gestore degli errori per logging
     * @return true se almeno un file è stato eliminato, false altrimenti
     */
    public static boolean deleteSave(String saveName, ErrorHandler err) {
        Path savePath = SAVE_DIR.resolve(saveName + ".dat");
        boolean logDeleted = false;
        boolean saveDeleted = false;

        try {
            // 1. Cerca il file di log associato (senza dipendere dalla deserializzazione)
            Path logFileName = findLogFileName(savePath);

            // 2. Elimina il file di log se esiste
            if (logFileName != null) {
                logDeleted = LoggerInput.deleteLogFile(logFileName);
            }

            // 3. Elimina il salvataggio con gestione esplicita degli errori
            try {
                saveDeleted = Files.deleteIfExists(savePath);
            } catch (IOException ex) {
                // Registra l'errore ma non interrompere l'operazione
                err.handleRecoverableError("Errore eliminazione salvataggio: " + ex.getMessage());
                saveDeleted = false;
            }

            // 4. Se il salvataggio non esisteva ma il log sì, considera l'operazione comunque riuscita
            return logDeleted || saveDeleted;

        } catch (Exception ex) {
            err.handleRecoverableError("Errore durante l'eliminazione: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Estrae il percorso del file di log da un salvataggio.
     * 
     * <p>Tecnica utilizzata:
     * <ul>
     *   <li>Deserializzazione parziale del file</li>
     *   <li>Lettura sequenziale fino al campo logFileName</li>
     *   <li>Ignoro degli altri campi per performance</li>
     * </ul>
     * 
     * @param savePath Percorso completo del file .dat
     * @return Percorso del log associato o null se errore
     */
    static Path findLogFileName(Path savePath) {
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(savePath))) {
            // Legge l'ordine standard degli oggetti serializzati
            in.readObject(); // Salta playerName
            in.readObject(); // Salta game
            in.readObject(); // Salta timeManager
            in.readObject(); // Salta gameTime
            String logFileName = (String) in.readObject(); // Prende il nome del log

            return Paths.get(logFileName);
        } catch (Exception ex) {
            return null;
        }
    }
}
