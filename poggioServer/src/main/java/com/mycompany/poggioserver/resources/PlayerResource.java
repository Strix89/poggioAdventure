package com.mycompany.poggioserver.resources;

import com.mycompany.poggioserver.db.PlayerDAO;
import com.mycompany.poggioserver.db.PlayerDAOImpl;
import com.mycompany.poggioserver.dto.VictoryData;
import com.mycompany.poggioserver.model.PlayerRecord;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    // Endpoint per registrare una vittoria per un giocatore esistente
    // PUT /players/{username}/victory
    @PUT
    @Path("/{username}/victory")
    @Consumes(MediaType.APPLICATION_JSON) // Accetta dati JSON nel body
    @Produces(MediaType.APPLICATION_JSON)
    public Response recordVictory(@PathParam("username") String username, VictoryData victoryData) {
         logger.info("Ricevuta richiesta PUT /players/{}/victory", username);
         if (username == null || username.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\":\"Username mancante nel path\"}")
                           .build();
        }
        if (victoryData == null || victoryData.getData() == null || victoryData.getOra() == null) {
             return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\":\"Dati della vittoria (data, ora) mancanti nel body JSON\"}")
                           .build();
        }

        try {
            // Parsing di data e ora dal DTO
            // NOTA: Il parsing di date/ore è delicato. Assicurarsi che il formato sia consistente.
            // java.sql.Date e java.sql.Time sono un po' legacy, ma usati negli esempi JDBC.
            Date sqlDate = parseSqlDate(victoryData.getData());
            Time sqlTime = parseSqlTime(victoryData.getOra());

            boolean updated = playerDAO.recordVictory(username, sqlDate, sqlTime, victoryData.getPercorsoFileLog());

            if (updated) {
                // Aggiornamento riuscito, restituisce 200 OK
                 logger.info("Vittoria registrata per {}", username);
                return Response.ok("{\"message\":\"Vittoria registrata per " + username + "\"}").build();
            } else {
                // Giocatore non trovato per l'aggiornamento, restituisce 404 Not Found
                logger.warn("Tentativo di registrare vittoria per utente non esistente: {}", username);
                 return Response.status(Response.Status.NOT_FOUND)
                               .entity("{\"error\":\"Giocatore '" + username + "' non trovato per registrare la vittoria\"}")
                               .build();
            }
        } catch (ParseException e) {
             logger.warn("Formato data/ora non valido per {}: Data='{}', Ora='{}', Errore={}",
                         username, victoryData.getData(), victoryData.getOra(), e.getMessage());
             return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\":\"Formato data (YYYY-MM-DD) o ora (HH:MM:SS) non valido: " + e.getMessage() + "\"}")
                           .build();
        } catch (SQLException e) {
             logger.error("Errore DB durante registrazione vittoria per {}: {}", username, e.getMessage(), e);
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\":\"Errore interno del server: " + e.getMessage() + "\"}")
                           .build();
        } catch (IllegalArgumentException e) {
             logger.warn("Input non valido per recordVictory: {}", e.getMessage());
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
}
