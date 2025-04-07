package com.mycompany.poggioserver.resources;

import com.mycompany.poggioserver.db.PlayerDAO;
import com.mycompany.poggioserver.db.PlayerDAOImpl;
import com.mycompany.poggioserver.model.PlayerRecord;
import java.text.NumberFormat;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Principio OOP: Interfaccia utente (web) separata dalla logica di business/dati
/**
 *
 * @author Strix89
 */
@Path("/players") // Percorso base per questa risorsa
public class PlayerResource {

    private static final Logger logger = LoggerFactory.getLogger(PlayerResource.class);
    // Istanzia direttamente il DAO (senza DI avanzato)
    private final PlayerDAO playerDAO = new PlayerDAOImpl();
    private static final String UPLOAD_LOG_DIRECTORY = Paths.get("resources", "uploaded_logs").toAbsolutePath().toString();
    
    // Endpoint per aggiungere un nuovo giocatore (solo username)
    // POST /players/{username}
    @POST
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON) // Produce una risposta JSON (anche se vuota o di errore)
    public Response addPlayer(@PathParam("username") String username) {
        logger.info("Ricevuta richiesta POST /players/{}", username);
        if (username == null || username.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"Username mancante nel path\"}")
                .build();
        }
        try {
            playerDAO.addPlayer(username);
            // Creato con successo, restituisce 201 Created
            // Potrebbe restituire l'URI della risorsa creata nell'header Location
            return Response.status(Response.Status.CREATED)
                .entity("{\"message\":\"Giocatore " + username + " creato\"}")
                .build();
        } catch (SQLException e) {
            // Se è un errore di duplicato (violazione constraint)
            if (e.getMessage() != null && e.getMessage().contains("esiste già")) {
                logger.warn("Tentativo di creare utente già esistente: {}", username);
                return Response.status(Response.Status.CONFLICT) // 409 Conflict
                    .entity("{\"error\":\"Username '" + username + "' esiste già\"}")
                    .build();
            }
            // Altri errori DB
            logger.error("Errore DB durante aggiunta giocatore {}: {}", username, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Errore interno del server: " + e.getMessage() + "\"}")
                .build();
        } catch (IllegalArgumentException e) {
            logger.warn("Input non valido per addPlayer: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }
    
    // Endpoint per registrare una vittoria PER UN GIOCATORE ESISTENTE (ora con upload log)
    @PUT
    @Path("/{username}/victory")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response recordVictory(
            @PathParam("username") String username,
            @FormDataParam("data") String dataString,
            @FormDataParam("ora") String oraString,
            @FormDataParam("durataMs") String durataMsString,
            @FormDataParam("logFile") InputStream logFileInputStream,
            @FormDataParam("logFile") FormDataContentDisposition fileDisposition
    ) {
        logger.info("Ricevuta richiesta PUT /players/{}/victory (multipart)", username);

        // --- Validazione Input Base ---
        if (username == null || username.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Username mancante nel path\"}").type(MediaType.APPLICATION_JSON).build();
        }
        if (dataString == null || oraString == null || durataMsString == null) { // AGGIUNTO CONTROLLO durataMsString
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Parametri 'data', 'ora' e 'durataMs' mancanti nel form\"}").type(MediaType.APPLICATION_JSON).build();
        }
        if (logFileInputStream == null || fileDisposition == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Parametro file 'logFile' mancante nel form\"}").type(MediaType.APPLICATION_JSON).build();
        }

        String serverLogPath = null; // Path dove verrà salvato il file sul server
        boolean fileSaved = false; // Flag per sapere se il file è stato salvato
        Long durataMs = null;
        
        try {
            // --- NUOVO CONTROLLO: Verifica esistenza utente e log esistente ---
            PlayerRecord existingPlayer = playerDAO.getPlayer(username);

            if (existingPlayer == null) {
                // Utente non trovato, non possiamo registrare la vittoria
                logger.warn("Tentativo di registrare vittoria per utente non esistente: {}", username);
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("{\"error\":\"Giocatore '" + username + "' non trovato\"}")
                               .type(MediaType.APPLICATION_JSON)
                               .build();
            }

            // Controlla se esiste già un percorso log per questo utente
            if (existingPlayer.getPercorsoFileLog() != null && !existingPlayer.getPercorsoFileLog().trim().isEmpty()) {
                logger.warn("Tentativo di caricare un log per l'utente '{}' che ha già vinto", username);
                return Response.status(Response.Status.CONFLICT) // 409 Conflict è appropriato qui
                               .entity("{\"error\":\"L'utente '" + username + "' ha già un file di log associato ("+ existingPlayer.getPercorsoFileLog() +"). Impossibile sovrascrivere.\", \"existingLogPath\":\""+existingPlayer.getPercorsoFileLog()+"\"}")
                               .type(MediaType.APPLICATION_JSON)
                               .build();
            }
            // --- Parsing Data e Ora ---
            Date sqlDate = parseSqlDate(dataString);
            Time sqlTime = parseSqlTime(oraString);
            
            try {
                 durataMs = Long.valueOf(durataMsString);
                 if (durataMs < 0) { // Aggiungi un controllo se la durata non può essere negativa
                      throw new NumberFormatException("La durata non può essere negativa.");
                 }
            } catch (NumberFormatException e) {
                 logger.warn("Formato durata non valido per {}: Durata='{}', Errore={}", username, durataMsString, e.getMessage());
                 return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Formato durataMs non valido (deve essere un numero intero non negativo): " + e.getMessage() + "\"}").type(MediaType.APPLICATION_JSON).build();
            }

            // --- Salvataggio File di Log ---
            java.nio.file.Path uploadDir = Paths.get(UPLOAD_LOG_DIRECTORY);
            Files.createDirectories(uploadDir);

            String originalFileName = fileDisposition.getFileName();
            String safeOriginalName = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
            String uniqueFileName = username + "_" + System.currentTimeMillis() + "_" + safeOriginalName;
            java.nio.file.Path targetPath = uploadDir.resolve(uniqueFileName);
            serverLogPath = targetPath.toString();

            Files.copy(logFileInputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            fileSaved = true; // Imposta il flag a true dopo il salvataggio riuscito
            logger.info("File di log '{}' salvato come '{}' per utente '{}'", originalFileName, serverLogPath, username);


            // --- Aggiornamento Database ---
            boolean updated = playerDAO.recordVictory(username, sqlDate, sqlTime, serverLogPath, durataMs);

            if (updated) {
                // POTREMMO AGGIUNGERE durataMs ALLA RISPOSTA SE UTILE
                 logger.info("Vittoria e log registrati per {}", username);
                 return Response.ok("{\"message\":\"Vittoria registrata per " + username + "\", \"logFilePath\":\"" + serverLogPath + "\", \"durataMs\":" + durataMs + "}").type(MediaType.APPLICATION_JSON).build();
             } else {
                 logger.error("ERRORE INASPETTATO: L'utente '{}' esisteva ma l'update recordVictory è fallito. Cancello file caricato.", username);
                 deleteUploadedFile(serverLogPath);
                 return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Errore inaspettato durante l'aggiornamento del giocatore '" + username + "'\"}").type(MediaType.APPLICATION_JSON).build();
             }
        // Catch aggiornati per includere NumberFormatException (anche se gestita sopra)
        } catch (ParseException | NumberFormatException e) { // Gestisce sia parsing data/ora che durata
             // Il parsing durata è già gestito sopra, questo catch è più per data/ora
             // Se l'errore è NumberFormatException per durata, viene già ritornato 400 prima
             logger.warn("Errore di parsing per {}: Data='{}', Ora='{}', Durata='{}', Errore={}", username, dataString, oraString, durataMsString, e.getMessage());
              return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Formato data (YYYY-MM-DD), ora (HH:MM:SS) o durataMs (numero) non valido: " + e.getMessage() + "\"}").type(MediaType.APPLICATION_JSON).build();
        } catch (IOException e) {
              // ... (come prima) ...
               logger.error("Errore I/O durante salvataggio file log per {}: {}", username, e.getMessage(), e);
               return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Errore durante il salvataggio del file di log: " + e.getMessage() + "\"}").type(MediaType.APPLICATION_JSON).build();
        } catch (SQLException e) {
             // ... (come prima, la logica con fileSaved è corretta) ...
              if (!fileSaved) {
                   logger.error("Errore DB durante recupero giocatore {}: {}", username, e.getMessage(), e);
                   return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Errore interno del server durante verifica utente: " + e.getMessage() + "\"}") .type(MediaType.APPLICATION_JSON).build();
              } else {
                   logger.error("Errore DB durante registrazione vittoria per {}: {}", username, e.getMessage(), e);
                   deleteUploadedFile(serverLogPath);
                   return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Errore interno del server (DB) durante aggiornamento: " + e.getMessage() + "\"}").type(MediaType.APPLICATION_JSON).build();
              }
        } catch (IllegalArgumentException e) {
             // ... (come prima, la logica con fileSaved è corretta) ...
               if (!fileSaved) {
                    logger.warn("Input non valido per recupero giocatore {}: {}", username, e.getMessage());
                    return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Input non valido per verifica utente: "+e.getMessage()+"\"}").type(MediaType.APPLICATION_JSON).build();
               } else {
                    logger.warn("Input non valido per recordVictory (multipart): {}", e.getMessage());
                    deleteUploadedFile(serverLogPath);
                    return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Input non valido per aggiornamento vittoria: "+e.getMessage()+"\"}").type(MediaType.APPLICATION_JSON).build();
               }
        } finally {
            // Assicurati che l'InputStream sia chiuso
            if (logFileInputStream != null) {
                try { logFileInputStream.close(); } catch (IOException e) { /* Ignora errore chiusura */ }
            }
        }
    }

    // Endpoint per ottenere i dati di un giocatore
    // GET /players/{username}
    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPlayer(@PathParam("username") String username) {
         logger.info("Ricevuta richiesta GET /players/{}", username);
         if (username == null || username.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\":\"Username mancante nel path\"}")
                           .build();
        }
        try {
            PlayerRecord player = playerDAO.getPlayer(username);
            if (player != null) {
                // Trovato, restituisce 200 OK con i dati del giocatore in JSON
                return Response.ok(player).build(); // Jersey si occupa della serializzazione in JSON
            } else {
                // Non trovato, restituisce 404 Not Found
                logger.warn("Giocatore non trovato: {}", username);
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("{\"error\":\"Giocatore '" + username + "' non trovato\"}")
                               .build();
            }
        } catch (SQLException e) {
            logger.error("Errore DB durante recupero giocatore {}: {}", username, e.getMessage(), e);
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\":\"Errore interno del server: " + e.getMessage() + "\"}")
                           .build();
        } catch (IllegalArgumentException e) {
             logger.warn("Input non valido per getPlayer: {}", e.getMessage());
             return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\":\"" + e.getMessage() + "\"}")
                           .build();
        }
    }

    // --- Metodi di utility per parsing date/ore ---
    private Date parseSqlDate(String dateString) throws ParseException {
        if (dateString == null) return null;
        // Usa SimpleDateFormat (non thread-safe, crea nuovo ogni volta o usa java.time)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false); // Rifiuta date non valide (es. 2023-02-30)
        try {
             java.util.Date utilDate = sdf.parse(dateString);
             return new java.sql.Date(utilDate.getTime());
        } catch (ParseException e) {
            throw new ParseException("Formato data non valido, atteso YYYY-MM-DD: " + dateString, e.getErrorOffset());
        }
    }

    private Time parseSqlTime(String timeString) throws ParseException {
         if (timeString == null) return null;
         SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
         sdf.setLenient(false);
        try {
             java.util.Date utilDate = sdf.parse(timeString);
            return new java.sql.Time(utilDate.getTime());
        } catch (ParseException e) {
             throw new ParseException("Formato ora non valido, atteso HH:MM:SS: " + timeString, e.getErrorOffset());
        }
    }
     
    private void deleteUploadedFile(String filePath) {
        if (filePath != null) {
            try {
                boolean deleted = Files.deleteIfExists(Paths.get(filePath));
                if (deleted) {
                    logger.info("File '{}' cancellato a causa di errore successivo o utente non trovato.", filePath);
                } else {
                     logger.warn("Tentativo di cancellare file '{}', ma non esisteva più.", filePath);
                }
            } catch (IOException e) {
                // Logga l'errore ma non rilanciare l'eccezione per non mascherare l'errore originale
                logger.error("Impossibile cancellare il file '{}': {}", filePath, e.getMessage(), e);
            }
        }
    }
}
