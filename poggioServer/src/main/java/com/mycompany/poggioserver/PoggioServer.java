package com.mycompany.poggioserver;

// Import gestione Database
import com.mycompany.poggioserver.db.DatabaseManager; // Classe per gestire connessioni/pool DB

// Import componenti JAX-RS/Jersey (Filtri, Risorse, Features)
import com.mycompany.poggioserver.filters.ApiKeyFilter; // Filtro per validare le API Key nelle richieste
import com.mycompany.poggioserver.resources.PlayerResource; // Classe che definisce gli endpoint API REST (/players)
import org.glassfish.jersey.server.ResourceConfig; // Classe per configurare l'applicazione JAX-RS (Jersey)
import org.glassfish.jersey.media.multipart.MultiPartFeature; // Feature Jersey per abilitare il parsing di richieste multipart (upload file)

// Import Server HTTP Grizzly e integrazione Jersey
import org.glassfish.grizzly.http.server.HttpServer; // Classe del server HTTP Grizzly
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory; // Factory per creare server Grizzly che ospitano app Jersey

// Import Logging (SLF4J)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Import standard Java
import java.io.IOException; // Per eccezioni I/O (es. avvio server)
import java.net.URI; // Per rappresentare l'URI del server

/**
 * Classe principale del server backend PoggioServer.
 * Configura JAX-RS con Jersey e avvia un server HTTP Grizzly embedded.
 * 
 * @author Strix89
 */
public class PoggioServer {
    private static final Logger logger = LoggerFactory.getLogger(PoggioServer.class);

    // Server in ascolto su tutte le interfacce di rete sulla porta 8080
    public static final String BASE_URI = "http://0.0.0.0:8080/";

    /**
     * Configura l'applicazione JAX-RS registrando risorse, feature e filtri.
     */
    public static ResourceConfig createJaxRsApp() {
        logger.info("Configurazione applicazione JAX-RS...");
        final ResourceConfig rc = new ResourceConfig()
            .register(PlayerResource.class)              // Endpoint API per i giocatori
            .register(org.glassfish.jersey.gson.JsonGsonFeature.class)  // Support JSON con Gson
            .register(MultiPartFeature.class)            // Support per upload file multipart
            .register(ApiKeyFilter.class);               // Filtro autenticazione API key

        logger.info("JAX-RS configurato. Classi registrate: {}", rc.getClasses());
        return rc;
    }

    /**
     * Entry point dell'applicazione.
     * Inizializza il database, configura JAX-RS e avvia il server HTTP.
     */
    public static void main(String[] args) throws IOException {
        logger.info("Avvio PoggioServer...");

        // Inizializza il DatabaseManager (triggera il blocco static)
        logger.info("Inizializzazione DatabaseManager...");
        try {
            Class.forName("com.mycompany.poggioserver.db.DatabaseManager");
            logger.info("DatabaseManager caricato");
        } catch (ClassNotFoundException e) {
            logger.error("FATAL: Classe DatabaseManager non trovata", e);
            System.exit(1);
        } catch (ExceptionInInitializerError e) {
            logger.error("FATAL: Errore inizializzazione DatabaseManager", e.getCause());
            System.exit(1);
        }

        // Configura l'applicazione JAX-RS
        final ResourceConfig rc = createJaxRsApp();

        // Avvia il server HTTP Grizzly
        logger.info("Avvio server HTTP su {}", BASE_URI);
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
        logger.info("Server avviato");

        // Mostra info per facilitare l'accesso dalla rete locale
        try {
            java.net.InetAddress localhost = java.net.InetAddress.getLocalHost();
            logger.info("IP locale: {}. Server raggiungibile su: http://{}:8080",
                       localhost.getHostAddress(), localhost.getHostAddress());
        } catch (java.net.UnknownHostException e) {
            logger.warn("Impossibile determinare l'IP locale");
        }
        logger.info("File properties DB: {}", DatabaseManager.class.getResource(DatabaseManager.getPropFile()));

        // Registra shutdown hook per chiusura pulita
        logger.info("Registrazione shutdown hook...");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.warn("Shutdown hook attivato - chiusura forzata");
            server.shutdownNow();
            logger.info("Server fermato (da hook)");
            DatabaseManager.shutdown();
            logger.info("DB chiuso (da hook)");
            logger.warn("Chiusura forzata completata");
        }, "PoggioServerShutdownHook"));

        // Attende input utente per shutdown normale
        logger.info("Server in ascolto su {}. Premi INVIO per fermare...", BASE_URI);
        try {
            System.in.read();
        } finally {
            logger.info("Shutdown normale...");
            server.shutdownNow();
            logger.info("Server fermato");
            DatabaseManager.shutdown();
            logger.info("DB chiuso");
            logger.info("Shutdown completato");
        }
    }
}