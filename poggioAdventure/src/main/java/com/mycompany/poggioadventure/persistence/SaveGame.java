package com.mycompany.poggioadventure.persistence;

import com.mycompany.poggioadventure.core.Engine;
import com.mycompany.poggioadventure.core.GameMap;
import com.mycompany.poggioadventure.core.GameStateManager;
import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.utils.ApiClientResult;
import com.mycompany.poggioadventure.core.utils.EngineFactory;
import com.mycompany.poggioadventure.core.utils.PoggioClientJersey;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.model.Room;
import com.mycompany.poggioadventure.ui.InputHandler;
import com.mycompany.poggioadventure.ui.OutputHandler;
import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.ui.ErrorHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Sistema persistenza completo per salvataggi PoggioAdventure con sincronizzazione server.
 * 
 * <p>Gestisce ciclo vita completo salvataggi: serializzazione Java, associazione logs,
 * cleanup automatico, validazione integrità e sincronizzazione backend API.
 * Implementa pattern Repository per gestione dati e Factory per ricostruzione Engine.
 * 
 * <p><b>Funzionalità principali:</b>
 * <ul>
 *   <li>Serializzazione/deserializzazione stato Engine completo</li>
 *   <li>Associazione 1:1 salvataggio-log con validazione integrità</li>
 *   <li>Cleanup automatico salvataggi precedenti per giocatore</li>
 *   <li>Sincronizzazione user state con backend via API REST</li>
 *   <li>Deserializzazione parziale per metadati senza caricamento completo</li>
 * </ul>
 * 
 * <p><b>Formato persistenza:</b>
 * <ol>
 *   <li>String playerName</li>
 *   <li>GameDescription game</li>
 *   <li>long gameTime</li>
 *   <li>String logFileName</li>
 *   <li>int currentLevelIndex</li>
 *   <li>long levelElapsedTime</li>
 *   <li>GameMap levelMapSnapshot</li>
 *   <li>List&lt;AdvObject&gt; levelInventorySnapshot</li>
 *   <li>Room levelStartingRoomSnapshot</li>
 * </ol>
 * 
 * <p><b>Pattern:</b> Repository per persistenza, Factory per ricostruzione,
 * Callback per gestione asincrona risultati.
 */
public class SaveGame {

    /** Directory base salvataggi definita in ResourceLoader */
    private static final Path SAVE_DIR = ResourceLoader.SAVES_DIRECTORY;

    /** Formattatore timestamp per naming consistente file */
    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd_MM_yyyy_HH-mm-ss");

    /**
     * Recupera lista salvataggi disponibili ordinata cronologicamente (recenti primi).
     * Filtra file .dat e rimuove estensioni per user-friendly naming.
     * 
     * @return Lista nomi salvataggi senza estensione, vuota se errori I/O
     */
    public static List<String> getSaveList() {
        try {
            Files.createDirectories(SAVE_DIR);

            return Files.list(SAVE_DIR)
                .filter(p -> p.toString().endsWith(".dat"))
                .map(p -> p.getFileName().toString().replace(".dat", ""))
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        } catch (IOException ex) {
            System.err.println("Errore durante l'accesso alla directory dei salvataggi: " + SAVE_DIR + " - " + ex.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Salva stato Engine completo con cleanup salvataggi precedenti e sync server.
     * Serializza componenti critici in ordine fisso per deserializzazione consistente.
     * 
     * @param engine Istanza Engine da persistere
     * @param output Handler per feedback utente
     */
    public static void saveGame(Engine engine, OutputHandler output) {
        PoggioClientJersey gameClient = null;
        try {
            String playerName = engine.getPlayerName();
            LocalDateTime now = LocalDateTime.now();

            // Cleanup salvataggi esistenti per giocatore
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
                        output.writeln("\nATTENZIONE: Errore durante la cancellazione del vecchio salvataggio "
                                       + saveFile.getFileName() + ": " + e.getMessage(), ColorText.ERROR);
                    }
                }
            } catch (IOException e) {
                output.writeln("\nATTENZIONE: Errore durante l'accesso ai salvataggi per la pulizia: " + e.getMessage(), ColorText.ERROR);
            }

            // Generazione nuovo file con timestamp
            String fileName = String.format("%s_%s.dat",
                playerName,
                DATE_FORMATTER.format(now)
            );
            Path savePath = SAVE_DIR.resolve(fileName);

            // Serializzazione componenti in ordine fisso
            try (ObjectOutputStream out = new ObjectOutputStream(
                Files.newOutputStream(savePath, StandardOpenOption.CREATE)
            )) {
                out.writeObject(engine.getPlayerName());
                out.writeObject(engine.getGame());
                out.writeObject(engine.getLongGameTime());
                out.writeObject(engine.getLogger().getFileName());
                
                GameStateManager gsm = engine.getGameStateManager();
                if (gsm != null) {
                    out.writeObject(gsm.getCurrentLevelIndex());
                    out.writeObject(gsm.getCurrentLevelElapsedTime());
                    out.writeObject(gsm.getLevelMapSnapshot());
                    out.writeObject(gsm.getLevelInventorySnapshot());
                    out.writeObject(gsm.getLevelStartingRoomSnapshot());
                } else {
                    // Fallback values per GSM null
                    out.writeObject(0);
                    out.writeObject(0L);
                    out.writeObject(null);
                    out.writeObject(null);
                    out.writeObject(null);
                }
            }
            output.writeln("\nGioco salvato con successo come: " + fileName, ColorText.GREEN);

            // Sync user con backend server
             gameClient = new PoggioClientJersey();
            ApiClientResult addResult = gameClient.addUser(playerName);
            if (addResult != ApiClientResult.SUCCESS_CREATED && addResult != ApiClientResult.USER_ALREADY_EXISTS) {
                 output.writeln("Attenzione: problema nella sincronizzazione utente con server (" + addResult + ")", ColorText.YELLOW);
            }
            gameClient.close();

        } catch (IOException e) {
            output.writeln("\nERRORE durante il salvataggio del gioco: " + e.getMessage(), ColorText.ERROR);
        } catch(Exception e) {
             output.writeln("\nERRORE IMPREVISTO durante il salvataggio: " + e.getMessage(), ColorText.ERROR);
             if (gameClient != null) gameClient.close();
        }
    }

    /**
     * Carica salvataggio con ricostruzione Engine via Factory e gestione callback.
     * Deserializza componenti in ordine fisso, valida log associato e ricostruisce stato.
     * 
     * @param saveName Nome file senza estensione .dat
     * @param onSuccess Callback successo con Engine ricostruito
     * @param onError Callback errore con messaggio descrittivo
     * @param errorHandler Handler errori per Engine
     * @param input Handler input per Engine
     * @param output Handler output per Engine
     */
    public static void loadSave(
        String saveName,
        Consumer<Engine> onSuccess,
        Consumer<String> onError,
        ErrorHandler errorHandler,
        InputHandler input,
        OutputHandler output
    ) {
        Path savePath = SAVE_DIR.resolve(saveName + ".dat");

        if (!Files.exists(savePath)) {
            onError.accept("Salvataggio non trovato: " + saveName + ".dat");
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(savePath))) {
            // Deserializzazione in ordine fisso matching saveGame
            String playerName = (String) in.readObject();
            GameDescription game = (GameDescription) in.readObject();
            long gameTime = (long) in.readObject();
            String logFileName = (String) in.readObject();

            int currentLevelIndex = (int) in.readObject();
            long levelElapsedTime = (long) in.readObject();
            GameMap levelMapSnapshot = (GameMap) in.readObject();
            @SuppressWarnings("unchecked")
            List<AdvObject> levelInventorySnapshot = (List<AdvObject>) in.readObject();
            Room levelStartingRoomSnapshot = (Room) in.readObject();

            // Validazione integrità log associato
            if (!LoggerInput.checkLog(logFileName)) {
                onError.accept("File di log associato ('" + logFileName + "') corrotto o non trovato. Caricamento annullato.");
                return;
            }
            LoggerInput logger = new LoggerInput(errorHandler, logFileName);

            // Ricostruzione Engine via Factory
            Engine engine = EngineFactory.createFromSave(
                game, playerName, output, input,
                errorHandler, logger,
                gameTime
            );

            // Ripristino stato GameStateManager con snapshot
            GameStateManager gsm = engine.getGameStateManager();
            if (gsm != null) {
                gsm.restoreFromSave(currentLevelIndex, levelElapsedTime, levelMapSnapshot, levelInventorySnapshot, levelStartingRoomSnapshot);
            }

            onSuccess.accept(engine);

        } catch (IOException | ClassNotFoundException | ClassCastException ex) {
            onError.accept("Errore durante il caricamento del salvataggio '" + saveName + "': " + ex.getMessage());
        } catch (Exception ex) {
            onError.accept("Errore imprevisto durante il caricamento: " + ex.getMessage());
        }
    }

    /**
     * Elimina salvataggio con cleanup file associati e sync server.
     * Rimuove user da backend, elimina log associato e file .dat locale.
     * 
     * @param saveName Nome salvataggio da eliminare
     * @param err Handler errori per logging operazioni
     * @param deletePlayer Flag eliminazione user da server
     * @return true se almeno un file locale eliminato con successo
     */
    public static boolean deleteSave(String saveName, ErrorHandler err, boolean deletePlayer) {
        Path savePath = SAVE_DIR.resolve(saveName + ".dat");
        String saveUsername = SaveGame.getUsernameFromSave(saveName);
        boolean logDeleted = false;
        boolean saveDeleted = false;
        PoggioClientJersey gameClient = null;

        if (saveUsername == null) {
            err.handleRecoverableError("Impossibile eliminare: nome utente non trovato nel salvataggio " + saveName);
            return false;
        }

        try {
            // Eliminazione user da server se richiesta
            if (deletePlayer) {
                gameClient = new PoggioClientJersey();
                ApiClientResult result = gameClient.deletePlayer(saveUsername);
                gameClient.close();
                switch(result){
                    case SUCCESS_OK:
                        break;
                    case USER_NOT_FOUND:
                        err.handleRecoverableError("Utente " + saveUsername + " non trovato sul server durante eliminazione salvataggio.");
                        break;
                    case INVALID_INPUT_CLIENT:
                        throw new Exception("Errore API deletePlayer: Input client invalido (username: " + saveUsername + ")");
                    case CONNECTION_ERROR:
                        throw new Exception("Errore API deletePlayer: Errore di comunicazione con il server.");
                    case UNAUTHORIZED:
                        throw new Exception("Errore API deletePlayer: Non autorizzato (API Key errata?).");
                    case SERVER_ERROR:
                        throw new Exception("Errore API deletePlayer: Errore interno del server.");
                    case UNKNOWN_ERROR:
                        throw new Exception("Errore API deletePlayer: Errore sconosciuto dal server.");
                    default:
                        throw new Exception("Errore API deletePlayer: Risultato API non gestito (" + result + ")");
                }
            }
            
            // Eliminazione file log associato
            Path logFilePath = findLogFileName(savePath);
            if (logFilePath != null) {
                logDeleted = LoggerInput.deleteLogFile(logFilePath);
                if (!logDeleted) {
                     err.handleRecoverableError("Impossibile eliminare file di log associato: " + logFilePath.getFileName());
                }
            } else {
                 err.handleRecoverableError("Nome file di log non trovato all'interno di " + saveName + ".dat");
            }

            // Eliminazione file salvataggio
            try {
                saveDeleted = Files.deleteIfExists(savePath);
            } catch (IOException ex) {
                err.handleRecoverableError("Errore durante l'eliminazione del file di salvataggio " + saveName + ".dat: " + ex.getMessage());
                saveDeleted = false;
            }

            return logDeleted || saveDeleted;

        } catch (Exception ex) {
            err.handleRecoverableError("Errore generale durante l'eliminazione del salvataggio '" + saveName + "': " + ex.getMessage());
            if (gameClient != null) gameClient.close();
            return false;
        }
    }

    /**
     * Estrae path file log da salvataggio usando deserializzazione parziale.
     * Legge sequenzialmente oggetti fino al quarto (logFileName) senza caricare tutto.
     * 
     * @param savePath Path completo file .dat
     * @return Path file log o null se errore/non trovato
     */
    static Path findLogFileName(Path savePath) {
        if (!Files.exists(savePath)) {
            return null;
        }
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(savePath))) {
            in.readObject(); // Skip playerName
            in.readObject(); // Skip game  
            in.readObject(); // Skip gameTime
            String logFileName = (String) in.readObject(); // Read logFileName
            return Paths.get(logFileName);
        } catch (IOException | ClassNotFoundException | ClassCastException ex) {
            // Gestisce errori comuni di deserializzazione o I/O, o cast errato.
            // Restituisce null per indicare fallimento nell'estrazione del nome log.
             System.err.println("Errore durante l'estrazione del nome log da " + savePath.getFileName() + ": " + ex.getMessage());
            return null;
        } catch (Exception ex) {
             // Cattura altre eccezioni impreviste
             System.err.println("Errore imprevisto durante l'estrazione del nome log da " + savePath.getFileName() + ": " + ex.getMessage());
            return null;
        }
    }

    /**
     * Estrae username da salvataggio usando deserializzazione parziale.
     * Legge solo primo oggetto serializzato (playerName) per efficienza.
     * 
     * @param saveName Nome file senza estensione .dat
     * @return Username estratto o null se errore/non trovato
     */
    public static String getUsernameFromSave(String saveName) {
        Path savePath = SAVE_DIR.resolve(saveName + ".dat");

        // Verifica esistenza file prima di tentare apertura
        if (!Files.exists(savePath)) {
            return null;
        }

        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(savePath))) {
            return (String) in.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}