package com.mycompany.poggioserver.resources;

// Import DAO e Model locali
import com.mycompany.poggioserver.db.PlayerDAO; // Interfaccia DAO per operazioni sui player
import com.mycompany.poggioserver.db.PlayerDAOImpl; // Implementazione concreta del DAO
import com.mycompany.poggioserver.model.PlayerRecord; // Oggetto modello/entità per i dati completi del player

// Import JAX-RS (Jakarta RESTful Web Services) API
import jakarta.ws.rs.*; // Annotazioni JAX-RS (@Path, @GET, @POST, @PUT, @DELETE, @PathParam, @Produces, @Consumes)
import jakarta.ws.rs.core.MediaType; // Definisce i tipi di media (JSON, OCTET_STREAM, etc.)
import jakarta.ws.rs.core.Response; // Classe per costruire risposte HTTP (status code, entity, headers)
import jakarta.ws.rs.core.StreamingOutput; // Interfaccia per inviare dati binari (file) in streaming

// Import per gestione file upload (Multipart con Jersey)
import org.glassfish.jersey.media.multipart.FormDataContentDisposition; // Contiene metadati del file uploadato (es. nome originale)
import org.glassfish.jersey.media.multipart.FormDataParam; // Annotazione per iniettare parti di una richiesta multipart

// Import Java IO/NIO per gestione file
import java.io.File; // Rappresentazione classica di un file
import java.io.FileInputStream; // Stream per leggere da un file
import java.io.IOException; // Eccezioni I/O
import java.io.InputStream; // Stream di input generico
import java.io.OutputStream; // Stream di output generico
import java.nio.charset.StandardCharsets; // Definizioni charset (UTF-8)
import java.nio.file.Files; // Utility NIO.2 per operazioni su file/directory
import java.nio.file.InvalidPathException; // Eccezione per path non validi
import java.nio.file.Path; // Interfaccia NIO.2 per rappresentare percorsi file/directory
import java.nio.file.Paths; // Factory per creare oggetti Path
import java.nio.file.StandardCopyOption; // Opzioni per la copia file (es. sovrascrittura)

// Import Java SQL (per tipi Date/Time e SQLException)
import java.sql.SQLException;
import java.sql.Date; // Tipo SQL Date
import java.sql.Time; // Tipo SQL Time

// Import per parsing Date/Time (con attenzione alla thread-safety)
import java.text.ParseException; // Eccezione durante il parsing
import java.text.SimpleDateFormat; // Classe (NON thread-safe) per formattare/parsare date

// Import Java Collections e Streams
import java.util.List; // Interfaccia lista
import java.util.stream.Stream; // Per elaborare file riga per riga

// Import Logging (SLF4J)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe Risorsa JAX-RS che definisce gli endpoint RESTful per la gestione dei giocatori (`Player`).
 * Espone operazioni CRUD (Create, Read, Update, Delete) e funzionalità aggiuntive
 * come il recupero della classifica e il download/upload di file di log associati.
 * Interagisce con lo strato DAO ({@link PlayerDAO}) per la persistenza dei dati.
 * <p>
 * Principio OOP: Interfaccia utente (web/API) separata dalla logica di business/dati (Commento originale mantenuto).
 * </p>
 * @author Strix89 // Autore originale
 */
@jakarta.ws.rs.Path("/players") // Tutte le richieste che iniziano con /players saranno gestite da questa classe
public class PlayerResource {

    // Logger SLF4J per questa classe
    private static final Logger logger = LoggerFactory.getLogger(PlayerResource.class);

    // Istanza del DAO per interagire con il database.
    // ATTENZIONE: Istanziazione diretta! In applicazioni più grandi, usare Dependency Injection (CDI, Guice, Spring).
    private final PlayerDAO playerDAO = new PlayerDAOImpl();

    // Directory sul server dove verranno salvati i file di log uploadati.
    // Costruisce un percorso assoluto basato sulla directory corrente ("." implicita) + "resources/uploaded_logs".
    // Potrebbe essere reso configurabile.
    private static final String UPLOAD_LOG_DIRECTORY = Paths.get("resources", "uploaded_logs").toAbsolutePath().toString();

    /**
     * Endpoint per aggiungere un nuovo giocatore.
     * Accetta lo username come parte del path.
     * Risponde a: {@code POST /players/{username}}
     *
     * @param username Lo username del giocatore da creare, estratto dal path.
     * @return Una {@link Response} JAX-RS:
     * - 201 Created con messaggio JSON in caso di successo.
     * - 400 Bad Request se lo username è mancante o non valido.
     * - 409 Conflict se lo username esiste già (basato sull'eccezione del DAO).
     * - 500 Internal Server Error per altri errori del database.
     */
    @POST // Risponde al metodo HTTP POST
    @jakarta.ws.rs.Path("/{username}") // Aggiunge lo username al path base (/players)
    @Produces(MediaType.APPLICATION_JSON) // Specifica che produce risposte JSON
    public Response addPlayer(@PathParam("username") String username) { // @PathParam inietta il valore dal path
        logger.info("Ricevuta richiesta POST /players/{}", username); // Log della richiesta

        // Validazione input base (presenza username nel path)
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Richiesta POST /players/{username} con username mancante o vuoto.");
            return Response.status(Response.Status.BAD_REQUEST) // Status 400
                           .entity("{\"error\":\"BAD_REQUEST\", \"message\":\"Username mancante nel path\"}") // Corpo JSON errore
                           .type(MediaType.APPLICATION_JSON) // Tipo del corpo
                           .build(); // Costruisce la risposta
        }

        try {
            // Chiama il metodo del DAO per aggiungere il giocatore
            playerDAO.addPlayer(username);

            // Se addPlayer non lancia eccezioni, l'inserimento è riuscito
            // Restituisce 201 Created
            return Response.status(Response.Status.CREATED) // Status 201
                           .entity("{\"message\":\"Giocatore '" + username + "' creato con successo.\"}")
                           .type(MediaType.APPLICATION_JSON)
                           .build();

        } catch (SQLException e) {
            // Gestisce eccezioni SQL lanciate dal DAO
            // Controlla se l'errore è un duplicato basandosi sul messaggio specifico lanciato dal DAO
            // NB: Questo accoppia la Resource al messaggio specifico del DAO, meno robusto che usare eccezioni custom.
            if (e.getMessage() != null && e.getMessage().contains("esiste già")) {
                 logger.warn("Tentativo di creare utente già esistente (rilevato da eccezione DAO): {}", username);
                 return Response.status(Response.Status.CONFLICT) // Status 409
                                .entity("{\"error\":\"USER_ALREADY_EXISTS\", \"message\":\"Username '" + username + "' esiste già.\"}")
                                .type(MediaType.APPLICATION_JSON)
                                .build();
            }
            // Per altri errori SQL, restituisce errore generico del server
             logger.error("Errore Database durante aggiunta giocatore {}: {}", username, e.getMessage(), e);
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR) // Status 500
                           .entity("{\"error\":\"SERVER_ERROR\", \"message\":\"Errore interno del server durante accesso al database: " + e.getMessage() + "\"}")
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        } catch (IllegalArgumentException e) {
            // Gestisce eccezioni lanciate dal DAO per input non valido (es. username vuoto dopo trim nel DAO)
             logger.warn("Input non valido per addPlayer (rilevato da eccezione DAO): {}", e.getMessage());
             return Response.status(Response.Status.BAD_REQUEST) // Status 400
                           .entity("{\"error\":\"INVALID_INPUT\", \"message\":\"" + e.getMessage() + "\"}")
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        }
    }


    /**
     * Endpoint per registrare una vittoria per un giocatore esistente.
     * Riceve i dati della vittoria (data, ora, durata) e il file di log associato
     * tramite una richiesta multipart/form-data. Salva il file di log sul server,
     * calcola il punteggio e aggiorna il record del giocatore nel database.
     * Risponde a: {@code PUT /players/{username}/victory}
     *
     * @param username Lo username del giocatore (dal path).
     * @param dataString La data della vittoria come stringa (formato YYYY-MM-DD).
     * @param oraString L'ora della vittoria come stringa (formato HH:MM:SS).
     * @param durataMsString La durata della partita in millisecondi come stringa.
     * @param logFileInputStream Lo stream di input del file di log uploadato.
     * @param fileDisposition Metadati del file uploadato (es. nome originale).
     * @return Una {@link Response} JAX-RS:
     * - 200 OK con dettagli (path log, score) in caso di successo.
     * - 400 Bad Request per input mancanti o formattati male.
     * - 404 Not Found se lo username non esiste nel DB.
     * - 409 Conflict se l'utente ha già un log associato.
     * - 500 Internal Server Error per errori durante salvataggio file, calcolo score o update DB.
     */
    @PUT // Risponde al metodo HTTP PUT
    @jakarta.ws.rs.Path("/{username}/victory") // Path specifico per la vittoria
    @Consumes(MediaType.MULTIPART_FORM_DATA) // Specifica che accetta dati multipart (per file upload)
    @Produces(MediaType.APPLICATION_JSON) // Produce risposte JSON
    public Response recordVictory(
        @PathParam("username") String username, // Username dal path
        @FormDataParam("data") String dataString, // Campo 'data' dal form multipart
        @FormDataParam("ora") String oraString,   // Campo 'ora' dal form multipart
        @FormDataParam("durataMs") String durataMsString, // Campo 'durataMs' dal form multipart
        @FormDataParam("logFile") InputStream logFileInputStream, // Stream del file uploadato (campo 'logFile')
        @FormDataParam("logFile") FormDataContentDisposition fileDisposition // Metadati del file (campo 'logFile')
    ) {
        logger.info("Ricevuta richiesta PUT /players/{}/victory (multipart) per registrazione vittoria...", username);

        // --- 1. Validazione Input Base (presenza parametri) ---
        if (username == null || username.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"INVALID_INPUT\", \"message\":\"Username mancante nel path.\"}").type(MediaType.APPLICATION_JSON).build();
        }
        if (dataString == null || oraString == null || durataMsString == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"INVALID_INPUT\", \"message\":\"Parametri form 'data', 'ora', 'durataMs' sono obbligatori.\"}").type(MediaType.APPLICATION_JSON).build();
        }
        // Controlla che il file sia stato inviato e abbia un nome
        if (logFileInputStream == null || fileDisposition == null || fileDisposition.getFileName() == null || fileDisposition.getFileName().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"INVALID_INPUT\", \"message\":\"File 'logFile' mancante o nome file non valido nella richiesta multipart.\"}").type(MediaType.APPLICATION_JSON).build();
        }

        // Variabili per contenere dati processati e stato
        String serverLogPath = null; // Path finale del file log sul server
        boolean fileSaved = false;   // Indica se il file è stato salvato fisicamente
        Long durataMs = null;        // Durata parsata
        Date sqlDate = null;         // Data parsata (tipo SQL)
        Time sqlTime = null;         // Ora parsata (tipo SQL)

        // Blocco try-finally per garantire la chiusura dell'InputStream ricevuto
        try {
            // --- 2. Verifica Esistenza Utente e Assenza Log Precedente ---
            logger.debug("Verifica esistenza utente '{}'...", username);
            PlayerRecord existingPlayer = playerDAO.getPlayer(username);
            if (existingPlayer == null) {
                 logger.warn("Tentativo di registrare vittoria per utente non esistente: {}", username);
                 return Response.status(Response.Status.NOT_FOUND) // 404
                                .entity("{\"error\": \"USER_NOT_FOUND\", \"message\":\"Giocatore '" + username + "' non trovato.\"}")
                                .type(MediaType.APPLICATION_JSON).build();
            }
            // Controlla se esiste già un log associato a questo utente nel DB
            if (existingPlayer.getPercorsoFileLog() != null && !existingPlayer.getPercorsoFileLog().trim().isEmpty()) {
                 logger.warn("Tentativo di sovrascrivere log per utente '{}' (log esistente: {}). Operazione bloccata.", username, existingPlayer.getPercorsoFileLog());
                 return Response.status(Response.Status.CONFLICT) // 409
                                .entity("{\"error\": \"LOG_ALREADY_EXISTS\", \"message\":\"L'utente '" + username + "' ha già un file di log associato. Impossibile sovrascrivere.\"}")
                                .type(MediaType.APPLICATION_JSON).build();
            }
            logger.debug("Utente '{}' trovato e senza log preesistente. Proseguo...", username);

            // --- 3. Parsing Input Stringhe (Data, Ora, Durata) ---
            logger.debug("Parsing parametri data='{}', ora='{}', durataMs='{}'", dataString, oraString, durataMsString);
            try {
                sqlDate = parseSqlDate(dataString); // Usa helper per parsare la data
                sqlTime = parseSqlTime(oraString);   // Usa helper per parsare l'ora
                durataMs = Long.valueOf(durataMsString); // Converte durata in Long
                if (durataMs < 0) { // Validazione aggiuntiva sulla durata
                    throw new NumberFormatException("La durata non può essere negativa.");
                }
                logger.debug("Parsing completato: sqlDate={}, sqlTime={}, durataMs={}", sqlDate, sqlTime, durataMs);
            } catch (ParseException | NumberFormatException e) {
                 // Errore durante il parsing di data, ora o durata
                 logger.warn("Formato data/ora/durata non valido per utente '{}': {}", username, e.getMessage());
                 return Response.status(Response.Status.BAD_REQUEST) // 400
                                .entity("{\"error\":\"INVALID_INPUT_FORMAT\", \"message\":\"Formato data (YYYY-MM-DD), ora (HH:MM:SS) o durata (numero >= 0) non valido: " + e.getMessage() + "\"}")
                                .type(MediaType.APPLICATION_JSON).build();
            }

            // --- 4. Salvataggio File di Log sul Server ---
            logger.debug("Salvataggio file log ricevuto per utente '{}'...", username);
            Path targetPath = null; // Path NIO.2 del file destinazione
            try {
                // Ottiene la directory di upload e la crea se non esiste
                Path uploadDir = Paths.get(UPLOAD_LOG_DIRECTORY);
                Files.createDirectories(uploadDir);

                // Prepara un nome file sicuro e univoco sul server
                String originalFileName = fileDisposition.getFileName();
                // Rimuove caratteri potenzialmente problematici dal nome originale (sanificazione base)
                String safeOriginalName = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
                // Costruisce un nome univoco: username_timestamp_nomeoriginale_sanificato
                String uniqueFileName = username + "_" + System.currentTimeMillis() + "_" + safeOriginalName;
                targetPath = uploadDir.resolve(uniqueFileName); // Path completo del file sul server
                serverLogPath = targetPath.toString(); // Converte in String per salvarlo nel DB

                // Copia lo stream di input ricevuto nella richiesta nel file di destinazione sul server
                // Files.copy chiude automaticamente l'inputStream fornito
                Files.copy(logFileInputStream, targetPath, StandardCopyOption.REPLACE_EXISTING); // Sovrascrive se esiste (improbabile con nome univoco)
                fileSaved = true; // Imposta flag a true dopo il salvataggio riuscito
                logger.info("File log per utente '{}' salvato con successo come: {}", username, serverLogPath);

            } catch (IOException e) {
                 // Errore durante la creazione directory o la copia del file
                 logger.error("Errore I/O durante il salvataggio del file log per utente '{}' a '{}': {}", username, serverLogPath, e.getMessage(), e);
                 // Non è necessario cancellare il file perché il salvataggio è fallito
                 return Response.status(Response.Status.INTERNAL_SERVER_ERROR) // 500
                                .entity("{\"error\":\"FILE_SAVE_ERROR\", \"message\":\"Errore interno del server durante il salvataggio del file di log: " + e.getMessage() + "\"}")
                                .type(MediaType.APPLICATION_JSON).build();
            } catch (InvalidPathException e) {
                 // Errore se UPLOAD_LOG_DIRECTORY o il nome file generato non sono validi
                 logger.error("Errore: Path non valido per salvataggio log per utente '{}': {}", username, e.getMessage(), e);
                 return Response.status(Response.Status.INTERNAL_SERVER_ERROR) // 500
                                .entity("{\"error\":\"SERVER_CONFIG_ERROR\", \"message\":\"Errore configurazione percorso salvataggio log sul server.\"}")
                                .type(MediaType.APPLICATION_JSON).build();
            }

            // --- 5. Calcolo Punteggio ---
            logger.debug("Calcolo punteggio per utente '{}' basato su file '{}' e durata {}ms...", username, serverLogPath, durataMs);
            long lineCount = 0; // Contatore righe file log
            Integer calculatedScore = null; // Punteggio calcolato
            try (Stream<String> stream = Files.lines(targetPath, StandardCharsets.UTF_8)) { // Legge il file riga per riga (efficiente)
                 lineCount = stream.count(); // Conta le righe
                 logger.info("File log '{}' per utente '{}' contiene {} righe.", serverLogPath, username, lineCount);
                 // Applica la formula per calcolare lo score (esempio)
                 // Punteggio base - penalità tempo (secondi) - penalità righe log
                 calculatedScore = Math.max(0, // Assicura che il punteggio non sia negativo
                                       10000 - (durataMs.intValue() / 1000) - ((int)lineCount / 20));
                 logger.info("Punteggio calcolato per utente {}: {}", username, calculatedScore);
            } catch (IOException e) {
                 // Errore durante la lettura del file APPENA salvato (es. permessi persi, disco pieno improvviso)
                 logger.error("Errore I/O durante la lettura/conteggio righe del file log '{}' per utente '{}': {}", serverLogPath, username, e.getMessage(), e);
                 // Errore critico: il file è stato salvato ma non possiamo calcolare lo score.
                 // Tentiamo di cancellare il file salvato per consistenza.
                 deleteUploadedFile(serverLogPath); // Chiama helper per pulire
                 fileSaved = false; // Resetta flag perché il file è stato rimosso
                 return Response.status(Response.Status.INTERNAL_SERVER_ERROR) // 500
                                .entity("{\"error\":\"SCORE_CALCULATION_ERROR\", \"message\":\"Errore durante l'analisi del file log salvato per calcolo punteggio: " + e.getMessage() + "\"}")
                                .type(MediaType.APPLICATION_JSON).build();
            } catch (Exception e) { // Catch generico per errori imprevisti durante il calcolo (es. divisione per zero se la formula cambiasse)
                logger.error("Errore imprevisto durante il calcolo del punteggio per utente '{}' dal file '{}': {}", username, serverLogPath, e.getMessage(), e);
                deleteUploadedFile(serverLogPath); // Pulisce il file
                fileSaved = false;
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"SCORE_CALCULATION_ERROR\", \"message\":\"Errore server imprevisto durante il calcolo del punteggio: " + e.getMessage() + "\"}").type(MediaType.APPLICATION_JSON).build();
            }

            // --- 6. Aggiornamento Database ---
            logger.debug("Aggiornamento database per utente '{}' con dati vittoria e punteggio {}...", username, calculatedScore);
            boolean updated = playerDAO.recordVictory(username, sqlDate, sqlTime, serverLogPath, durataMs, calculatedScore);

            // Controlla l'esito dell'aggiornamento DB
            if (updated) {
                // Tutto è andato a buon fine!
                 logger.info("Vittoria, log e punteggio ({}) registrati con successo nel DB per utente '{}'.", calculatedScore, username);
                 // Restituisce 200 OK con un messaggio di successo e alcuni dettagli (path log, score)
                 return Response.ok("{\"message\":\"Vittoria registrata con successo per '" + username + "'\", \"logFilePath\":\"" + serverLogPath + "\", \"score\":" + calculatedScore + "}")
                                .type(MediaType.APPLICATION_JSON).build();
            } else {
                // Questo caso è strano: l'utente esisteva (controllo al punto 2), il file è stato salvato, lo score calcolato,
                // ma l'UPDATE nel DAO non ha modificato righe. Potrebbe indicare un problema concorrente
                // (utente cancellato nel frattempo?) o un errore nella logica del DAO.
                 logger.error("ERRORE INASPETTATO: L'aggiornamento recordVictory nel DAO ha fallito per l'utente '{}' dopo i controlli preliminari e il salvataggio del file. Eseguo cleanup del file log.", username);
                 deleteUploadedFile(serverLogPath); // Tenta di pulire il file log orfano
                 return Response.status(Response.Status.INTERNAL_SERVER_ERROR) // 500
                                .entity("{\"error\":\"DB_UPDATE_FAILED\", \"message\":\"Errore inaspettato: aggiornamento del database fallito dopo salvataggio file e calcolo score.\"}")
                                .type(MediaType.APPLICATION_JSON).build();
            }

        // Gestione eccezioni globali per il metodo (quelle non già gestite nei blocchi interni)
        } catch (SQLException e) {
             // Errore SQL avvenuto durante getPlayer o recordVictory
             logger.error("Errore Database nell'operazione di registrazione vittoria per utente '{}': {}", username, e.getMessage(), e);
             // Se l'errore avviene DOPO che il file è stato salvato, tentiamo di cancellarlo
             if (fileSaved) deleteUploadedFile(serverLogPath);
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"DB_ERROR\", \"message\":\"Errore database durante registrazione vittoria: " + e.getMessage() + "\"}").type(MediaType.APPLICATION_JSON).build();
        } catch (IllegalArgumentException e) {
            // Può venire da getPlayer se l'username è invalido (anche se già controllato all'inizio)
             logger.warn("Input non valido rilevato dal DAO per utente '{}': {}", username, e.getMessage());
             // A questo punto il file non dovrebbe essere stato salvato
             return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"INVALID_INPUT\", \"message\":\"" + e.getMessage() + "\"}").type(MediaType.APPLICATION_JSON).build();
        } catch (Exception e) { // Catch generico per ogni altra eccezione imprevista
             logger.error("Errore generico non previsto durante registrazione vittoria per utente '{}': {}", username, e.getMessage(), e);
             // Tentiamo la pulizia del file log per sicurezza, se era stato salvato
             if (fileSaved) deleteUploadedFile(serverLogPath);
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"UNEXPECTED_SERVER_ERROR\", \"message\":\"Errore server imprevisto: " + e.getMessage() + "\"}").type(MediaType.APPLICATION_JSON).build();
        } finally {
            // Blocco finally per assicurare la chiusura dell'InputStream del file uploadato,
            // nel caso non sia stato già chiuso da Files.copy o da un'eccezione precedente.
            // È una doppia sicurezza.
             if (logFileInputStream != null) {
                 try {
                     logFileInputStream.close();
                 } catch (IOException e) {
                     logger.warn("Errore durante la chiusura (nel finally) dell'InputStream del file log per utente '{}': {}", username, e.getMessage());
                 }
             }
        }
    }


    /**
     * Endpoint per ottenere i dati (record completo) di un giocatore specifico.
     * Risponde a: {@code GET /players/{username}}
     *
     * @param username Lo username del giocatore da recuperare (dal path).
     * @return Una {@link Response} JAX-RS:
     * - 200 OK con il {@link PlayerRecord} in formato JSON se trovato.
     * - 404 Not Found se lo username non esiste.
     * - 400 Bad Request se lo username è mancante o invalido.
     * - 500 Internal Server Error per errori del database.
     */
    @GET // Risponde al metodo HTTP GET
    @jakarta.ws.rs.Path("/{username}") // Path con parametro username
    @Produces(MediaType.APPLICATION_JSON) // Produce JSON (il PlayerRecord o un errore JSON)
    public Response getPlayer(@PathParam("username") String username) {
         logger.info("Ricevuta richiesta GET /players/{}", username);
         // Validazione input base
         if (username == null || username.trim().isEmpty()) {
             return Response.status(Response.Status.BAD_REQUEST) // 400
                            .entity("{\"error\":\"BAD_REQUEST\", \"message\":\"Username mancante nel path.\"}")
                            .type(MediaType.APPLICATION_JSON)
                            .build();
         }
        try {
            // Chiama il DAO per ottenere i dati del giocatore
            PlayerRecord player = playerDAO.getPlayer(username);

            // Controlla se il giocatore è stato trovato
            if (player != null) {
                // Giocatore trovato: restituisci 200 OK con l'oggetto PlayerRecord.
                // Jersey (con GsonFeature) si occuperà di serializzare 'player' in JSON.
                return Response.ok(player).build(); // Status 200
            } else {
                // Giocatore non trovato
                 logger.warn("Richiesta GET per utente non esistente: {}", username);
                 return Response.status(Response.Status.NOT_FOUND) // Status 404
                                .entity("{\"error\":\"USER_NOT_FOUND\", \"message\":\"Giocatore '" + username + "' non trovato.\"}")
                                .type(MediaType.APPLICATION_JSON)
                                .build();
            }
        } catch (SQLException e) {
             // Errore durante l'accesso al database
             logger.error("Errore Database durante recupero giocatore {}: {}", username, e.getMessage(), e);
              return Response.status(Response.Status.INTERNAL_SERVER_ERROR) // Status 500
                            .entity("{\"error\":\"SERVER_ERROR\", \"message\":\"Errore interno del server durante accesso al database: " + e.getMessage() + "\"}")
                            .type(MediaType.APPLICATION_JSON)
                            .build();
        } catch (IllegalArgumentException e) {
            // Errore input rilevato dal DAO
              logger.warn("Input non valido per getPlayer (rilevato da eccezione DAO): {}", e.getMessage());
              return Response.status(Response.Status.BAD_REQUEST) // Status 400
                            .entity("{\"error\":\"INVALID_INPUT\", \"message\":\"" + e.getMessage() + "\"}")
                            .type(MediaType.APPLICATION_JSON)
                            .build();
        } catch (Exception e) { // Catch generico per sicurezza
            logger.error("Errore imprevisto recupero giocatore {}: {}", username, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"SERVER_ERROR\", \"message\":\"Errore server imprevisto: " + e.getMessage() + "\"}").type(MediaType.APPLICATION_JSON).build();
        }
    }

    // --- Metodi di utility privati ---

    /**
     * Helper privato per parsare una stringa in formato "yyyy-MM-dd" in un oggetto {@code java.sql.Date}.
     * Utilizza {@link SimpleDateFormat}, che NON è thread-safe, quindi ne crea una nuova istanza ad ogni chiamata.
     * Imposta `setLenient(false)` per rifiutare date formalmente non valide (es. 30 Febbraio).
     *
     * @param dateString La stringa da parsare.
     * @return L'oggetto {@code java.sql.Date} corrispondente, o {@code null} se la stringa input è {@code null}.
     * @throws ParseException Se la stringa non è nel formato atteso "yyyy-MM-dd" o rappresenta una data non valida.
     */
    private Date parseSqlDate(String dateString) throws ParseException {
        if (dateString == null) return null; // Gestisce input null
        // Crea una nuova istanza ad ogni chiamata per evitare problemi di thread-safety
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false); // Modalità rigorosa: rifiuta date invalide
        try {
            // Parsa la stringa in un java.util.Date
             java.util.Date utilDate = sdf.parse(dateString);
            // Converte java.util.Date in java.sql.Date (troncando l'eventuale parte oraria)
             return new java.sql.Date(utilDate.getTime());
        } catch (ParseException e) {
            // Rilancia l'eccezione aggiungendo informazione sul formato atteso
             throw new ParseException("Formato data non valido, atteso yyyy-MM-DD: '" + dateString + "'", e.getErrorOffset());
        }
    }

    /**
     * Helper privato per parsare una stringa in formato "HH:mm:ss" in un oggetto {@code java.sql.Time}.
     * Utilizza {@link SimpleDateFormat}, che NON è thread-safe, quindi ne crea una nuova istanza ad ogni chiamata.
     * Imposta `setLenient(false)` per rifiutare orari formalmente non validi.
     *
     * @param timeString La stringa da parsare.
     * @return L'oggetto {@code java.sql.Time} corrispondente, o {@code null} se la stringa input è {@code null}.
     * @throws ParseException Se la stringa non è nel formato atteso "HH:mm:ss" o rappresenta un orario non valido.
     */
    private Time parseSqlTime(String timeString) throws ParseException {
         if (timeString == null) return null; // Gestisce input null
         // Crea una nuova istanza ad ogni chiamata
         SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
         sdf.setLenient(false); // Modalità rigorosa
         try {
            // Parsa la stringa in un java.util.Date (la data sarà quella dell'epoca, ma non importa per Time)
             java.util.Date utilDate = sdf.parse(timeString);
            // Converte java.util.Date in java.sql.Time (mantenendo solo l'informazione oraria)
            return new java.sql.Time(utilDate.getTime());
         } catch (ParseException e) {
            // Rilancia l'eccezione aggiungendo informazione sul formato atteso
             throw new ParseException("Formato ora non valido, atteso HH:mm:ss: '" + timeString + "'", e.getErrorOffset());
         }
    }

    /**
     * Helper privato per tentare di eliminare un file precedentemente uploadato,
     * tipicamente chiamato in scenari di errore per fare cleanup.
     * Logga l'esito ma non lancia eccezioni per non mascherare l'errore originale
     * che ha causato la necessità di cleanup.
     *
     * @param filePath Il percorso (come String) del file da eliminare. Se null, non fa nulla.
     */
    private void deleteUploadedFile(String filePath) {
        if (filePath != null) {
            try {
                // Tenta di eliminare il file usando NIO.2 Files.deleteIfExists
                // deleteIfExists non lancia eccezione se il file non esiste, ma restituisce false.
                boolean deleted = Files.deleteIfExists(Paths.get(filePath));
                if (deleted) {
                    // Logga successo della pulizia
                     logger.info("File temporaneo/orfano '{}' cancellato con successo a seguito di errore.", filePath);
                } else {
                    // Logga se il file non esisteva (potrebbe essere normale in alcuni scenari di errore)
                      logger.warn("Tentativo di cancellare file '{}' per cleanup, ma non è stato trovato (potrebbe essere già stato cancellato o mai creato completamente).", filePath);
                }
            } catch (IOException e) {
                // Errore I/O durante il tentativo di eliminazione (es. permessi, lock)
                // Logga l'errore ma NON rilanciare per non nascondere la causa originale del problema.
                 logger.error("Impossibile cancellare il file '{}' durante il cleanup: {}", filePath, e.getMessage(), e);
            } catch (InvalidPathException e) {
                 // Errore se il path salvato nel DB o generato è sintatticamente invalido
                 logger.error("Impossibile cancellare il file durante il cleanup a causa di un path non valido: '{}'", filePath, e);
            }
        }
    }

    /**
     * Endpoint per eliminare un giocatore esistente.
     * Risponde a: {@code DELETE /players/{username}}
     *
     * @param username Lo username del giocatore da eliminare (dal path).
     * @return Una {@link Response} JAX-RS:
     * - 200 OK con messaggio JSON se l'eliminazione ha successo.
     * - 404 Not Found se lo username non viene trovato.
     * - 400 Bad Request se lo username è mancante o invalido.
     * - 500 Internal Server Error per errori del database.
     */
    @DELETE // Risponde al metodo HTTP DELETE
    @jakarta.ws.rs.Path("/{username}") // Path con parametro username
    @Produces(MediaType.APPLICATION_JSON) // Produce risposte JSON
    public Response deletePlayer(@PathParam("username") String username) {
          logger.info("Ricevuta richiesta DELETE /players/{}", username);
          // Validazione input base
          if (username == null || username.trim().isEmpty()) {
              return Response.status(Response.Status.BAD_REQUEST) // 400
                             .entity("{\"error\": \"INVALID_INPUT\", \"message\":\"Username mancante nel path\"}")
                             .type(MediaType.APPLICATION_JSON).build();
          }

          try {
            // Chiama il metodo del DAO per eliminare il giocatore
              boolean deleted = playerDAO.deletePlayer(username);

              // Controlla se l'eliminazione è avvenuta (il DAO restituisce true se ha eliminato > 0 righe)
              if (deleted) {
                  // Successo: restituisce 200 OK con messaggio di conferma
                  // (Alternativa comune per DELETE è 204 No Content con corpo vuoto)
                  logger.info("Utente {} eliminato con successo.", username);
                  return Response.ok("{\"message\":\"Giocatore '" + username + "' eliminato con successo.\"}")
                                 .type(MediaType.APPLICATION_JSON).build();
              } else {
                  // Il DAO ha restituito false, significa che l'utente non è stato trovato
                  logger.warn("Tentativo di eliminare utente non esistente: {}", username);
                  return Response.status(Response.Status.NOT_FOUND) // 404
                                 .entity("{\"error\": \"USER_NOT_FOUND\", \"message\":\"Giocatore '" + username + "' non trovato.\"}")
                                 .type(MediaType.APPLICATION_JSON).build();
              }
          } catch (SQLException e) {
              // Errore durante l'accesso al database
               logger.error("Errore Database durante eliminazione giocatore {}: {}", username, e.getMessage(), e);
               return Response.status(Response.Status.INTERNAL_SERVER_ERROR) // 500
                              .entity("{\"error\": \"SERVER_ERROR\", \"message\":\"Errore interno del server durante l'eliminazione: " + e.getMessage() + "\"}")
                              .type(MediaType.APPLICATION_JSON).build();
          } catch (IllegalArgumentException e) {
              // Errore input rilevato dal DAO
               logger.warn("Input non valido per deletePlayer (rilevato da eccezione DAO): {}", e.getMessage());
               return Response.status(Response.Status.BAD_REQUEST) // 400
                              .entity("{\"error\": \"INVALID_INPUT\", \"message\":\"" + e.getMessage() + "\"}")
                              .type(MediaType.APPLICATION_JSON).build();
          } catch (Exception e) { // Catch generico per sicurezza
              logger.error("Errore imprevisto eliminazione giocatore {}: {}", username, e.getMessage(), e);
              return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"SERVER_ERROR\", \"message\":\"Errore server imprevisto: " + e.getMessage() + "\"}").type(MediaType.APPLICATION_JSON).build();
          }
    }

    /**
     * Endpoint per recuperare la classifica dei giocatori.
     * Recupera una lista di {@link RankingEntryDTO} ordinata per punteggio decrescente.
     * Risponde a: {@code GET /players/ranking}
     *
     * @return Una {@link Response} JAX-RS:
     * - 200 OK con la lista della classifica (anche se vuota) in formato JSON.
     * - 500 Internal Server Error in caso di errori del database.
     */
    @GET // Risponde al metodo HTTP GET
    @jakarta.ws.rs.Path("/ranking") // Path specifico per la classifica
    @Produces(MediaType.APPLICATION_JSON) // Produce una lista JSON
    public Response getPlayerRanking() {
        logger.info("Ricevuta richiesta GET /players/ranking");

        try {
            // Chiama il metodo del DAO per ottenere la lista della classifica
            List<RankingEntryDTO> rankingList = playerDAO.getRanking();

            // Restituisce sempre 200 OK, anche se la lista è vuota.
            // Jersey (con GsonFeature) serializzerà la lista (o una lista vuota []) in JSON.
            return Response.ok(rankingList) // Status 200, corpo = lista
                           .type(MediaType.APPLICATION_JSON) // Assicura Content-Type corretto
                           .build();
        } catch (SQLException e) {
             // Errore durante l'accesso al database
             logger.error("Errore Database durante recupero classifica: {}", e.getMessage(), e);
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR) // 500
                            .entity("{\"error\": \"SERVER_ERROR\", \"message\":\"Errore interno del server durante il recupero della classifica: " + e.getMessage() + "\"}")
                            .type(MediaType.APPLICATION_JSON).build();
        } catch (Exception e) { // Catch generico per sicurezza
            logger.error("Errore imprevisto durante recupero classifica: {}", e.getMessage(), e);
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR) // 500
                            .entity("{\"error\": \"SERVER_ERROR\", \"message\":\"Errore server imprevisto: " + e.getMessage() + "\"}")
                            .type(MediaType.APPLICATION_JSON).build();
        }
    }


    /**
     * Endpoint per scaricare il file di log associato a un giocatore.
     * Risponde a: {@code GET /players/{username}/log}
     *
     * @param username Lo username del giocatore di cui scaricare il log (dal path).
     * @return Una {@link Response} JAX-RS:
     * - 200 OK con il contenuto del file log come {@code application/octet-stream} e header per il download, se trovato.
     * - 404 Not Found se l'utente o il log associato non esistono, o se il file non è trovato/leggibile sul server.
     * - 403 Forbidden se viene rilevato un tentativo di path traversal.
     * - 400 Bad Request se lo username è mancante o invalido.
     * - 500 Internal Server Error per errori DB o I/O durante lo streaming.
     */
    @GET // Risponde al metodo HTTP GET
    @jakarta.ws.rs.Path("/{username}/log") // Path specifico per il download del log
    @Produces(MediaType.APPLICATION_OCTET_STREAM) // Produce un flusso di byte (il file)
    public Response downloadLogFile(@PathParam("username") String username) {
        logger.info("Ricevuta richiesta GET /players/{}/log per download log", username);

        // Validazione input base
        if (username == null || username.trim().isEmpty()) {
            // Qui restituiamo JSON errore anche se @Produces è OCTET_STREAM.
            // JAX-RS è abbastanza flessibile da permetterlo per le risposte di errore.
            return Response.status(Response.Status.BAD_REQUEST) // 400
                           .entity("{\"error\":\"INVALID_INPUT\", \"message\":\"Username mancante nel path.\"}")
                           .type(MediaType.APPLICATION_JSON).build();
        }

        try {
            // --- 1. Recupera il record del giocatore dal DB ---
            logger.debug("Recupero dati giocatore '{}' per trovare path log...", username);
            PlayerRecord player = playerDAO.getPlayer(username);

            // --- 2. Controlla se utente e percorso log esistono nel DB ---
            if (player == null) {
                 logger.warn("Download log fallito: utente '{}' non trovato nel DB.", username);
                 return Response.status(Response.Status.NOT_FOUND) // 404
                                .entity("{\"error\":\"USER_NOT_FOUND\", \"message\":\"Giocatore '" + username + "' non trovato.\"}")
                                .type(MediaType.APPLICATION_JSON).build();
            }
            String serverLogPath = player.getPercorsoFileLog(); // Ottiene il path salvato nel DB
            if (serverLogPath == null || serverLogPath.trim().isEmpty()) {
                 logger.warn("Download log fallito: nessun percorso log registrato nel DB per utente '{}'.", username);
                 return Response.status(Response.Status.NOT_FOUND) // 404 (Log non trovato semanticamente)
                                .entity("{\"error\":\"LOG_NOT_FOUND_IN_DB\", \"message\":\"Nessun file di log risulta associato a '" + username + "' nel database.\"}")
                                .type(MediaType.APPLICATION_JSON).build();
            }
            logger.debug("Path log per utente '{}' trovato nel DB: {}", username, serverLogPath);

            // --- 3. Validazione Sicurezza Percorso e Esistenza/Accessibilità File sul Server ---
            Path logPath;         // Oggetto NIO.2 Path
            File logFile;         // Oggetto IO File
            try {
                // Converte la stringa path dal DB in un oggetto Path e lo normalizza
                // (es. risolve ".", "..", rimuove slash duplicati)
                logPath = Paths.get(serverLogPath).normalize();
                logFile = logPath.toFile(); // Converte in oggetto File per controlli esistenza/lettura

                // --- IMPORTANTE: Controllo Sicurezza Anti Path Traversal ---
                // Verifica che il percorso normalizzato del file richiesto si trovi EFFETTIVAMENTE
                // all'interno della directory designata per gli upload (UPLOAD_LOG_DIRECTORY).
                // Questo previene che un utente malintenzionato possa fornire un path relativo (es. ../../../etc/passwd)
                // nel DB (o tramite altri exploit) per accedere a file arbitrari sul server.
                Path uploadDir = Paths.get(UPLOAD_LOG_DIRECTORY).toAbsolutePath().normalize(); // Ottiene il path assoluto e normalizzato della directory di upload
                // Controlla se il path assoluto e normalizzato del file richiesto inizia con il path della directory di upload
                if (!logPath.toAbsolutePath().startsWith(uploadDir)) {
                     // Se non inizia, è un tentativo di accedere a file fuori dalla directory consentita!
                     logger.error("Tentativo di PATH TRAVERSAL bloccato! Utente: {}, Path richiesto (dal DB): '{}', Path normalizzato: '{}', Directory upload attesa: '{}'",
                                 username, serverLogPath, logPath.toAbsolutePath(), uploadDir);
                     return Response.status(Response.Status.FORBIDDEN) // 403 Forbidden è la risposta corretta
                                    .entity("{\"error\":\"ACCESS_DENIED\", \"message\":\"Accesso al percorso del file specificato non consentito.\"}")
                                    .type(MediaType.APPLICATION_JSON).build();
                }
                logger.debug("Controllo Path Traversal superato per '{}'.", logPath.toAbsolutePath());
                // --- FINE Controllo Path Traversal ---

                // Controlla se il file esiste fisicamente sul server, è un file normale (non directory) e possiamo leggerlo
                if (!logFile.exists() || !logFile.isFile() || !logFile.canRead()) {
                     // Il DB conteneva un path, ma il file non esiste più sul server o non è accessibile
                     logger.error("Download log fallito: file non trovato o non leggibile sul server '{}' per utente '{}'. (Inconsistenza DB/Filesystem?)", serverLogPath, username);
                     return Response.status(Response.Status.NOT_FOUND) // 404 (File non trovato effettivamente sul server)
                                    .entity("{\"error\":\"FILE_NOT_FOUND_ON_SERVER\", \"message\":\"Il file di log associato non è stato trovato o non è leggibile sul server.\"}")
                                    .type(MediaType.APPLICATION_JSON).build();
                }
                logger.debug("File log '{}' trovato e leggibile sul server.", serverLogPath);

            } catch (InvalidPathException e) {
                // Errore se il path stringa letto dal DB non è un path valido per il filesystem attuale
                 logger.error("Download log fallito: il percorso '{}' memorizzato nel DB per utente '{}' non è un path valido: {}", serverLogPath, username, e.getMessage());
                 return Response.status(Response.Status.INTERNAL_SERVER_ERROR) // 500
                                .entity("{\"error\":\"INVALID_FILE_PATH_STORED\", \"message\":\"Il percorso del file log associato all'utente non è valido.\"}")
                                .type(MediaType.APPLICATION_JSON).build();
            }

            // --- 4. Prepara la Risposta per lo Streaming del File ---
            // Ottiene il nome "pulito" del file (senza percorso) da suggerire al browser/client per il download
            String downloadFilename = logFile.getName();

            // Crea un oggetto StreamingOutput. L'interfaccia funzionale permette di scrivere
            // direttamente sull'OutputStream della risposta HTTP. Questo è efficiente per file
            // grandi perché evita di caricare l'intero file in memoria sul server.
            StreamingOutput fileStream = (OutputStream outputStreamHttp) -> { // 'outputStreamHttp' è lo stream della risposta HTTP
                // Usa try-with-resources per aprire un FileInputStream al file log
                try (FileInputStream fis = new FileInputStream(logFile)) {
                    logger.debug("Inizio streaming file '{}'...", logFile.getName());
                    byte[] buffer = new byte[4096]; // Buffer per leggere/scrivere chunks di dati
                    int bytesRead;
                    // Legge blocchi di dati dal file (fis.read) e li scrive direttamente
                    // sull'output stream della risposta HTTP (outputStreamHttp.write) finché non finisce il file (read restituisce -1)
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        outputStreamHttp.write(buffer, 0, bytesRead);
                    }
                    outputStreamHttp.flush(); // Assicura che tutti i dati bufferizzati siano inviati
                    logger.debug("Streaming file '{}' completato.", logFile.getName());
                 } catch (IOException e) {
                    // Errore durante la lettura dal file locale o la scrittura sulla risposta HTTP
                    // (es. connessione chiusa dal client, disco illeggibile)
                     logger.error("Errore durante lo streaming del file log '{}' per utente '{}': {}", serverLogPath, username, e.getMessage());
                     // È difficile inviare un errore JSON qui perché lo streaming potrebbe essere già iniziato.
                     // Lanciare WebApplicationException è un modo standard per segnalare l'errore a JAX-RS.
                     throw new WebApplicationException("Errore durante lo streaming del file log.", e, Response.Status.INTERNAL_SERVER_ERROR);
                 }
                 // FileInputStream viene chiuso automaticamente dal try-with-resources
            }; // Fine lambda StreamingOutput

            // Costruisce la risposta 200 OK con il corpo come StreamingOutput
            logger.info("Invio file log '{}' ({} bytes) per utente '{}' come allegato '{}'",
                       serverLogPath, logFile.length(), username, downloadFilename);
            return Response.ok(fileStream, MediaType.APPLICATION_OCTET_STREAM) // Imposta il corpo e il Content-Type generico per file binari/download
                           // Aggiunge l'header 'Content-Disposition' con 'attachment'.
                           // Questo suggerisce al browser di scaricare il file ("Salva con nome")
                           // invece di tentare di visualizzarlo inline, usando il 'downloadFilename'.
                           .header("Content-Disposition", "attachment; filename=\"" + downloadFilename + "\"")
                           // Aggiunge l'header 'Content-Length' con la dimensione del file.
                           // Utile per il client (es. barra di progresso del download).
                           .header("Content-Length", logFile.length())
                           .build(); // Costruisce e invia la risposta

        } catch (SQLException e) {
            // Errore DB durante il recupero iniziale dei dati del player
             logger.error("Errore Database durante recupero dati per download log utente {}: {}", username, e.getMessage(), e);
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"DB_ERROR\", \"message\":\"Errore database durante recupero informazioni utente.\"}" ).type(MediaType.APPLICATION_JSON).build();
        } catch (Exception e) { // Catch generico per altri errori imprevisti
             logger.error("Errore imprevisto durante preparazione download log per utente {}: {}", username, e.getMessage(), e);
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"UNEXPECTED_SERVER_ERROR\", \"message\":\"Errore server imprevisto.\"}" ).type(MediaType.APPLICATION_JSON).build();
        }
    }
}