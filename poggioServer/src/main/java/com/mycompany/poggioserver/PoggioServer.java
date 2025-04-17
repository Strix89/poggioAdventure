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
 * Classe principale per l'avvio e la gestione del server backend PoggioServer.
 * Questa classe configura l'applicazione JAX-RS (usando Jersey), avvia un server
 * HTTP embedded (Grizzly) per ospitare l'applicazione, gestisce l'inizializzazione
 * del database e assicura una chiusura pulita delle risorse (server e pool DB)
 * all'arresto dell'applicazione.
 *
 * @author Strix89 // Autore originale
 */
public class PoggioServer {
    // Logger SLF4J per registrare eventi e informazioni del server
    private static final Logger logger = LoggerFactory.getLogger(PoggioServer.class);

    // URI base su cui il server Grizzly si metterà in ascolto.
    // "0.0.0.0" indica che il server ascolterà su tutte le interfacce di rete disponibili sulla macchina.
    // La porta 8080 è quella standard per questo servizio.
    public static final String BASE_URI = "http://0.0.0.0:8080/";

    /**
     * Crea e configura l'istanza dell'applicazione JAX-RS (Jersey).
     * Registra tutte le classi risorsa (che definiscono gli endpoint API),
     * le feature necessarie (es. per JSON, multipart) e i filtri (es. per autenticazione).
     *
     * @return Un oggetto {@link ResourceConfig} pronto per essere passato al factory del server HTTP.
     */
    public static ResourceConfig createJaxRsApp() {
        // Crea una nuova configurazione delle risorse JAX-RS
        // Jersey scansionerà le classi registrate per trovare annotazioni JAX-RS (@Path, @GET, @POST, etc.)
        logger.info("Configurazione applicazione JAX-RS...");
        final ResourceConfig rc = new ResourceConfig()
            // Registra la classe che contiene gli endpoint API relativi ai giocatori
            .register(PlayerResource.class)
            // Registra la feature per abilitare l'uso di Gson come provider JSON
            // Necessita della dipendenza 'jersey-media-json-gson'
            .register(org.glassfish.jersey.gson.JsonGsonFeature.class) // Assicurarsi che l'import sia corretto
            // Registra la feature per abilitare il supporto a richieste multipart/form-data (necessario per upload file)
            .register(MultiPartFeature.class)
            // Registra il filtro custom per validare la presenza e correttezza dell'API Key
            .register(ApiKeyFilter.class);

        // Logga le classi registrate per conferma/debug
        logger.info("Applicazione JAX-RS configurata. Risorse, Feature e Filtri registrati: {}", rc.getClasses());
        return rc; // Restituisce la configurazione creata
    }

    /**
     * Metodo principale (entry point) dell'applicazione server.
     * Esegue i passi necessari per avviare il server:
     * 1. Inizializza/Verifica il Database Manager.
     * 2. Crea la configurazione dell'applicazione JAX-RS.
     * 3. Crea e avvia il server HTTP Grizzly.
     * 4. Registra uno shutdown hook per la chiusura pulita.
     * 5. Attende l'input dell'utente (tasto Invio) per terminare.
     * 6. Esegue la chiusura pulita delle risorse.
     *
     * @param args Argomenti passati da linea di comando (non utilizzati attualmente).
     * @throws IOException Se si verifica un errore I/O durante la creazione/avvio del server HTTP.
     */
    public static void main(String[] args) throws IOException {
        logger.info("Avvio PoggioServer...");

        // --- Fase 1: Inizializzazione Database ---
        // Tenta di caricare la classe DatabaseManager. Questo triggera l'esecuzione
        // del suo blocco statico, che è responsabile dell'inizializzazione del pool di connessioni.
        // È importante gestire gli errori che possono verificarsi durante questa inizializzazione.
        logger.info("Inizializzazione DatabaseManager (tramite caricamento classe)...");
        try {
            // Forza il caricamento della classe, che esegue il blocco static initializer.
             Class.forName("com.mycompany.poggioserver.db.DatabaseManager");
             logger.info("DatabaseManager caricato correttamente.");
        } catch (ClassNotFoundException e) {
             // Errore grave: la classe non è nel classpath. L'applicazione non può funzionare.
             logger.error("FATAL: Classe DatabaseManager non trovata! Assicurati che sia nel classpath.", e);
             System.exit(1); // Termina l'applicazione con codice di errore
        } catch (ExceptionInInitializerError e) {
            // Errore grave: si è verificata un'eccezione DENTRO il blocco statico di DatabaseManager.
            // Questo di solito indica un problema di configurazione DB (file .properties, driver, etc.)
            logger.error("FATAL: Errore durante l'inizializzazione statica di DatabaseManager! Controlla la configurazione DB e i log precedenti.", e.getCause());
             System.exit(1); // Termina l'applicazione con codice di errore
        }
        logger.info("Inizializzazione DatabaseManager completata.");


        // --- Fase 2: Configurazione Applicazione JAX-RS ---
        // Ottiene la configurazione dell'applicazione (registrazione risorse, filtri, features)
        final ResourceConfig rc = createJaxRsApp();


        // --- Fase 3: Creazione e Avvio Server HTTP Grizzly ---
        // Crea un'istanza del server HTTP Grizzly che ospiterà l'applicazione Jersey (`rc`)
        // sull'URI specificato (`BASE_URI`).
        logger.info("Avvio server HTTP Grizzly su {}", BASE_URI);
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
        logger.info("Server Grizzly avviato con successo.");


        // --- Log Informativi Aggiuntivi ---
        try {
            // Tenta di determinare e loggare l'indirizzo IP locale per facilitare l'accesso al server dalla rete locale.
            // Nota: questo potrebbe non essere l'IP corretto in tutte le configurazioni di rete (es. NAT, VPN, Docker).
             java.net.InetAddress localhost = java.net.InetAddress.getLocalHost();
             logger.info("Indirizzo IP locale rilevato: {}. Il server potrebbe essere raggiungibile (dalla rete locale) a: http://{}:8080",
                        localhost.getHostAddress(), localhost.getHostAddress());
        } catch (java.net.UnknownHostException e) {
             logger.warn("Impossibile determinare l'indirizzo IP locale automaticamente.");
        }
        // Logga il percorso da cui sono state caricate le proprietà del database (per debug configurazione)
        logger.info("File properties del database utilizzato: {}", DatabaseManager.class.getResource(DatabaseManager.getPropFile()));


        // --- Fase 4: Registrazione Shutdown Hook ---
        // Registra un "gancio" (hook) che la JVM eseguirà quando sta per terminare
        // (es. a causa di System.exit(), CTRL+C, segnale di terminazione dal S.O.).
        // Questo assicura una chiusura pulita delle risorse anche in caso di arresto non standard.
        logger.info("Registrazione shutdown hook per chiusura pulita...");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.warn("Shutdown hook attivato! Inizio chiusura forzata risorse...");
            // È importante chiudere prima il server per non accettare nuove richieste,
            // poi chiudere il pool del database.
            server.shutdownNow(); // Tenta di fermare il server immediatamente
            logger.info("Server Grizzly fermato (da hook).");
            DatabaseManager.shutdown(); // Chiama il metodo per chiudere il pool di connessioni DB
            logger.info("Pool connessioni DB chiuso (da hook).");
            logger.warn("Chiusura forzata risorse completata (da hook).");
        }, "PoggioServerShutdownHook")); // Nome del thread per facilitare il debug


        // --- Fase 5: Attesa e Shutdown Normale ---
        logger.info("Server PoggioServer avviato e in ascolto su {}. Premi INVIO per fermare...", BASE_URI);
        // Blocca il thread principale indefinitamente finché non viene letto un byte
        // dall'input standard (solitamente, finché l'utente non preme Invio nella console).
        try {
            System.in.read();
        } finally {
            // Questo blocco viene eseguito quando System.in.read() termina (cioè dopo Invio).
            // Esegue lo shutdown "normale" controllato dall'utente.
            logger.info("Tasto INVIO premuto. Inizio shutdown normale...");
            // Anche se lo shutdown hook è registrato, è buona pratica chiamare esplicitamente
            // i metodi di shutdown qui per il flusso normale.
            // I metodi shutdown dovrebbero essere idempotenti o gestire chiamate multiple.
            server.shutdownNow(); // Ferma il server Grizzly
            logger.info("Server Grizzly fermato (shutdown normale).");
            DatabaseManager.shutdown(); // Chiude il pool di connessioni
            logger.info("Pool connessioni DB chiuso (shutdown normale).");
            logger.info("Shutdown normale completato.");
        }
    }
}