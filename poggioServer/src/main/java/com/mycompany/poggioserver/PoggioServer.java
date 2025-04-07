package com.mycompany.poggioserver;

import com.mycompany.poggioserver.db.DatabaseManager;
import com.mycompany.poggioserver.resources.PlayerResource;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URI;

/**
 *
 * @author Strix89
 */
public class PoggioServer {
    private static final Logger logger = LoggerFactory.getLogger(PoggioServer.class);
    // URI base su cui il server ascolterà
    public static final String BASE_URI = "http://0.0.0.0:8080/";

    /**
     * Crea e configura l'applicazione JAX-RS.
     * @return Una istanza di ResourceConfig.
     */
    public static ResourceConfig createJaxRsApp() {
        // Crea una configurazione e registra la nostra classe Resource
        // Jersey scansionerà automaticamente le annotazioni (@Path, @GET, ecc.)
        final ResourceConfig rc = new ResourceConfig()
                .register(PlayerResource.class)
                 // Registra il provider Gson di Jersey (necessario se usi jersey-media-json-gson)
                .register(org.glassfish.jersey.gson.JsonGsonFeature.class); // Con l'import corretti
        logger.info("Applicazione JAX-RS configurata con le risorse: {}", rc.getClasses());
        return rc;
    }

    /**
     * Metodo Main per avviare il server Grizzly.
     * @param args Argomenti da linea di comando (non usati).
     * @throws IOException Se c'è un errore nell'avvio del server.
     */
    public static void main(String[] args) throws IOException {
        // L'inizializzazione del DB ora avviene nel blocco static di DatabaseManager
        // Il Class.forName qui non è più strettamente necessario per l'init,
        // ma lasciarlo assicura che DatabaseManager sia caricato se non lo fosse già.
        // Potrebbe essere rimosso se altri usi garantiscono il caricamento.
        try {
             Class.forName("com.mycompany.poggioserver.db.DatabaseManager");
             logger.info("DatabaseManager caricato/verificato.");
         } catch (ClassNotFoundException e) {
             logger.error("Impossibile trovare DatabaseManager, l'applicazione non può partire.", e);
             // L'eccezione nel blocco static dovrebbe già aver fermato tutto,
             // ma per sicurezza usciamo.
             System.exit(1);
         } catch (ExceptionInInitializerError e) {
             // Questa cattura l'errore se il blocco static lancia RuntimeException
             logger.error("Errore durante inizializzazione statica di DatabaseManager!", e.getCause());
             System.exit(1);
         }


        final ResourceConfig rc = createJaxRsApp();
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);

        try {
             // Trova e logga l'IP locale (questo è solo un esempio, potrebbe non funzionare su tutte le macchine/reti)
             java.net.InetAddress localhost = java.net.InetAddress.getLocalHost();
             logger.info("Server potenzialmente raggiungibile su: http://{}:8080", localhost.getHostAddress());
        } catch (java.net.UnknownHostException e) {
             logger.warn("Impossibile determinare l'IP locale.");
        }
        logger.info("Database properties caricate da: {}", DatabaseManager.class.getResource(DatabaseManager.getPropFile()));
        logger.info("Premi Enter per fermare il server...");

        // Aggiungi uno Shutdown Hook per sicurezza, se l'applicazione viene
        // terminata bruscamente (es. CTRL+C in console)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook attivato. Chiusura risorse...");
            server.shutdownNow(); // Ferma prima il server
            DatabaseManager.shutdown(); // Poi chiudi il pool DB
            logger.info("Risorse chiuse dallo shutdown hook.");
        }, "ShutdownHookThread"));


        // Attesa normale per Enter
        try {
           System.in.read();
        } finally {
           // Lo shutdown hook dovrebbe gestire la chiusura, ma possiamo
           // chiamarlo anche qui per un arresto "normale".
           // Lo shutdown hook eviterà doppie chiusure se viene eseguito.
           logger.info("Tasto Enter premuto. Inizio shutdown normale...");
           server.shutdownNow();
           DatabaseManager.shutdown();
           logger.info("Server e pool DB chiusi (shutdown normale).");
        }
    }
}
