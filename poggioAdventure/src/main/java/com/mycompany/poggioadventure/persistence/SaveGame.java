package com.mycompany.poggioadventure.persistence;

// Import delle classi Core del gioco
import com.mycompany.poggioadventure.core.utils.TimeManager; // Gestore del tempo di gioco
import com.mycompany.poggioadventure.core.Engine; // Motore principale del gioco
import com.mycompany.poggioadventure.core.GameStateManager;
import com.mycompany.poggioadventure.core.abstracts.GameDescription; // Descrizione astratta dello stato del gioco
import com.mycompany.poggioadventure.core.utils.ApiClientResult;
import com.mycompany.poggioadventure.core.utils.EngineFactory; // Factory per creare istanze di Engine
import com.mycompany.poggioadventure.core.utils.PoggioClientJersey; // Client per API REST PoggioServer
import com.mycompany.poggioadventure.ui.InputHandler; // Gestore input utente
import com.mycompany.poggioadventure.ui.OutputHandler; // Gestore output utente
import com.mycompany.poggioadventure.ui.ColorText; // Utility per testo colorato
import com.mycompany.poggioadventure.ui.ErrorHandler; // Gestore centralizzato errori

// Import standard Java IO e NIO.2 per file e serializzazione
import java.io.IOException; // Eccezione base per errori I/O
import java.io.ObjectInputStream; // Per deserializzare oggetti Java
import java.io.ObjectOutputStream; // Per serializzare oggetti Java
import java.nio.file.Files; // Utility per operazioni su file (NIO.2)
import java.nio.file.Path; // Rappresenta un percorso file (NIO.2)
import java.nio.file.Paths; // Utility per creare Path (NIO.2)
import java.nio.file.StandardOpenOption; // Opzioni per apertura/scrittura file (NIO.2)

// Import Java Time API
import java.time.LocalDateTime; // Data e ora senza fuso orario
import java.time.format.DateTimeFormatter; // Formattatore per date/ore

// Import Java Collections e Funzionali
import java.util.Collections; // Utility per collezioni (es. emptyList)
import java.util.Comparator; // Per definire ordinamenti
import java.util.List; // Interfaccia per liste
import java.util.function.Consumer; // Interfaccia funzionale per callback
import java.util.stream.Collectors; // Utility per raccogliere risultati da Stream

/**
 * Gestisce il ciclo di vita completo dei salvataggi del gioco PoggioAdventure.
 * Questa classe agisce come un utility statica per centralizzare le operazioni
 * di persistenza dello stato del gioco su file.
 *
 * <p>Responsabilità principali:
 * <ul>
 * <li>Serializzazione/deserializzazione dello stato di gioco tramite Java Serialization.</li>
 * <li>Gestione dei file di salvataggio (.dat) e dei file di log associati (.log).</li>
 * <li>Pulizia automatica dei salvataggi precedenti per lo stesso giocatore al momento del nuovo salvataggio.</li>
 * <li>Validazione base dell'integrità dei salvataggi (controllo esistenza log associato).</li>
 * <li>Interazione con il server backend (tramite PoggioClientJersey) per sincronizzare lo stato utente.</li>
 * </ul>
 *
 * <p>Caratteristiche:
 * <ul>
 * <li>Salvataggi nominati con timestamp: {@code [username]_[data_ora].dat}.</li>
 * <li>Associazione 1:1 tra file di salvataggio (.dat) e file di log (.log), il cui nome è memorizzato nel .dat.</li>
 * <li>Gestione degli errori I/O e di deserializzazione.</li>
 * <li>Utilizzo di callback (Consumer) per gestire l'esito del caricamento.</li>
 * </ul>
 *
 * <p>Pattern/Tecniche utilizzate:
 * <ul>
 * <li>Utility Class (metodi statici).</li>
 * <li>Java Serialization per la persistenza degli oggetti.</li>
 * <li>Factory Method (tramite {@link EngineFactory}) per la ricostruzione dell'engine dal salvataggio.</li>
 * <li>Callback ({@code Consumer<T>}) per la gestione asincrona/disaccoppiata dei risultati.</li>
 * <li>Partial Deserialization (in `findLogFileName`, `getUsernameFromSave`) per leggere metadati specifici.</li>
 * </ul>
 *
 * @author Strix89 | Elia-Valenza26 // Autori originali
 * @version 1.2 // Versione della classe
 */
public class SaveGame {

    /**
     * Directory base dove vengono memorizzati i file di salvataggio (.dat).
     * Il percorso effettivo è definito nella classe {@link ResourceLoader}.
     * <p>Percorso assoluto definito in {@link ResourceLoader#SAVES_DIRECTORY}
     */
    private static final Path SAVE_DIR = ResourceLoader.SAVES_DIRECTORY;

    /**
     * Formattatore utilizzato per creare la parte timestamp nei nomi dei file di salvataggio.
     * Garantisce un formato consistente e ordinabile cronologicamente (in parte).
     * <p>Formato: {@code dd_MM_yyyy_HH-mm-ss} (giorno_mese_anno_ora-minuti-secondi)
     */
    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd_MM_yyyy_HH-mm-ss");

    /**
     * Recupera la lista dei nomi dei salvataggi disponibili, ordinati dal più recente al meno recente.
     * Esplora la directory dei salvataggi, filtra i file .dat e ne restituisce i nomi senza estensione.
     *
     * <p>Operazioni eseguite:
     * <ol>
     * <li>Assicura che la directory dei salvataggi esista (la crea se necessario).</li>
     * <li>Elenca i file nella directory usando {@code Files.list()}.</li>
     * <li>Filtra mantenendo solo i file che terminano con l'estensione ".dat".</li>
     * <li>Estrae il nome del file e rimuove l'estensione ".dat".</li>
     * <li>Ordina i nomi in ordine lessicografico inverso (che corrisponde approssimativamente
     * all'ordine cronologico inverso grazie al formato del timestamp).</li>
     * <li>Colleziona i nomi in una lista.</li>
     * </ol>
     *
     * @return Una lista ({@code List<String>}) non modificabile contenente i nomi dei salvataggi
     * (senza estensione), ordinati dal più recente al meno recente.
     * Restituisce una lista vuota in caso di errore durante l'accesso alla directory (es. permessi).
     */
    public static List<String> getSaveList() {
        try {
            // Assicura che la directory esista, altrimenti la crea. Idempotente.
            Files.createDirectories(SAVE_DIR);

            // Usa Stream API per processare i file nella directory
            return Files.list(SAVE_DIR) // Ottiene uno Stream<Path> dei contenuti della directory
                .filter(p -> p.toString().endsWith(".dat")) // Filtra: solo file con estensione .dat
                .map(p -> p.getFileName().toString().replace(".dat", "")) // Trasforma: Path -> nome file senza estensione
                .sorted(Comparator.reverseOrder()) // Ordina: in ordine inverso (lessicografico, approssima cronologico)
                .collect(Collectors.toList()); // Raccoglie: i risultati in una List<String>
        } catch (IOException ex) {
            // In caso di errore I/O (es. permessi mancanti sulla directory),
            // logga implicitamente l'errore (tramite stack trace se non gestito altrove)
            // e restituisce una lista vuota per indicare fallimento controllato.
            System.err.println("Errore durante l'accesso alla directory dei salvataggi: " + SAVE_DIR + " - " + ex.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Salva lo stato corrente del gioco (rappresentato dall'istanza di {@link Engine})
     * in un file .dat. Prima di salvare, elimina tutti i salvataggi precedenti
     * appartenenti allo stesso giocatore. Tenta anche di assicurare che l'utente
     * esista sul server tramite {@link PoggioClientJersey}.
     *
     * <p>Operazioni eseguite:
     * <ol>
     * <li>Recupera il nome del giocatore dall'engine.</li>
     * <li>Ottiene il timestamp corrente.</li>
     * <li>Cerca ed elimina tutti i file .dat esistenti che iniziano con {@code nomeGiocatore + "_"}.</li>
     * <li>Genera il nuovo nome del file di salvataggio usando nome giocatore e timestamp.</li>
     * <li>Serializza gli oggetti necessari dall'engine nel nuovo file .dat. L'ordine di scrittura è FONDAMENTALE.</li>
     * <li>Notifica l'utente tramite {@link OutputHandler}.</li>
     * <li>Chiama l'API del server per aggiungere/assicurare l'esistenza dell'utente ({@code PoggioClientJersey.addUser}).</li>
     * </ol>
     *
     * <p>Componenti serializzati (in questo ordine):
     * <ul>
     * <li>{@code String playerName}</li>
     * <li>{@code GameDescription game}</li>
     * <li>{@code TimeManager timeManager}</li>
     * <li>{@code long gameTime} (tempo di gioco trascorso)</li>
     * <li>{@code String logFileName} (percorso/nome del file di log associato)</li>
     * </ul>
     *
     * @param engine L'istanza del motore di gioco {@link Engine} il cui stato deve essere salvato.
     * @param output L'handler {@link OutputHandler} usato per mostrare messaggi all'utente (es. successo, errore).
     */
    public static void saveGame(Engine engine, OutputHandler output) {
        PoggioClientJersey gameClient = null; // Dichiarato fuori dal try per poterlo chiudere nel finally implicito se usassimo try-with-resources
        try {
            String playerName = engine.getPlayerName();
            LocalDateTime now = LocalDateTime.now(); // Timestamp del salvataggio

            // --- Fase 1: Pulizia salvataggi precedenti ---
            // Cerca tutti i file nella directory dei salvataggi
            try {
                List<Path> existingSaves = Files.list(SAVE_DIR)
                    .filter(p -> { // Filtra i file che appartengono a questo giocatore
                        String fileName = p.getFileName().toString();
                        // Controlla se inizia con "nomeGiocatore_" e finisce con ".dat"
                        return fileName.startsWith(playerName + "_") && fileName.endsWith(".dat");
                    })
                    .collect(Collectors.toList()); // Raccoglie i percorsi trovati

                // Elimina ogni salvataggio precedente trovato
                for (Path saveFile : existingSaves) {
                    try {
                        Files.delete(saveFile);
                         // output.writeln("Salvataggio precedente eliminato: " + saveFile.getFileName(), ColorText.YELLOW); // Log opzionale
                    } catch (IOException e) {
                        // Se l'eliminazione fallisce, notifica l'utente ma continua il processo
                        output.writeln("\nATTENZIONE: Errore durante la cancellazione del vecchio salvataggio "
                                       + saveFile.getFileName() + ": " + e.getMessage(), ColorText.ERROR);
                    }
                }
            } catch (IOException e) {
                // Errore durante l'accesso alla directory per la pulizia
                output.writeln("\nATTENZIONE: Errore durante l'accesso ai salvataggi per la pulizia: " + e.getMessage(), ColorText.ERROR);
                // Si potrebbe decidere di interrompere qui o continuare comunque il salvataggio.
                // Il codice attuale continua.
            }

            // --- Fase 2: Creazione nuovo file di salvataggio ---
            // Genera il nome del file usando il pattern: nomeGiocatore_timestamp.dat
            String fileName = String.format("%s_%s.dat",
                playerName,
                DATE_FORMATTER.format(now) // Formatta il timestamp
            );
            Path savePath = SAVE_DIR.resolve(fileName); // Crea il percorso completo del file

            // --- Fase 3: Serializzazione ---
            // Usa try-with-resources per assicurare la chiusura di ObjectOutputStream
            try (ObjectOutputStream out = new ObjectOutputStream(
                // Apre un output stream verso il file, creando il file se non esiste
                Files.newOutputStream(savePath, StandardOpenOption.CREATE)
            )) {
                // Scrive gli oggetti nell'ordine PRESTABILITO e CRITICO
                out.writeObject(engine.getPlayerName());        // 1. Username (String)
                out.writeObject(engine.getGame());              // 2. Stato Gioco (GameDescription)
                out.writeObject(engine.getLongGameTime());      // 3. Tempo Trascorso (long) -> Autoboxing a Long
                out.writeObject(engine.getLogger().getFileName()); // 4. Nome File Log (String)
                // AGGIUNTO: Salva lo stato del livello corrente e del TimeManager
                GameStateManager gsm = engine.getGameStateManager();
                if (gsm != null) {
                    out.writeObject(gsm.getCurrentLevelIndex());    // 5. Indice livello corrente (int)
                    out.writeObject(gsm.getCurrentLevelElapsedTime()); // 6. Tempo trascorso nel livello (long)
                } else {
                    out.writeObject(0);    // 5. Livello 0 se GSM è null
                    out.writeObject(0L);   // 6. Tempo 0 se GSM è null
                }
            }
            output.writeln("\nGioco salvato con successo come: " + fileName, ColorText.GREEN);

            // --- Fase 4: Sincronizzazione con Server (opzionale/specifica applicazione) ---
            // Tenta di assicurare che l'utente esista sul server backend.
            // Potrebbe essere superfluo se l'utente viene creato altrove.
             gameClient = new PoggioClientJersey();
            ApiClientResult addResult = gameClient.addUser(playerName); // Chiama API addUser
            // Logica per gestire il risultato di addUser (opzionale)
            if (addResult != ApiClientResult.SUCCESS_CREATED && addResult != ApiClientResult.USER_ALREADY_EXISTS) {
                 output.writeln("Attenzione: problema nella sincronizzazione utente con server (" + addResult + ")", ColorText.YELLOW);
            }
            gameClient.close(); // Chiude il client Jersey

        } catch (IOException e) {
            // Errore I/O generico durante la serializzazione o creazione file
            output.writeln("\nERRORE durante il salvataggio del gioco: " + e.getMessage(), ColorText.ERROR);
             // Qui si potrebbe tentare di eliminare il file parzialmente creato, se necessario.
        } catch(Exception e) {
             // Cattura altre eccezioni impreviste durante il processo
             output.writeln("\nERRORE IMPREVISTO durante il salvataggio: " + e.getMessage(), ColorText.ERROR);
             if (gameClient != null) gameClient.close(); // Assicura chiusura client anche in caso di eccezioni non IO
        }
        // Nota: la chiusura di 'gameClient' dovrebbe idealmente essere in un blocco finally
        // per garantire l'esecuzione anche in caso di eccezioni nel blocco try principale.
        // L'implementazione attuale lo chiude solo nel flusso normale o in caso di Exception generica.
    }

    /**
     * Carica uno stato di gioco da un file .dat specificato.
     * Deserializza i componenti del gioco e utilizza {@link EngineFactory} per
     * ricostruire un'istanza funzionante di {@link Engine}. Gestisce l'esito
     * tramite callback.
     *
     * <p>Flusso operativo:
     * <ol>
     * <li>Costruisce il percorso completo del file .dat.</li>
     * <li>Verifica se il file esiste; in caso contrario, chiama {@code onError}.</li>
     * <li>Apre un {@link ObjectInputStream} per leggere dal file.</li>
     * <li>Deserializza gli oggetti nell'ordine ESATTO in cui sono stati scritti da {@code saveGame}.</li>
     * <li>Recupera il nome del file di log e verifica l'esistenza/integrità del log tramite {@link LoggerInput#checkLog}. Se fallisce, chiama {@code onError}.</li>
     * <li>Istanzia il {@link LoggerInput} associato.</li>
     * <li>Utilizza {@link EngineFactory#createFromSave} per assemblare la nuova istanza di {@link Engine}.</li>
     * <li>Se tutto ha successo, chiama {@code onSuccess} passando l'engine ricostruito.</li>
     * <li>In caso di {@link IOException} o {@link ClassNotFoundException} durante la deserializzazione, chiama {@code onError}.</li>
     * </ol>
     *
     * @param saveName Il nome del file di salvataggio (senza estensione ".dat").
     * @param onSuccess Il {@link Consumer} da eseguire in caso di successo, riceve l'{@link Engine} caricato.
     * @param onError Il {@link Consumer} da eseguire in caso di errore, riceve un messaggio ({@code String}) descrittivo.
     * @param errorHandler Il gestore {@link ErrorHandler} da passare all'engine ricostruito.
     * @param input L'handler {@link InputHandler} da passare all'engine ricostruito.
     * @param output L'handler {@link OutputHandler} da passare all'engine ricostruito.
     */
    public static void loadSave(
        String saveName,
        Consumer<Engine> onSuccess, // Callback successo
        Consumer<String> onError,   // Callback errore
        ErrorHandler errorHandler,
        InputHandler input,
        OutputHandler output
    ) {
        // Costruisce il percorso completo al file .dat
        Path savePath = SAVE_DIR.resolve(saveName + ".dat");

        // Verifica preliminare: il file di salvataggio esiste?
        if (!Files.exists(savePath)) {
            onError.accept("Salvataggio non trovato: " + saveName + ".dat");
            return; // Esce se il file non esiste
        }

        // Usa try-with-resources per assicurare la chiusura di ObjectInputStream
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(savePath))) {
            // --- Deserializzazione: L'ORDINE È CRITICO e deve matchare saveGame ---
            String playerName = (String) in.readObject();             // 1. Legge playerName
            GameDescription game = (GameDescription) in.readObject(); // 2. Legge GameDescription
            long gameTime = (long) in.readObject();                   // 3. Legge gameTime (unboxing da Long)
            String logFileName = (String) in.readObject();            // 4. Legge logFileName

            // AGGIUNTO: Legge lo stato del livello
            int currentLevelIndex = (int) in.readObject();            // 5. Indice livello corrente
            long levelElapsedTime = (long) in.readObject();             // 6. Tempo trascorso nel livello corrente

            // --- Verifica e Creazione Logger ---
            // Controlla l'integrità/esistenza del file di log associato PRIMA di creare l'engine
            if (!LoggerInput.checkLog(logFileName)) {
                 // Se il log non è valido, considera il salvataggio corrotto o incompleto
                onError.accept("File di log associato ('" + logFileName + "') corrotto o non trovato. Caricamento annullato.");
                return;
            }
            // Crea l'istanza del logger usando il nome file recuperato
            LoggerInput logger = new LoggerInput(errorHandler, logFileName);


            // --- Ricostruzione Engine ---
            // Usa la factory per creare una nuova istanza di Engine con i dati deserializzati
            Engine engine = EngineFactory.createFromSave(
                game, playerName, output, input,
                errorHandler, logger, // Passa il logger appena creato
                gameTime
            );

            // AGGIUNTO: Ripristina lo stato del GameStateManager
            GameStateManager gsm = engine.getGameStateManager();
            if (gsm != null) {
                gsm.restoreFromSave(currentLevelIndex, levelElapsedTime);
            }

            // --- Successo ---
            // Esegue il callback di successo passando l'engine pronto all'uso
            onSuccess.accept(engine);

        } catch (IOException | ClassNotFoundException | ClassCastException ex) {
            // Gestisce errori durante la deserializzazione:
            // IOException: Problemi lettura file.
            // ClassNotFoundException: Una classe salvata non è trovata nel classpath attuale (versione diversa?).
            // ClassCastException: Il tipo letto non corrisponde a quello atteso (file corrotto o ordine errato?).
            onError.accept("Errore durante il caricamento del salvataggio '" + saveName + "': " + ex.getMessage());
             // Log più dettagliato dell'eccezione potrebbe essere utile qui
             // errorHandler.handleFatalError("Dettaglio Errore Caricamento", ex);
        } catch (Exception ex) {
             // Cattura altre eccezioni impreviste
            onError.accept("Errore imprevisto durante il caricamento: " + ex.getMessage());
            // errorHandler.handleFatalError("Dettaglio Errore Caricamento Imprevisto", ex);
        }
    }

    /**
     * Elimina un file di salvataggio (.dat) e tenta di eliminare anche il
     * file di log associato. Tenta anche di eliminare l'utente corrispondente
     * dal server backend tramite {@link PoggioClientJersey}.
     *
     * <p>Strategia di eliminazione:
     * <ol>
     * <li>Chiama l'API del server per eliminare l'utente; se fallisce (lancia eccezione), interrompe e restituisce false.</li>
     * <li>Trova il nome del file di log associato leggendolo dal file .dat (tramite {@code findLogFileName}).</li>
     * <li>Tenta di eliminare il file di log trovato (tramite {@code LoggerInput.deleteLogFile}).</li>
     * <li>Tenta di eliminare il file di salvataggio (.dat).</li>
     * <li>Restituisce {@code true} se almeno uno tra il file di log o il file di salvataggio è stato eliminato con successo, {@code false} altrimenti o in caso di errore server iniziale.</li>
     * </ol>
     *
     * @param saveName Nome del salvataggio da eliminare (senza estensione ".dat").
     * @param err Il gestore {@link ErrorHandler} per registrare eventuali errori durante l'eliminazione locale.
     * @return {@code true} se l'operazione sul server ha successo E almeno uno dei file locali (.log o .dat) viene eliminato; {@code false} altrimenti.
     */
    public static boolean deleteSave(String saveName, ErrorHandler err, boolean deletePlayer) {
        Path savePath = SAVE_DIR.resolve(saveName + ".dat");
        String saveUsername = SaveGame.getUsernameFromSave(saveName); // Estrae username dal file .dat
        boolean logDeleted = false;
        boolean saveDeleted = false;
        PoggioClientJersey gameClient = null;

        // Verifica preliminare: l'username è stato estratto correttamente?
        if (saveUsername == null) {
            err.handleRecoverableError("Impossibile eliminare: nome utente non trovato nel salvataggio " + saveName);
            return false;
        }

        try {
            // Controlla l'esito della chiamata API
            if (deletePlayer) {
                // --- Fase 1: Eliminazione Utente dal Server ---
                gameClient = new PoggioClientJersey();
                ApiClientResult result = gameClient.deletePlayer(saveUsername); // Chiama API DELETE /players/{username}
                gameClient.close(); // Chiude il client dopo la chiamata
                switch(result){
                    case SUCCESS_OK:
                        // Utente eliminato con successo dal server, procedi con eliminazione locale
                        // err.handleInfo("Utente " + saveUsername + " eliminato dal server."); // Log opzionale
                        break;
                    case USER_NOT_FOUND:
                        // Utente non trovato sul server, potrebbe essere uno stato inconsistente.
                        // Procediamo comunque con l'eliminazione locale.
                        err.handleRecoverableError("Utente " + saveUsername + " non trovato sul server durante eliminazione salvataggio.");
                        break;
                    // Gestione esplicita degli errori API che interrompono l'operazione
                    case INVALID_INPUT_CLIENT: // Non dovrebbe accadere se saveUsername non è null
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
                        // Gestisce eventuali nuovi valori enum non previsti
                        throw new Exception("Errore API deletePlayer: Risultato API non gestito (" + result + ")");
                } // Fine switch risultato API
            }
            // --- Fase 2: Eliminazione File Locali (Log e Save) ---
            // Trova il nome/percorso del file di log associato leggendolo dal .dat
            Path logFilePath = findLogFileName(savePath); // Può restituire null

            // Tenta di eliminare il file di log, se trovato
            if (logFilePath != null) {
                logDeleted = LoggerInput.deleteLogFile(logFilePath); // Usa metodo specifico del Logger
                if (!logDeleted) {
                    // Logga se l'eliminazione del log fallisce, ma non interrompere
                     err.handleRecoverableError("Impossibile eliminare file di log associato: " + logFilePath.getFileName());
                }
            } else {
                // Se non troviamo il nome del log nel .dat, potrebbe essere un salvataggio incompleto/corrotto
                 err.handleRecoverableError("Nome file di log non trovato all'interno di " + saveName + ".dat");
            }

            // Tenta di eliminare il file di salvataggio .dat stesso
            try {
                saveDeleted = Files.deleteIfExists(savePath); // Restituisce true se esisteva ed è stato eliminato
            } catch (IOException ex) {
                // Errore durante l'eliminazione del file .dat
                err.handleRecoverableError("Errore durante l'eliminazione del file di salvataggio " + saveName + ".dat: " + ex.getMessage());
                saveDeleted = false; // Assicura che sia false in caso di errore
            }

            // Restituisce true se almeno uno dei file locali è stato effettivamente eliminato
            return logDeleted || saveDeleted;

        } catch (Exception ex) {
            // Cattura eccezioni lanciate dal controllo API o altri errori imprevisti
            err.handleRecoverableError("Errore generale durante l'eliminazione del salvataggio '" + saveName + "': " + ex.getMessage());
            if (gameClient != null) gameClient.close(); // Assicura chiusura client anche qui
            return false; // L'operazione è fallita
        }
        // Nota: la chiusura di 'gameClient' è gestita nel try e nel catch.
    }

    /**
     * Estrae il nome/percorso del file di log associato da un file di salvataggio (.dat)
     * senza deserializzare l'intero contenuto del file.
     * Legge gli oggetti serializzati in sequenza fino a raggiungere il quinto oggetto,
     * che per convenzione (vedi {@code saveGame}) è il nome del file di log (String).
     *
     * <p>Tecnica utilizzata: Deserializzazione Parziale.
     * <ul>
     * <li>Apre un {@link ObjectInputStream} sul file .dat.</li>
     * <li>Chiama {@code in.readObject()} per 4 volte per "saltare" i primi 4 oggetti (playerName, game, timeManager, gameTime).</li>
     * <li>Legge il quinto oggetto e lo casta a {@code String} (il nome del log).</li>
     * <li>Converte la stringa in un oggetto {@link Path}.</li>
     * </ul>
     * ATTENZIONE: Questo metodo è molto fragile. Se l'ordine o il tipo degli oggetti
     * serializzati in {@code saveGame} cambia, questo metodo smetterà di funzionare
     * o restituirà dati errati senza preavviso.
     *
     * @param savePath Il percorso ({@link Path}) completo del file di salvataggio .dat.
     * @return Il percorso ({@link Path}) del file di log associato se trovato con successo,
     * oppure {@code null} se si verifica un errore durante la lettura/deserializzazione
     * (es. file corrotto, formato cambiato, file non esistente, ClassCastException).
     */
    static Path findLogFileName(Path savePath) {
        // Verifica esistenza file prima di tentare apertura
        if (!Files.exists(savePath)) {
            return null;
        }
        // Usa try-with-resources per chiudere automaticamente l'ObjectInputStream
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(savePath))) {
            // Legge e scarta i primi 4 oggetti secondo l'ordine definito in saveGame
            in.readObject(); // 1. Salta playerName (String)
            in.readObject(); // 2. Salta game (GameDescription)
            in.readObject(); // 3. Salta gameTime (Long)

            // Legge il quarto oggetto, che dovrebbe essere il nome del file di log
            String logFileName = (String) in.readObject(); // 4. Legge logFileName (String)

            // Converte la stringa del nome file in un oggetto Path
            // Presume che logFileName contenga un percorso valido o solo il nome file.
            // Se è solo il nome, Paths.get() lo tratterà come relativo alla directory di default.
            // Il logger probabilmente lo gestirà correttamente basandosi sulla sua directory.
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
     * Recupera lo username del giocatore associato a un file di salvataggio (.dat)
     * leggendo solo il primo oggetto serializzato nel file.
     *
     * <p>Il metodo apre il file .dat specificato e legge il primissimo oggetto,
     * che per convenzione (vedi {@code saveGame}) è lo username (String).
     *
     * ATTENZIONE: Simile a {@code findLogFileName}, questo metodo dipende strettamente
     * dall'ordine di serializzazione definito in {@code saveGame}. Se l'ordine cambia,
     * questo metodo fallirà o restituirà dati errati.
     *
     * @param saveName Il nome del file di salvataggio (senza estensione ".dat").
     * @return Lo username ({@code String}) letto dal file se l'operazione ha successo,
     * oppure {@code null} se il file non esiste o si verifica un errore
     * durante la lettura/deserializzazione (IOException, ClassNotFoundException, ClassCastException).
     */
    public static String getUsernameFromSave(String saveName) {
        Path savePath = SAVE_DIR.resolve(saveName + ".dat"); // Costruisce il percorso completo

        // Verifica esistenza file prima di tentare apertura
        if (!Files.exists(savePath)) {
            return null; // File non trovato
        }

        // Usa try-with-resources per chiudere automaticamente l'ObjectInputStream
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(savePath))) {
            // Legge il primo oggetto, che deve essere lo username (String)
            return (String) in.readObject(); // Legge e casta a String
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            // Gestisce errori comuni durante la lettura/deserializzazione del primo oggetto.
            // Restituisce null per indicare fallimento.
            return null;
        } catch (Exception e) {
            // Cattura altre eccezioni impreviste
            return null;
        }
    }
}