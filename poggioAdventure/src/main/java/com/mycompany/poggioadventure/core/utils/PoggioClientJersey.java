package com.mycompany.poggioadventure.core.utils;

import com.mycompany.poggioadventure.persistence.RankingEntryDTO; // DTO per i dati della classifica
import com.mycompany.poggioadventure.persistence.ResourceLoader; // Utility per caricare/trovare percorsi risorse

// Import Jakarta RS (JAX-RS) per il client REST
import jakarta.ws.rs.client.Client; // Interfaccia principale del client JAX-RS
import jakarta.ws.rs.client.ClientBuilder; // Factory per creare istanze di Client
import jakarta.ws.rs.client.Entity; // Utility per creare entità (corpo della richiesta)
import jakarta.ws.rs.client.WebTarget; // Rappresenta l'URI di una risorsa target
import jakarta.ws.rs.core.MediaType; // Definisce i tipi di media (es. application/json)
import jakarta.ws.rs.core.Response; // Rappresenta la risposta HTTP ricevuta
import jakarta.ws.rs.core.GenericType; // Usato per deserializzare tipi generici (es. List<T>)
import jakarta.ws.rs.ProcessingException; // Eccezione per errori durante l'elaborazione della richiesta/risposta (es. I/O, rete)
import jakarta.ws.rs.WebApplicationException; // Eccezione base per errori specifici dell'applicazione web (spesso legata a status HTTP)

// Import Jersey specifici per il supporto MultiPart (upload file)
import org.glassfish.jersey.media.multipart.FormDataMultiPart; // Contenitore per dati multipart/form-data
import org.glassfish.jersey.media.multipart.MultiPartFeature; // Registra il supporto per multipart nel client
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart; // Parte specifica per allegare file

import java.io.File; // Rappresenta un file nel filesystem locale
import java.io.IOException; // Eccezione per errori di Input/Output
import java.io.InputStream; // Stream di input per leggere dati (es. corpo risposta file)
import java.nio.charset.StandardCharsets; // Set di caratteri standard (es. UTF-8)
import java.nio.file.Files; // Utility per operazioni su file e directory (NIO.2)
import java.nio.file.Path; // Rappresenta un percorso nel filesystem (NIO.2)
import java.nio.file.Paths; // Utility per creare oggetti Path
import java.nio.file.StandardCopyOption; // Opzioni per la copia di file (es. sovrascrittura)
import java.nio.file.StandardOpenOption; // Opzioni per l'apertura/scrittura di file
import java.time.LocalDate; // Data senza orario
import java.time.LocalTime; // Orario senza data
import java.time.format.DateTimeFormatter; // Formattatore per date/orari
import java.util.ArrayList; // Implementazione di List ridimensionabile
import java.util.Arrays; // Utility per array (es. asList)
import java.util.Collections; // Utility per collezioni (es. emptyList)
import java.util.HashMap; // Implementazione di Map basata su hash table
import java.util.List; // Interfaccia per collezioni ordinate
import java.util.Map; // Interfaccia per collezioni chiave-valore

/**
 * Client basato su Jersey per interagire con l'API REST del server PoggioServer.
 * Utilizza JAX-RS API standard con implementazione Jersey.
 * Ogni metodo pubblico che interagisce con l'API restituisce un {@link ApiClientResult}
 * per indicare l'esito dell'operazione, ad eccezione di `getRanking` che restituisce
 * la lista di dati o una lista vuota in caso di errore.
 */
/**
 *
 * @author Strix89
 */
public class PoggioClientJersey {

    // Costanti di configurazione e sicurezza
    private static final String SHARED_SECRET = "Z10_F4_WO4H"; // Segreto condiviso per l'autenticazione via header
    private static final String HEADER_API_KEY = "X-API-Key"; // Nome dell'header HTTP per inviare il segreto
    private static final String SERVER_URI = "http://localhost:8080/"; // URI base del server API

    // Istanza del client Jersey (thread-safe, riutilizzabile)
    private final Client client;
    // URI base del server, memorizzato per comodità
    private final String serverBaseUri;

    /**
     * Costruisce un nuovo PoggioClientJersey.
     * Inizializza l'URI base e crea l'istanza del client Jersey,
     * registrando la feature {@link MultiPartFeature} necessaria per
     * consentire l'invio di richieste multipart/form-data (upload di file).
     */
    public PoggioClientJersey() {
        this.serverBaseUri = SERVER_URI;
        this.client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class) // Abilita il supporto per upload multipart
                .build(); // Costruisce l'istanza del client
    }

    /**
     * Chiude il client Jersey, rilasciando le risorse sottostanti
     * (es. connection pool, thread). È importante chiamare questo metodo
     * quando il client non è più necessario. Ignora eventuali eccezioni
     * che potrebbero verificarsi durante la chiusura.
     */
    public void close() {
        if (this.client != null) {
            try {
                this.client.close();
            } catch (Exception e) {
                // Ignora errori durante la chiusura, pratica comune ma potrebbe nascondere problemi.
            }
        }
    }

    /**
     * Invia una richiesta POST per aggiungere un nuovo utente al server.
     * Endpoint: POST /players/{username}
     *
     * @param username Lo username del giocatore da aggiungere.
     * @return {@link ApiClientResult} indicante l'esito:
     * - {@code SUCCESS_CREATED} (201) se l'utente è stato creato.
     * - {@code USER_ALREADY_EXISTS} (409) se l'utente esiste già.
     * - {@code INVALID_INPUT_CLIENT} se lo username è nullo o vuoto.
     * - {@code CONNECTION_ERROR} per errori di rete/protocollo.
     * - {@code UNAUTHORIZED} (401) se la API key non è valida.
     * - {@code SERVER_ERROR} (5xx) per errori lato server.
     * - {@code UNKNOWN_ERROR} per altri errori imprevisti.
     */
    public ApiClientResult addUser(String username) {
        // Validazione input lato client
        if (username == null || username.trim().isEmpty()) {
             return ApiClientResult.INVALID_INPUT_CLIENT;
        }
        // Costruisce il WebTarget per l'endpoint specifico
        WebTarget target = client.target(serverBaseUri).path("players").path(username);
        Response response = null;
        try {
            // Esegue la richiesta POST, inviando l'header API Key e aspettandosi JSON
            response = target.request(MediaType.APPLICATION_JSON)
                           .header(HEADER_API_KEY, SHARED_SECRET) // Aggiunge header di autenticazione
                           .post(Entity.json(null)); // Esegue POST senza corpo (o con corpo JSON nullo)

            // Delega la gestione dello status code all'helper
            // Codici attesi: 201 (Created), 409 (Conflict)
            return handleResponseStatus(response, "addUser", username, 201, 409);
        } catch (ProcessingException | WebApplicationException e) {
             // Errori di connessione, I/O, o protocollo durante la richiesta/risposta
             return ApiClientResult.CONNECTION_ERROR;
        } catch (Exception e) {
             // Altri errori imprevisti
             return ApiClientResult.UNKNOWN_ERROR;
        } finally {
             // Assicura che la risposta sia chiusa per rilasciare le risorse (es. connessione)
             if (response != null) response.close();
        }
    }

    /**
     * Invia una richiesta GET per verificare l'esistenza di un utente sul server.
     * Endpoint: GET /players/{username}
     *
     * @param username Lo username del giocatore da verificare.
     * @return {@link ApiClientResult} indicante l'esito:
     * - {@code SUCCESS_OK} (200) se l'utente esiste.
     * - {@code USER_NOT_FOUND} (404) se l'utente non esiste.
     * - {@code INVALID_INPUT_CLIENT} se lo username è nullo o vuoto.
     * - {@code CONNECTION_ERROR} per errori di rete/protocollo.
     * - {@code UNAUTHORIZED} (401).
     * - {@code SERVER_ERROR} (5xx).
     * - {@code UNKNOWN_ERROR}.
     */
    public ApiClientResult checkUserExists(String username) {
        if (username == null || username.trim().isEmpty()) {
             return ApiClientResult.INVALID_INPUT_CLIENT;
        }
        WebTarget target = client.target(serverBaseUri).path("players").path(username);
        Response response = null;
        try {
            // Esegue la richiesta GET
            response = target.request(MediaType.APPLICATION_JSON)
                           .header(HEADER_API_KEY, SHARED_SECRET)
                           .get();

            // Delega la gestione dello status code
            // Codici attesi: 200 (OK), 404 (Not Found)
            return handleResponseStatus(response, "checkUserExists", username, 200, 404);
        } catch (ProcessingException | WebApplicationException e) {
             return ApiClientResult.CONNECTION_ERROR;
        } catch (Exception e) {
             return ApiClientResult.UNKNOWN_ERROR;
        } finally {
             if (response != null) response.close();
        }
    }

    /**
     * Invia una richiesta PUT per registrare una vittoria e caricare il file di log associato.
     * Utilizza multipart/form-data per inviare dati testuali e file.
     * Endpoint: PUT /players/{username}/victory
     *
     * @param username Lo username del giocatore.
     * @param data La data della vittoria (formato stringa, es. YYYY-MM-DD).
     * @param ora L'ora della vittoria (formato stringa, es. HH:mm:ss).
     * @param durataMs La durata della partita in millisecondi.
     * @param logFilePath Il percorso locale del file di log da caricare.
     * @return {@link ApiClientResult} indicante l'esito:
     * - {@code SUCCESS_OK} (200) se la vittoria e il log sono stati registrati/aggiornati.
     * - {@code USER_NOT_FOUND} (404) se l'utente non esiste.
     * - {@code LOG_ALREADY_EXISTS} (409) se un log per quella vittoria esiste già (server potrebbe impedire sovrascrittura).
     * - {@code INVALID_INPUT_CLIENT} per input non validi.
     * - {@code FILE_ERROR} se il file di log locale non esiste o è illeggibile, o errore IO durante la creazione del multipart.
     * - {@code CONNECTION_ERROR}, {@code UNAUTHORIZED}, {@code SERVER_ERROR}, {@code UNKNOWN_ERROR}.
     */
     public ApiClientResult recordVictoryWithLog(String username, String data, String ora, long durataMs, String logFilePath) {
         // Validazione input multipli
         if (username == null || username.trim().isEmpty() || data == null || ora == null || logFilePath == null || durataMs < 0) {
              return ApiClientResult.INVALID_INPUT_CLIENT;
         }
         WebTarget target = client.target(serverBaseUri).path("players").path(username).path("victory");

         // Verifica esistenza e tipo del file di log locale
         File logFile = new File(logFilePath);
         if (!logFile.exists() || !logFile.isFile()) {
              return ApiClientResult.FILE_ERROR;
         }

         // Usa try-with-resources per assicurare la chiusura del FormDataMultiPart
         try (FormDataMultiPart multipart = new FormDataMultiPart()) {
              // Aggiunge i campi testuali al corpo multipart
              multipart.field("data", data);
              multipart.field("ora", ora);
              multipart.field("durataMs", String.valueOf(durataMs));

              // Crea la parte del file per il corpo multipart
              // "logFile" è il nome del campo atteso dal server per il file
              FileDataBodyPart filePart = new FileDataBodyPart("logFile", logFile, MediaType.APPLICATION_OCTET_STREAM_TYPE);
              multipart.bodyPart(filePart); // Aggiunge la parte file al multipart

              Response response = null;
              try {
                   // Esegue la richiesta PUT inviando l'entità multipart
                   response = target.request(MediaType.APPLICATION_JSON) // Tipo di risposta atteso dal server
                                  .header(HEADER_API_KEY, SHARED_SECRET)
                                  .put(Entity.entity(multipart, multipart.getMediaType())); // Invia il corpo multipart con il suo content type

                   // Delega la gestione dello status code
                   // Codici attesi: 200 (OK), 404 (User Not Found), 409 (Log Already Exists?)
                   return handleResponseStatus(response, "recordVictory", username, 200, 404, 409);
              } finally {
                   if (response != null) response.close();
                   // multipart.close() è gestito dal try-with-resources
              }
         } catch (ProcessingException | WebApplicationException e) {
              return ApiClientResult.CONNECTION_ERROR;
         } catch (IOException e) {
             // Errore durante la creazione/gestione del FormDataMultiPart o FileDataBodyPart
             return ApiClientResult.FILE_ERROR;
         } catch (Exception e) {
             return ApiClientResult.UNKNOWN_ERROR;
         }
     }

    /**
     * Metodo helper centralizzato per mappare lo status code di una {@link Response} HTTP
     * a un valore dell'enum {@link ApiClientResult}.
     * Gestisce i codici di successo specifici, i codici di fallimento attesi per l'operazione,
     * e i codici di errore HTTP comuni (400, 401, 5xx).
     * Tenta di leggere il corpo della risposta in caso di errori 4xx specifici per distinguere
     * ulteriormente la causa (es. 409 User Exists vs 409 Log Exists).
     *
     * @param response La risposta HTTP ricevuta (può essere null se si è verificata un'eccezione prima della chiamata).
     * @param operation Nome dell'operazione che ha generato la risposta (usato per logging/debug).
     * @param username Username coinvolto nell'operazione (usato per logging/debug).
     * @param successStatus Il codice HTTP che indica successo per questa specifica operazione (es. 200, 201).
     * @param failureStatus Un array varargs di codici HTTP che rappresentano fallimenti "previsti" o specifici
     * per questa operazione (es. 404 Not Found, 409 Conflict).
     * @return L' {@link ApiClientResult} corrispondente allo status code della risposta.
     */
    private ApiClientResult handleResponseStatus(Response response, String operation, String username, int successStatus, int... failureStatus) {
        if (response == null) {
            System.err.println("Errore Interno Client [" + operation + " " + username + "]: Risposta nulla ricevuta da handleResponseStatus.");
            // Potrebbe essere CONNECTION_ERROR se l'eccezione è avvenuta prima, ma UNKNOWN è più generico
            return ApiClientResult.UNKNOWN_ERROR;
        }

        int statusCode = response.getStatus();

        // --- Gestione Successo ---
        if (statusCode == successStatus) {
             // Mappa 201 a CREATED, tutti gli altri successi specificati a OK
             return (successStatus == 201) ? ApiClientResult.SUCCESS_CREATED : ApiClientResult.SUCCESS_OK;
        }

        // --- Gestione Fallimenti Specifici Attesi ---
        for (int failCode : failureStatus) {
             if (statusCode == failCode) {
                 // Legge il corpo dell'errore (se presente) per analisi più approfondita
                 String errorBody = safeReadEntity(response);

                 // Mappa i codici specifici all'enum ApiClientResult
                 switch (statusCode) {
                     case 404 -> {
                         // Not Found
                         // Qui potrebbe essere USER_NOT_FOUND o LOG_NOT_FOUND a seconda dell'operazione.
                         // Assumiamo USER_NOT_FOUND come default per 404 se non specificato diversamente.
                         return ApiClientResult.USER_NOT_FOUND;
                     }
                     case 409 -> {
                         // Conflict
                         // Tenta di distinguere il tipo di conflitto basandosi sul contenuto del corpo errore
                         // ATTENZIONE: Questo approccio è FRAGILE, dipende dal formato del messaggio del server.
                         if (errorBody != null && errorBody.contains("USER_ALREADY_EXISTS")) {
                             return ApiClientResult.USER_ALREADY_EXISTS;
                         } else if (errorBody != null && errorBody.contains("LOG_ALREADY_EXISTS")) {
                             return ApiClientResult.LOG_ALREADY_EXISTS;
                         } else {
                             // Conflitto 409 non riconosciuto, trattato come errore generico
                             return ApiClientResult.UNKNOWN_ERROR;
                         }
                     }
                     default -> {
                         // Se un codice passato in failureStatus non ha un 'case' specifico qui, errore.
                         return ApiClientResult.UNKNOWN_ERROR;
                     }
                 }
                 // Aggiungere altri 'case' qui se si gestiscono altri failureStatus specifici (es. 403)
                              }
        } // Fine loop failureStatus

        // --- Gestione Errori Comuni Generici (non specificati in failureStatus) ---
        String genericErrorBody = safeReadEntity(response); // Legge body anche per errori generici (utile per debug)
        System.err.println("Errore Generico [" + operation + " " + username + "]: Status=" + statusCode + ", Body=" + genericErrorBody);
        switch (statusCode) {
            case 400 -> {
                // Bad Request
                return ApiClientResult.INVALID_INPUT_SERVER; // Input errato secondo il server
            }
            case 401 -> {
                // Unauthorized
                return ApiClientResult.UNAUTHORIZED; // Chiave API errata o mancante
            }
            default -> {
                if (statusCode >= 500) { // Server Error (5xx)
                    return ApiClientResult.SERVER_ERROR;
                } else {
                    // Tutti gli altri codici 4xx non gestiti o altri codici imprevisti
                    return ApiClientResult.UNKNOWN_ERROR;
                }
            }
        }
        // Aggiungere altri codici comuni se necessario (es. 403 Forbidden -> UNAUTHORIZED o un nuovo enum?)
            }

    /**
     * Invia una richiesta DELETE per eliminare un utente dal server.
     * Endpoint: DELETE /players/{username}
     *
     * @param username L'utente da eliminare.
     * @return {@link ApiClientResult} indicante l'esito:
     * - {@code SUCCESS_OK} (200) se l'utente è stato eliminato.
     * - {@code USER_NOT_FOUND} (404) se l'utente non esisteva.
     * - {@code INVALID_INPUT_CLIENT} se lo username è nullo o vuoto.
     * - {@code CONNECTION_ERROR}, {@code UNAUTHORIZED}, {@code SERVER_ERROR}, {@code UNKNOWN_ERROR}.
     */
    public ApiClientResult deletePlayer(String username) {
        if (username == null || username.trim().isEmpty()) {
             return ApiClientResult.INVALID_INPUT_CLIENT;
        }
        WebTarget target = client.target(serverBaseUri).path("players").path(username);
        Response response = null;
        try {
            // Esegue la richiesta DELETE
            response = target.request(MediaType.APPLICATION_JSON) // Tipo di risposta atteso (anche se spesso è vuota per DELETE)
                           .header(HEADER_API_KEY, SHARED_SECRET)
                           .delete();

            // Delega la gestione dello status code
            // Codici attesi: 200 (OK), 404 (Not Found)
            return handleResponseStatus(response, "deletePlayer", username, 200, 404);
        } catch (ProcessingException | WebApplicationException e) {
             return ApiClientResult.CONNECTION_ERROR;
        } catch (Exception e) {
             return ApiClientResult.UNKNOWN_ERROR;
        } finally {
             if (response != null) response.close();
        }
    }

    /**
     * Recupera la classifica dei giocatori (lista di {@link RankingEntryDTO}) dal server.
     * Endpoint: GET /players/ranking
     * In caso di errore (connessione, HTTP status non 200, errore parsing JSON),
     * restituisce una lista vuota invece di sollevare eccezioni o restituire null.
     *
     * @return Una {@code List<RankingEntryDTO>} contenente la classifica (ordinata dal server),
     * oppure una lista vuota ({@code Collections.emptyList()}) se la classifica è vuota
     * o si verifica un qualsiasi errore durante il recupero o la deserializzazione.
     */
     public List<RankingEntryDTO> getRanking() {
         WebTarget target = client.target(serverBaseUri).path("players").path("ranking");
         Response response = null;

         try {
              // Esegue la richiesta GET, aspettandosi una risposta JSON
              response = target.request(MediaType.APPLICATION_JSON)
                             .header(HEADER_API_KEY, SHARED_SECRET)
                             .get();

              int statusCode = response.getStatus();

              if (statusCode == Response.Status.OK.getStatusCode()) { // 200 OK
                   // Tenta di deserializzare il corpo della risposta in una List<RankingEntryDTO>
                   try {
                        // Usa GenericType per gestire la deserializzazione di un tipo generico List<T>
                        List<RankingEntryDTO> ranking = response.readEntity(new GenericType<List<RankingEntryDTO>>() {});
                        // Se ranking è null (alcune implementazioni potrebbero farlo), restituisci comunque lista vuota
                        return ranking != null ? ranking : Collections.emptyList();
                   } catch (Exception e) {
                        // Errore durante la deserializzazione del JSON (es. formato non valido)
                        return Collections.emptyList(); // Restituisce lista vuota
                   }
              } else {
                   // Errore HTTP diverso da 200 OK (es. 401, 404, 500)
                   String errorBody = safeReadEntity(response); // Legge corpo errore per debug
                   // Qui non usiamo handleResponseStatus perché vogliamo sempre restituire List, non ApiClientResult
                   return Collections.emptyList(); // Restituisce lista vuota
              }
         } catch (ProcessingException | WebApplicationException e) {
              // Errore di connessione, I/O o protocollo
              return Collections.emptyList(); // Restituisce lista vuota
         } catch (Exception e) {
              // Altri errori imprevisti
              return Collections.emptyList(); // Restituisce lista vuota
         } finally {
              // Assicura chiusura della risposta
              if (response != null) {
                   response.close();
              }
              // Non chiudiamo il client qui, è riutilizzabile
         }
     }

    /**
     * Metodo helper per leggere in modo sicuro il corpo (entity) di una {@link Response} come String.
     * Utile per ottenere messaggi di errore dal server per il logging o l'analisi.
     * Gestisce eccezioni durante la lettura e limita la lunghezza della stringa restituita.
     *
     * @param response La risposta HTTP da cui leggere il corpo.
     * @return Il corpo della risposta come String (troncato se troppo lungo),
     * o un messaggio di errore/placeholder se la lettura fallisce o non c'è corpo.
     */
    private String safeReadEntity(Response response) {
        try {
            if (response != null && response.hasEntity()) {
                // Legge l'entità come stringa. Importante: questo consuma lo stream!
                // Non può essere chiamato più volte sulla stessa risposta senza buffering.
                String body = response.readEntity(String.class);
                // Tronca stringhe molto lunghe per evitare log eccessivi
                final int MAX_LEN = 500;
                return body.length() > MAX_LEN ? body.substring(0, MAX_LEN) + "..." : body;
            }
        } catch (Exception e) {
             // Ignora eccezioni durante la lettura (es. stream già chiuso, formato non compatibile)
             // Potrebbe essere utile loggare questa eccezione a livello DEBUG.
             // System.err.println("Debug: Errore lettura entity response: " + e.getMessage());
        }
        return "(Nessun corpo o errore lettura)"; // Placeholder
    }

     // Directory usata dal metodo main per creare/salvare file di log temporanei per test
     private static final String LOG_TEST_DIRECTORY = "test_logs_client"; // Nome diverso da quello nel main originale per chiarezza

     /**
      * Scarica il file di log associato a una vittoria di un utente dal server
      * e lo salva nel percorso specificato dalla configurazione {@link ResourceLoader}.
      * Endpoint: GET /players/{username}/log
      *
      * @param username L'utente di cui scaricare il log.
      * @return {@link ApiClientResult} indicante l'esito:
      * - {@code SUCCESS_OK} (200) se il file è stato scaricato e salvato correttamente.
      * - {@code USER_NOT_FOUND} (404) se l'utente non esiste.
      * - {@code LOG_NOT_FOUND} (potrebbe essere un 404 specifico dal server, attualmente mappato a USER_NOT_FOUND dall'helper).
      * - {@code FILE_ERROR} se si verifica un errore durante la scrittura del file locale.
      * - {@code INVALID_INPUT_CLIENT} per input non validi.
      * - {@code CONNECTION_ERROR}, {@code UNAUTHORIZED}, {@code SERVER_ERROR}, {@code UNKNOWN_ERROR}.
      */
     public ApiClientResult downloadLogFile(String username) {
         // Ottiene il percorso di destinazione dalla classe ResourceLoader
         String localSavePath = ResourceLoader.LOGS_DW_DIRECTORY.resolve(username + "_log.txt").toString(); // Aggiunge nome file

         // Validazione input
         if (username == null || username.trim().isEmpty() || localSavePath == null || localSavePath.trim().isEmpty()) {
              return ApiClientResult.INVALID_INPUT_CLIENT;
         }

         WebTarget target = client.target(serverBaseUri).path("players").path(username).path("log");
         Response response = null;

         try {
              // Esegue la richiesta GET, specificando che accettiamo un flusso di byte (il file)
              response = target.request(MediaType.APPLICATION_OCTET_STREAM)
                             .header(HEADER_API_KEY, SHARED_SECRET)
                             .get();

              int statusCode = response.getStatus();

              if (statusCode == Response.Status.OK.getStatusCode()) { // 200 OK -> File trovato sul server
                   // Tenta di leggere l'InputStream dalla risposta e salvarlo su file
                   // Usa try-with-resources per assicurare la chiusura dell'InputStream
                   try (InputStream inputStream = response.readEntity(InputStream.class)) {
                        Path targetPath = Paths.get(localSavePath);
                        // Assicura che la directory genitore esista, creandola se necessario
                        Path parentDir = targetPath.getParent();
                        if (parentDir != null) {
                             Files.createDirectories(parentDir);
                        }
                        // Copia i dati dallo stream di input al file di destinazione
                        // REPLACE_EXISTING sovrascrive il file locale se esiste già
                        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        return ApiClientResult.SUCCESS_OK; // Successo!
                   } catch (IOException e) {
                        // Errore durante la scrittura del file sul disco locale
                        return ApiClientResult.FILE_ERROR;
                   } catch (Exception e) {
                       // Errore durante la lettura dello stream dalla risposta (diverso da IOException sulla scrittura)
                       // Potrebbe essere un problema di connessione interrotta o formato stream non valido.
                        return ApiClientResult.CONNECTION_ERROR; // Mappato a errore connessione/stream
                   }

              } else {
                   // Il server ha risposto con un codice di errore (es. 404, 401, 500)
                   // Deleghiamo la mappatura all'helper standard.
                   // handleResponseStatus leggerà il corpo per noi se necessario.
                   // Nota: se il server risponde 404, l'helper lo mapperà a USER_NOT_FOUND.
                   // Non distinguiamo qui tra "utente non trovato" e "log non trovato per utente esistente".
                   return handleResponseStatus(response, "downloadLogFile", username, 200, 404);
              }

         } catch (ProcessingException | WebApplicationException e) {
              // Errore di connessione, I/O o protocollo durante la richiesta iniziale
              return ApiClientResult.CONNECTION_ERROR;
         } catch (Exception e) {
             // Altri errori imprevisti
              return ApiClientResult.UNKNOWN_ERROR;
         } finally {
              // Assicura chiusura della risposta
              if (response != null) response.close();
         }
     }

     /**
      * Metodo main utilizzato per testare le funzionalità del client,
      * in particolare la registrazione di vittorie multiple per diversi utenti.
      * Crea file di log fittizi, interagisce con il server API (aggiungendo utenti
      * e registrando vittorie), recupera la classifica finale e pulisce i file creati.
      *
      * ATTENZIONE: Questo metodo esegue operazioni che MODIFICANO i dati sul server
      * (http://localhost:8080/). Usare con cautela e solo in ambiente di test.
      *
      * @param args Argomenti da linea di comando (non utilizzati).
      */
     public static void main(String[] args) {
         System.out.println("--- Avvio Test Client PoggioAdventure (Jersey) ---");
         System.out.println("!!! ATTENZIONE: Modificherà dati su " + SERVER_URI + " !!!");

         PoggioClientJersey testClient = new PoggioClientJersey(); // Crea istanza client per il test

         // ---- Configurazione Dati di Test ----
         List<String> testUsers = Arrays.asList(
             "TesterAlfa", "TesterBeta", "TesterGamma", "TesterDelta", "TesterEpsilon", "TesterZeta"
         );
         // Dati variabili per simulare diverse partite: {username -> {durataMs, righeLog}}
         // Usati per generare punteggi differenti tramite la logica del server.
         Map<String, Map<String, Integer>> testData = new HashMap<>();
         testData.put("TesterAlfa", Map.of("durataMs", 50000, "righeLog", 50));    // Score Alto
         testData.put("TesterBeta", Map.of("durataMs", 120000, "righeLog", 200));   // Score Medio
         testData.put("TesterGamma", Map.of("durataMs", 300000, "righeLog", 100));  // Score Basso (tempo alto)
         testData.put("TesterDelta", Map.of("durataMs", 90000, "righeLog", 1000));  // Score Penalizzato (righe alte)
         testData.put("TesterEpsilon", Map.of("durataMs", 10000, "righeLog", 10));   // Score Molto Alto (tempo basso)
         testData.put("TesterZeta", Map.of("durataMs", 60000, "righeLog", 150));    // Score Medio-Alto

         // Mappa per tenere traccia dei percorsi dei file di log creati
         Map<String, String> logFilePaths = new HashMap<>();
         boolean setupSuccess = true; // Flag per controllare l'esito delle fasi preliminari

         try {
             // --- FASE 1: Creazione Directory e File di Log Fittizi ---
             System.out.println("\n[FASE 1] Creazione file di log fittizi in ./" + LOG_TEST_DIRECTORY);
             Path logDir = Paths.get(LOG_TEST_DIRECTORY);
             Files.createDirectories(logDir); // Crea la directory se non esiste (idempotente)

             for (String user : testUsers) {
                 int lineCount = testData.get(user).get("righeLog");
                 String fileName = "log_" + user + ".txt"; // Nome file univoco per utente
                 Path filePath = logDir.resolve(fileName);
                 try {
                     // Usa l'helper per creare il file con contenuto fittizio
                     createDummyLogFile(filePath, lineCount);
                     logFilePaths.put(user, filePath.toString()); // Memorizza il percorso
                     System.out.println("  - Creato: " + filePath + " (" + lineCount + " righe)");
                 } catch (IOException e) {
                     System.err.println("ERRORE FASE 1: Impossibile creare file log per " + user + ": " + e.getMessage());
                     setupSuccess = false;
                     break; // Interrompe la fase 1 se un file non può essere creato
                 }
             }

             if (!setupSuccess) throw new RuntimeException("Setup fallito: impossibile creare tutti i file di log.");

             // --- FASE 2: Assicura Esistenza Utenti sul Server ---
             System.out.println("\n[FASE 2] Verifica/Creazione utenti sul server...");
             for (String user : testUsers) {
                 System.out.print("  - Utente '" + user + "': ");
                 ApiClientResult checkResult = testClient.checkUserExists(user); // Verifica se l'utente esiste

                 // Gestisce i possibili risultati della verifica
                 if (null == checkResult) { // Controllo robustezza (non dovrebbe accadere)
                    System.out.println("ERRORE verifica (risultato nullo)");
                    setupSuccess = false;
                 } else switch (checkResult) {
                     case USER_NOT_FOUND -> {
                         // Utente non trovato
                         System.out.print("non trovato, aggiungo... ");
                         ApiClientResult addResult = testClient.addUser(user); // Tenta di aggiungerlo
                         if (addResult == ApiClientResult.SUCCESS_CREATED) {
                             System.out.println("OK (Aggiunto)");
                         } else {
                             System.out.println("ERRORE aggiunta (" + addResult + ")"); // Stampa l'errore specifico
                             setupSuccess = false;
                         }
                     }
                     case SUCCESS_OK -> // Utente già esistente
                         System.out.println("OK (Già esistente)");
                     // In un test reale, potremmo voler eliminare e ricreare l'utente
                     // o resettare i suoi dati prima di registrare nuove vittorie.
                     // Qui, per semplicità, lo lasciamo esistere.
                     default -> {
                         // Altro risultato inatteso dalla verifica (es. errore connessione)
                         System.out.println("ERRORE verifica (" + checkResult + ")");
                         setupSuccess = false;
                     }
                 }
                 if (!setupSuccess) break; // Interrompe la fase 2 se c'è un errore con un utente
             }

             if (!setupSuccess) throw new RuntimeException("Setup fallito: impossibile preparare gli utenti sul server.");

             // --- FASE 3: Registra Vittorie (con file di log) ---
             System.out.println("\n[FASE 3] Registrazione vittorie...");
             LocalDate today = LocalDate.now(); // Data corrente
             LocalTime now = LocalTime.now(); // Ora corrente

             for (String user : testUsers) {
                 System.out.print("  - Registrazione vittoria per '" + user + "': ");
                 String dataStr = today.format(DateTimeFormatter.ISO_DATE); // Formato YYYY-MM-DD
                 // Scaliamo l'ora indietro per ogni utente per avere timestamp diversi
                 String oraStr = now.minusMinutes(testUsers.indexOf(user) * 5L).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                 long durata = testData.get(user).get("durataMs"); // Durata specifica per l'utente
                 String logPath = logFilePaths.get(user); // Percorso del log fittizio

                 // Chiama il metodo del client per registrare la vittoria
                 ApiClientResult victoryResult = testClient.recordVictoryWithLog(user, dataStr, oraStr, durata, logPath);

                 System.out.println(victoryResult); // Stampa l'esito (es. SUCCESS_OK, LOG_ALREADY_EXISTS)

                 if (victoryResult != ApiClientResult.SUCCESS_OK) {
                     // Logga un errore se la registrazione non ha successo (es. utente non trovato, log già esistente)
                     System.err.println("    -> ATTENZIONE: Registrazione fallita o non ottimale per " + user + " (" + victoryResult + ")");
                     // Non interrompiamo il loop per fallimenti singoli, continuiamo con gli altri utenti.
                 }
                  // Piccola pausa per evitare potenziali race condition o timestamp troppo vicini
                  // sul server, se la logica server dipende da essi in modo critico.
                 try { Thread.sleep(50); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
             }

             // --- FASE 4: Verifica Classifica Finale ---
             System.out.println("\n[FASE 4] Recupero classifica finale...");
             List<RankingEntryDTO> ranking = testClient.getRanking(); // Recupera la classifica aggiornata

             if (ranking == null) { // Controllo robustezza (getRanking dovrebbe restituire lista vuota, non null)
                  System.err.println("ERRORE FASE 4: Impossibile recuperare la classifica (risultato nullo).");
             } else if (ranking.isEmpty()) {
                  System.out.println("  -> Classifica vuota o errore durante il recupero.");
             } else {
                  // Stampa la classifica formattata
                  System.out.println("  -> Classifica attuale:");
                  System.out.printf("     %-4s %-20s %-10s %-12s %-10s%n", "Pos", "Username", "Punteggio", "Data", "Ora");
                  System.out.println("     -------------------------------------------------------------");
                  int pos = 1;
                  for (RankingEntryDTO entry : ranking) {
                        String date = entry.getData() != null ? entry.getData().toString() : "N/D";
                        String time = entry.getOra() != null ? entry.getOra().toString() : "N/D";
                        System.out.printf("     %-4d %-20s %-10d %-12s %-10s%n",
                                          pos++,
                                          entry.getUsername(),
                                          entry.getPunteggio() != null ? entry.getPunteggio() : -1,
                                          date,
                                          time);
                  }
                  System.out.println("     -------------------------------------------------------------");
             }

         } catch (IOException | RuntimeException e) {
             // Cattura eccezioni critiche avvenute durante le fasi del test
             System.err.println("\nERRORE CRITICO DURANTE IL TEST: " + e.getMessage());
             e.printStackTrace(System.err);
         } finally {
             // --- FASE 5: Pulizia Risorse Locali e Client ---
             System.out.println("\n[FASE 5] Pulizia risorse...");

             // Elimina i file di log fittizi creati localmente
             System.out.println("  - Eliminazione file di log fittizi:");
             for (String path : logFilePaths.values()) {
                 try {
                     if (Files.deleteIfExists(Paths.get(path))) {
                        //System.out.println("    - Eliminato: " + path); // Log opzionale
                     }
                 } catch (IOException e) {
                     System.err.println("    ERRORE: Impossibile eliminare file " + path + ": " + e.getMessage());
                 }
             }
             // Prova ad eliminare la directory (fallirà se non è vuota o per permessi)
             try {
                 Files.deleteIfExists(Paths.get(LOG_TEST_DIRECTORY));
                 System.out.println("  - Eliminata directory: " + LOG_TEST_DIRECTORY);
             } catch (IOException e) {
                 System.err.println("  - ATTENZIONE: Impossibile eliminare directory " + LOG_TEST_DIRECTORY + " (potrebbe non essere vuota o mancano permessi): " + e.getMessage());
             }

             // Chiude il client Jersey per rilasciare le sue risorse
             if (testClient != null) {
                 testClient.close();
                 System.out.println("  - Client Jersey chiuso.");
             }
             System.out.println("\n--- Test Terminato ---");
         }
     }

     /**
      * Metodo helper per creare un file di testo fittizio con un numero specificato di righe.
      * Utile per generare i file di log usati nel metodo main di test.
      * Sovrascrive il file se esiste già.
      *
      * @param filePath Il {@link Path} del file da creare/sovrascrivere.
      * @param lineCount Il numero di righe di testo fittizio da scrivere nel file.
      * @throws IOException Se si verifica un errore durante la scrittura del file.
      */
     private static void createDummyLogFile(Path filePath, int lineCount) throws IOException {
         List<String> lines = new ArrayList<>(lineCount);
         // Genera il contenuto fittizio
         for (int i = 1; i <= lineCount; i++) {
             lines.add("Riga di log fittizia numero " + i + " per " + filePath.getFileName());
         }
         // Scrive le righe nel file specificato usando UTF-8.
         // Opzioni:
         // CREATE: Crea il file se non esiste.
         // WRITE: Apre il file per la scrittura.
         // TRUNCATE_EXISTING: Se il file esiste, lo svuota prima di scrivere.
         Files.write(filePath, lines, StandardCharsets.UTF_8,
                     StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
     }
}