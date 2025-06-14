package com.mycompany.poggioserver.resources;

import com.mycompany.poggioserver.db.PlayerDAO;
import com.mycompany.poggioserver.db.PlayerDAOImpl;
import com.mycompany.poggioserver.model.PlayerRecord;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Endpoint REST per la gestione dei giocatori.
 * Fornisce operazioni CRUD, upload/download log e classifica.
 * 
 * @author Strix89
 */
@jakarta.ws.rs.Path("/players")
public class PlayerResource {

    private static final Logger logger = LoggerFactory.getLogger(PlayerResource.class);
    private final PlayerDAO playerDAO = new PlayerDAOImpl();

    // Directory per i file log uploadati
    private static final String UPLOAD_LOG_DIRECTORY = Paths.get("resources", "uploaded_logs").toAbsolutePath().toString();

    /**
     * POST /players/{username} - Crea un nuovo giocatore
     */
    @POST
    @jakarta.ws.rs.Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addPlayer(@PathParam("username") String username) {
        logger.info("POST /players/{}", username);

        if (username == null || username.trim().isEmpty()) {
            logger.warn("Username mancante nella richiesta");
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\":\"BAD_REQUEST\", \"message\":\"Username mancante nel path\"}")
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        }

        try {
            playerDAO.addPlayer(username);
            return Response.status(Response.Status.CREATED)
                           .entity("{\"message\":\"Giocatore '" + username + "' creato con successo.\"}")
                           .type(MediaType.APPLICATION_JSON)
                           .build();

        } catch (SQLException e) {
            // Gestisce username duplicato
            if (e.getMessage() != null && e.getMessage().contains("esiste già")) {
                logger.warn("Username già esistente: {}", username);
                return Response.status(Response.Status.CONFLICT)
                               .entity("{\"error\":\"USER_ALREADY_EXISTS\", \"message\":\"Username '" + username + "' esiste già.\"}")
                               .type(MediaType.APPLICATION_JSON)
                               .build();
            }
            logger.error("Errore DB durante aggiunta giocatore {}: {}", username, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("{\"error\":\"SERVER_ERROR\", \"message\":\"Errore database: " + e.getMessage() + "\"}")
                          .type(MediaType.APPLICATION_JSON)
                          .build();
        } catch (IllegalArgumentException e) {
            logger.warn("Input non valido: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity("{\"error\":\"INVALID_INPUT\", \"message\":\"" + e.getMessage() + "\"}")
                          .type(MediaType.APPLICATION_JSON)
                          .build();
        }
    }

    /**
     * PUT /players/{username}/victory - Registra una vittoria con upload log
     */
    @PUT
    @jakarta.ws.rs.Path("/{username}/victory")
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
        logger.info("PUT /players/{}/victory", username);

        // Validazione input base
        if (username == null || username.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"INVALID_INPUT\", \"message\":\"Username mancante\"}").type(MediaType.APPLICATION_JSON).build();
        }
        if (dataString == null || oraString == null || durataMsString == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"INVALID_INPUT\", \"message\":\"Parametri 'data', 'ora', 'durataMs' obbligatori\"}").type(MediaType.APPLICATION_JSON).build();
        }
        if (logFileInputStream == null || fileDisposition == null || fileDisposition.getFileName() == null || fileDisposition.getFileName().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"INVALID_INPUT\", \"message\":\"File 'logFile' mancante\"}").type(MediaType.APPLICATION_JSON).build();
        }

        String serverLogPath = null;
        boolean fileSaved = false;
        Long durataMs = null;
        Date sqlDate = null;
        Time sqlTime = null;

        try {
            // Verifica utente esistente e assenza log precedente
            logger.debug("Verifica utente '{}'...", username);
            PlayerRecord existingPlayer = playerDAO.getPlayer(username);
            if (existingPlayer == null) {
                logger.warn("Utente non trovato: {}", username);
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("{\"error\": \"USER_NOT_FOUND\", \"message\":\"Giocatore '" + username + "' non trovato\"}")
                               .type(MediaType.APPLICATION_JSON).build();
            }
            if (existingPlayer.getPercorsoFileLog() != null && !existingPlayer.getPercorsoFileLog().trim().isEmpty()) {
                logger.warn("Log già esistente per '{}'", username);
                return Response.status(Response.Status.CONFLICT)
                               .entity("{\"error\": \"LOG_ALREADY_EXISTS\", \"message\":\"Log già associato a '" + username + "'\"}")
                               .type(MediaType.APPLICATION_JSON).build();
            }

            // Parsing parametri
            logger.debug("Parsing parametri: data='{}', ora='{}', durataMs='{}'", dataString, oraString, durataMsString);
            try {
                sqlDate = parseSqlDate(dataString);
                sqlTime = parseSqlTime(oraString);
                durataMs = Long.valueOf(durataMsString);
                if (durataMs < 0) {
                    throw new NumberFormatException("Durata non può essere negativa");
                }
            } catch (ParseException | NumberFormatException e) {
                logger.warn("Formato parametri non valido: {}", e.getMessage());
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity("{\"error\":\"INVALID_INPUT_FORMAT\", \"message\":\"Formato data (YYYY-MM-DD), ora (HH:MM:SS) o durata non valido: " + e.getMessage() + "\"}")
                               .type(MediaType.APPLICATION_JSON).build();
            }

            // Salvataggio file log
            logger.debug("Salvataggio file log...");
            Path targetPath = null;
            try {
                Path uploadDir = Paths.get(UPLOAD_LOG_DIRECTORY);
                Files.createDirectories(uploadDir);

                String originalFileName = fileDisposition.getFileName();
                String safeOriginalName = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
                String uniqueFileName = username + "_" + System.currentTimeMillis() + "_" + safeOriginalName;
                targetPath = uploadDir.resolve(uniqueFileName);
                serverLogPath = targetPath.toString();

                Files.copy(logFileInputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                fileSaved = true;
                logger.info("File log salvato: {}", serverLogPath);

            } catch (IOException e) {
                logger.error("Errore salvataggio file per '{}': {}", username, e.getMessage(), e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .entity("{\"error\":\"FILE_SAVE_ERROR\", \"message\":\"Errore salvataggio file: " + e.getMessage() + "\"}")
                               .type(MediaType.APPLICATION_JSON).build();
            } catch (InvalidPathException e) {
                logger.error("Path non valido per '{}': {}", username, e.getMessage(), e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .entity("{\"error\":\"SERVER_CONFIG_ERROR\", \"message\":\"Errore configurazione percorso\"}")
                               .type(MediaType.APPLICATION_JSON).build();
            }

            // Calcolo punteggio
            logger.debug("Calcolo punteggio...");
            long lineCount = 0;
            Integer calculatedScore = null;
            try (Stream<String> stream = Files.lines(targetPath, StandardCharsets.UTF_8)) {
                lineCount = stream.count();
                logger.info("File '{}' ha {} righe", serverLogPath, lineCount);
                // Formula punteggio: base 10000 - penalità tempo (secondi) - penalità righe log
                calculatedScore = Math.max(0, 10000 - (durataMs.intValue() / 1000) - ((int)lineCount / 20));
                logger.info("Punteggio calcolato per {}: {}", username, calculatedScore);
            } catch (IOException e) {
                logger.error("Errore lettura file per calcolo score: {}", e.getMessage(), e);
                deleteUploadedFile(serverLogPath);
                fileSaved = false;
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .entity("{\"error\":\"SCORE_CALCULATION_ERROR\", \"message\":\"Errore calcolo punteggio\"}")
                               .type(MediaType.APPLICATION_JSON).build();
            } catch (Exception e) {
                logger.error("Errore imprevisto calcolo score: {}", e.getMessage(), e);
                deleteUploadedFile(serverLogPath);
                fileSaved = false;
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"SCORE_CALCULATION_ERROR\", \"message\":\"Errore calcolo punteggio\"}").type(MediaType.APPLICATION_JSON).build();
            }

            // Aggiornamento database
            logger.debug("Aggiornamento database...");
            boolean updated = playerDAO.recordVictory(username, sqlDate, sqlTime, serverLogPath, durataMs, calculatedScore);

            if (updated) {
                logger.info("Vittoria registrata per '{}' con punteggio {}", username, calculatedScore);
                return Response.ok("{\"message\":\"Vittoria registrata per '" + username + "'\", \"logFilePath\":\"" + serverLogPath + "\", \"score\":" + calculatedScore + "}")
                               .type(MediaType.APPLICATION_JSON).build();
            } else {
                logger.error("Update DB fallito per '{}'", username);
                deleteUploadedFile(serverLogPath);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .entity("{\"error\":\"DB_UPDATE_FAILED\", \"message\":\"Aggiornamento database fallito\"}")
                               .type(MediaType.APPLICATION_JSON).build();
            }

        } catch (SQLException e) {
            logger.error("Errore DB per '{}': {}", username, e.getMessage(), e);
            if (fileSaved) deleteUploadedFile(serverLogPath);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"DB_ERROR\", \"message\":\"Errore database: " + e.getMessage() + "\"}").type(MediaType.APPLICATION_JSON).build();
        } catch (IllegalArgumentException e) {
            logger.warn("Input non valido: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"INVALID_INPUT\", \"message\":\"" + e.getMessage() + "\"}").type(MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            logger.error("Errore imprevisto per '{}': {}", username, e.getMessage(), e);
            if (fileSaved) deleteUploadedFile(serverLogPath);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"UNEXPECTED_SERVER_ERROR\", \"message\":\"Errore server: " + e.getMessage() + "\"}").type(MediaType.APPLICATION_JSON).build();
        } finally {
            // Chiude l'InputStream del file uploadato
            if (logFileInputStream != null) {
                try {
                    logFileInputStream.close();
                } catch (IOException e) {
                    logger.warn("Errore chiusura stream per '{}': {}", username, e.getMessage());
                }
            }
        }
    }

    /**
     * GET /players - Recupera tutti i giocatori
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPlayers() {
        try {
            List<PlayerRecord> players = playerDAO.getAllPlayers();
            return Response.ok(players).build();
        } catch (SQLException e) {
            logger.error("Errore DB: {}", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"DB_ERROR\", \"message\":\"Errore recupero giocatori\"}")
                .build();
        }
    }

    /**
     * GET /players/{username} - Recupera un giocatore specifico
     */
    @GET
    @jakarta.ws.rs.Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPlayer(@PathParam("username") String username) {
        logger.info("GET /players/{}", username);
        if (username == null || username.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\":\"BAD_REQUEST\", \"message\":\"Username mancante\"}")
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        }
        try {
            PlayerRecord player = playerDAO.getPlayer(username);

            if (player != null) {
                return Response.ok(player).build();
            } else {
                logger.warn("Utente non trovato: {}", username);
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("{\"error\":\"USER_NOT_FOUND\", \"message\":\"Giocatore '" + username + "' non trovato\"}")
                               .type(MediaType.APPLICATION_JSON)
                               .build();
            }
        } catch (SQLException e) {
            logger.error("Errore DB per '{}': {}", username, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("{\"error\":\"SERVER_ERROR\", \"message\":\"Errore database: " + e.getMessage() + "\"}")
                          .type(MediaType.APPLICATION_JSON)
                          .build();
        } catch (IllegalArgumentException e) {
            logger.warn("Input non valido: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity("{\"error\":\"INVALID_INPUT\", \"message\":\"" + e.getMessage() + "\"}")
                          .type(MediaType.APPLICATION_JSON)
                          .build();
        } catch (Exception e) {
            logger.error("Errore imprevisto per '{}': {}", username, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"SERVER_ERROR\", \"message\":\"Errore server: " + e.getMessage() + "\"}").type(MediaType.APPLICATION_JSON).build();
        }
    }

    /**
     * DELETE /players/{username} - Elimina un giocatore
     */
    @DELETE
    @jakarta.ws.rs.Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePlayer(@PathParam("username") String username) {
        logger.info("DELETE /players/{}", username);
        if (username == null || username.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\": \"INVALID_INPUT\", \"message\":\"Username mancante\"}")
                           .type(MediaType.APPLICATION_JSON).build();
        }

        try {
            boolean deleted = playerDAO.deletePlayer(username);

            if (deleted) {
                logger.info("Utente {} eliminato", username);
                return Response.ok("{\"message\":\"Giocatore '" + username + "' eliminato con successo\"}")
                               .type(MediaType.APPLICATION_JSON).build();
            } else {
                logger.warn("Utente non trovato per eliminazione: {}", username);
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("{\"error\": \"USER_NOT_FOUND\", \"message\":\"Giocatore '" + username + "' non trovato\"}")
                               .type(MediaType.APPLICATION_JSON).build();
            }
        } catch (SQLException e) {
            logger.error("Errore DB durante eliminazione '{}': {}", username, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("{\"error\": \"SERVER_ERROR\", \"message\":\"Errore database: " + e.getMessage() + "\"}")
                          .type(MediaType.APPLICATION_JSON).build();
        } catch (IllegalArgumentException e) {
            logger.warn("Input non valido: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity("{\"error\": \"INVALID_INPUT\", \"message\":\"" + e.getMessage() + "\"}")
                          .type(MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            logger.error("Errore imprevisto eliminazione '{}': {}", username, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"SERVER_ERROR\", \"message\":\"Errore server: " + e.getMessage() + "\"}").type(MediaType.APPLICATION_JSON).build();
        }
    }

    /**
     * GET /players/ranking - Recupera la classifica
     */
    @GET
    @jakarta.ws.rs.Path("/ranking")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPlayerRanking() {
        logger.info("GET /players/ranking");

        try {
            List<RankingEntryDTO> rankingList = playerDAO.getRanking();
            return Response.ok(rankingList)
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        } catch (SQLException e) {
            logger.error("Errore DB durante recupero classifica: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("{\"error\": \"SERVER_ERROR\", \"message\":\"Errore recupero classifica: " + e.getMessage() + "\"}")
                          .type(MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            logger.error("Errore imprevisto recupero classifica: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("{\"error\": \"SERVER_ERROR\", \"message\":\"Errore server: " + e.getMessage() + "\"}")
                          .type(MediaType.APPLICATION_JSON).build();
        }
    }

    /**
     * GET /players/{username}/log - Download file log giocatore
     */
    @GET
    @jakarta.ws.rs.Path("/{username}/log")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadLogFile(@PathParam("username") String username) {
        logger.info("GET /players/{}/log", username);

        if (username == null || username.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\":\"INVALID_INPUT\", \"message\":\"Username mancante\"}")
                           .type(MediaType.APPLICATION_JSON).build();
        }

        try {
            // Recupera dati giocatore
            PlayerRecord player = playerDAO.getPlayer(username);

            if (player == null) {
                logger.warn("Download log: utente '{}' non trovato", username);
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("{\"error\":\"USER_NOT_FOUND\", \"message\":\"Giocatore '" + username + "' non trovato\"}")
                               .type(MediaType.APPLICATION_JSON).build();
            }
            String serverLogPath = player.getPercorsoFileLog();
            if (serverLogPath == null || serverLogPath.trim().isEmpty()) {
                logger.warn("Download log: nessun log per '{}'", username);
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("{\"error\":\"LOG_NOT_FOUND_IN_DB\", \"message\":\"Nessun log associato a '" + username + "'\"}")
                               .type(MediaType.APPLICATION_JSON).build();
            }

            // Validazione sicurezza e controllo esistenza file
            Path logPath;
            File logFile;
            try {
                logPath = Paths.get(serverLogPath).normalize();
                logFile = logPath.toFile();

                // Controllo anti path traversal
                Path uploadDir = Paths.get(UPLOAD_LOG_DIRECTORY).toAbsolutePath().normalize();
                if (!logPath.toAbsolutePath().startsWith(uploadDir)) {
                    logger.error("PATH TRAVERSAL bloccato! User: {}, Path: '{}'", username, serverLogPath);
                    return Response.status(Response.Status.FORBIDDEN)
                                   .entity("{\"error\":\"ACCESS_DENIED\", \"message\":\"Accesso negato\"}")
                                   .type(MediaType.APPLICATION_JSON).build();
                }

                if (!logFile.exists() || !logFile.isFile() || !logFile.canRead()) {
                    logger.error("File log non trovato sul server: '{}' per '{}'", serverLogPath, username);
                    return Response.status(Response.Status.NOT_FOUND)
                                   .entity("{\"error\":\"FILE_NOT_FOUND_ON_SERVER\", \"message\":\"File log non trovato sul server\"}")
                                   .type(MediaType.APPLICATION_JSON).build();
                }

            } catch (InvalidPathException e) {
                logger.error("Path non valido nel DB per '{}': {}", username, e.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .entity("{\"error\":\"INVALID_FILE_PATH_STORED\", \"message\":\"Path file non valido\"}")
                               .type(MediaType.APPLICATION_JSON).build();
            }

            // Streaming del file
            String downloadFilename = logFile.getName();

            StreamingOutput fileStream = (OutputStream outputStreamHttp) -> {
                try (FileInputStream fis = new FileInputStream(logFile)) {
                    logger.debug("Streaming file '{}'...", logFile.getName());
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        outputStreamHttp.write(buffer, 0, bytesRead);
                    }
                    outputStreamHttp.flush();
                    logger.debug("Streaming completato: '{}'", logFile.getName());
                } catch (IOException e) {
                    logger.error("Errore streaming file '{}': {}", serverLogPath, e.getMessage());
                    throw new WebApplicationException("Errore streaming file", e, Response.Status.INTERNAL_SERVER_ERROR);
                }
            };

            logger.info("Download file '{}' ({} bytes) per '{}'", serverLogPath, logFile.length(), username);
            return Response.ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                           .header("Content-Disposition", "attachment; filename=\"" + downloadFilename + "\"")
                           .header("Content-Length", logFile.length())
                           .build();

        } catch (SQLException e) {
            logger.error("Errore DB download log '{}': {}", username, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"DB_ERROR\", \"message\":\"Errore database\"}" ).type(MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            logger.error("Errore imprevisto download log '{}': {}", username, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"UNEXPECTED_SERVER_ERROR\", \"message\":\"Errore server\"}" ).type(MediaType.APPLICATION_JSON).build();
        }
    }

    // --- Metodi di utility ---

    /**
     * Parsa una stringa data in formato yyyy-MM-dd.
     */
    private Date parseSqlDate(String dateString) throws ParseException {
        if (dateString == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            java.util.Date utilDate = sdf.parse(dateString);
            return new java.sql.Date(utilDate.getTime());
        } catch (ParseException e) {
            throw new ParseException("Formato data non valido, atteso yyyy-MM-DD: '" + dateString + "'", e.getErrorOffset());
        }
    }

    /**
     * Parsa una stringa orario in formato HH:mm:ss.
     */
    private Time parseSqlTime(String timeString) throws ParseException {
        if (timeString == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setLenient(false);
        try {
            java.util.Date utilDate = sdf.parse(timeString);
            return new java.sql.Time(utilDate.getTime());
        } catch (ParseException e) {
            throw new ParseException("Formato ora non valido, atteso HH:mm:ss: '" + timeString + "'", e.getErrorOffset());
        }
    }

    /**
     * Tenta di eliminare un file uploadato per cleanup.
     * Non lancia eccezioni per non mascherare errori precedenti.
     */
    private void deleteUploadedFile(String filePath) {
        if (filePath != null) {
            try {
                boolean deleted = Files.deleteIfExists(Paths.get(filePath));
                if (deleted) {
                    logger.info("File '{}' cancellato per cleanup", filePath);
                } else {
                    logger.warn("File '{}' non trovato per cleanup", filePath);
                }
            } catch (IOException e) {
                logger.error("Impossibile cancellare file '{}': {}", filePath, e.getMessage(), e);
            } catch (InvalidPathException e) {
                logger.error("Path non valido per cleanup: '{}'", filePath, e);
            }
        }
    }
}