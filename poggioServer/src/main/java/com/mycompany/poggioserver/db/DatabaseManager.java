package com.mycompany.poggioserver.db;

// Import HikariCP (Connection Pool library)
import com.zaxxer.hikari.HikariConfig; // Classe per configurare il pool HikariCP
import com.zaxxer.hikari.HikariDataSource; // Implementazione DataSource di HikariCP (il pool stesso)

// Import Logging (SLF4J)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Import standard Java IO e SQL
import java.io.IOException; // Per eccezioni I/O (es. lettura file properties)
import java.io.InputStream; // Per leggere dati da file (es. file properties)
import java.sql.Connection; // Interfaccia per una connessione al database
import java.sql.SQLException; // Eccezione per errori SQL
import java.sql.Statement; // Interfaccia per eseguire statement SQL statici
import java.util.Properties; // Per caricare e gestire file di configurazione .properties

/**
 * Gestisce l'accesso al database per PoggioServer, principalmente attraverso
 * la configurazione e la fornitura di un pool di connessioni HikariCP.
 * Si occupa anche dell'inizializzazione dello schema del database (creazione tabella)
 * al momento del caricamento della classe.
 * <p>
 * Utilizza un blocco di inizializzazione statico per configurare e creare
 * il pool di connessioni ({@link HikariDataSource}) una sola volta all'avvio
 * dell'applicazione, leggendo i parametri da un file {@code /db.properties}
 * presente nel classpath.
 *
 */
public class DatabaseManager {

    // Logger SLF4J per questa classe
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    // Path (relativo al classpath) del file di configurazione del database
    private static final String PROPERTIES_FILE = "/db.properties";
    // Istanza statica del DataSource HikariCP (il connection pool)
    // Sarà inizializzata nel blocco static sottostante.
    private static HikariDataSource dataSource;

    /**
     * Blocco di inizializzazione statico.
     * Questo blocco viene eseguito automaticamente dalla JVM *una sola volta*,
     * quando la classe `DatabaseManager` viene caricata per la prima volta.
     * È responsabile di:
     * 1. Caricare le proprietà di configurazione dal file .properties.
     * 2. Configurare HikariCP (`HikariConfig`).
     * 3. Creare il pool di connessioni (`HikariDataSource`).
     * 4. Inizializzare lo schema del database (`initializeDatabaseSchema`).
     * Eventuali eccezioni durante questo blocco sono critiche e impediranno
     * l'avvio corretto dell'applicazione (verranno catturate come `ExceptionInInitializerError`).
     */
    static {
        try {
            logger.info("Inizializzazione statica di DatabaseManager in corso...");

            // 1. Carica le proprietà da db.properties
            logger.debug("Caricamento proprietà da {}", PROPERTIES_FILE);
            Properties dbProps = loadProperties();

            // 2. Configura HikariCP
            logger.debug("Configurazione HikariConfig...");
            HikariConfig config = new HikariConfig();
            // Imposta i parametri JDBC base
            config.setJdbcUrl(dbProps.getProperty("db.url"));
            config.setUsername(dbProps.getProperty("db.user"));
            config.setPassword(dbProps.getProperty("db.password"));
            // Specifica la classe del driver JDBC (HikariCP la caricherà)
            config.setDriverClassName(dbProps.getProperty("db.driverClassName"));

            // Imposta parametri specifici del pool HikariCP, usando valori di default se non presenti nel file
            config.setMaximumPoolSize(Integer.parseInt(dbProps.getProperty("hikaricp.maximumPoolSize", "10"))); // Max connessioni
            config.setConnectionTimeout(Long.parseLong(dbProps.getProperty("hikaricp.connectionTimeout", "30000"))); // Max attesa per connessione (ms)
            config.setIdleTimeout(Long.parseLong(dbProps.getProperty("hikaricp.idleTimeout", "600000"))); // Max tempo connessione inattiva (ms)
            config.setMaxLifetime(Long.parseLong(dbProps.getProperty("hikaricp.maxLifetime", "1800000"))); // Max vita connessione (ms)
            config.setPoolName(dbProps.getProperty("hikaricp.poolName", "PoggioPool")); // Nome del pool (utile nei log)
            // Si potrebbero aggiungere altre configurazioni Hikari qui (es. connectionTestQuery)

            logger.info("Configurazione HikariCP completata per pool '{}'", config.getPoolName());
            logger.debug("Dettagli configurazione HikariCP: {}", config);

            // 3. Crea il DataSource HikariCP (il pool effettivo)
            dataSource = new HikariDataSource(config);
            logger.info("Pool di connessioni HikariDataSource ('{}') creato.", config.getPoolName());

            // 4. Inizializza lo schema del database (es. crea tabelle se non esistono)
            initializeDatabaseSchema();

            logger.info("Inizializzazione statica di DatabaseManager completata con successo.");

        } catch (IOException e) {
            // Errore durante la lettura del file .properties
            logger.error("FATAL: Errore I/O durante il caricamento del file di properties '{}'", PROPERTIES_FILE, e);
            // Rilancia come RuntimeException per causare ExceptionInInitializerError e fermare l'app
            throw new RuntimeException("Impossibile caricare la configurazione del database.", e);
        } catch (NumberFormatException e) {
            // Errore se una proprietà numerica nel file .properties non è un numero valido
            logger.error("FATAL: Errore nel formato numerico di una proprietà HikariCP nel file '{}'", PROPERTIES_FILE, e);
            throw new RuntimeException("Configurazione pool HikariCP non valida (errore numerico).", e);
        } catch (Exception e) {
            // Cattura altre eccezioni potenziali durante la configurazione di Hikari o l'init dello schema
            logger.error("FATAL: Errore critico durante l'inizializzazione statica di DatabaseManager.", e);
            throw new RuntimeException("Inizializzazione DatabaseManager fallita.", e);
        }
    }

    /**
     * Metodo helper privato per caricare le proprietà di configurazione
     * dal file specificato in {@code PROPERTIES_FILE} nel classpath.
     *
     * @return Un oggetto {@link Properties} contenente le configurazioni caricate.
     * @throws IOException Se il file non viene trovato o si verifica un errore durante la lettura.
     */
    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        // Usa getResourceAsStream per caricare un file dal classpath
        try (InputStream input = DatabaseManager.class.getResourceAsStream(PROPERTIES_FILE)) {
            // Controlla se il file è stato effettivamente trovato
            if (input == null) {
                logger.error("File di properties '{}' non trovato nel classpath!", PROPERTIES_FILE);
                // Lancia eccezione se il file è mancante
                throw new IOException("File properties non trovato: " + PROPERTIES_FILE);
            }
            // Carica le proprietà dall'InputStream
            properties.load(input);
            logger.info("File properties '{}' caricato correttamente.", PROPERTIES_FILE);
        } // try-with-resources chiude automaticamente l'InputStream
        return properties;
    }

    /**
     * Metodo helper privato per inizializzare lo schema del database.
     * In questo caso, crea la tabella 'players' se non esiste già.
     * Utilizza una connessione temporanea dal pool appena creato.
     *
     * @throws SQLException Se si verifica un errore durante l'esecuzione dell'SQL.
     */
    private static void initializeDatabaseSchema() throws SQLException {
        logger.info("Verifica/Creazione dello schema del database (tabella 'players')...");
        // Statement SQL per creare la tabella 'players' solo se non esiste già.
        // Definisce le colonne, i tipi di dato e la chiave primaria.
        String createTableSQL = "CREATE TABLE IF NOT EXISTS players (" +
                                "username VARCHAR(255) PRIMARY KEY, " + // Username univoco
                                "data DATE NULL, " +                     // Data ultima vittoria (solo data)
                                "ora TIME NULL, " +                      // Ora ultima vittoria (solo ora)
                                "percorso_file_log VARCHAR(1024) NULL, " +// Path del file di log associato
                                "durata_ms BIGINT NULL, " +              // Durata partita in millisecondi
                                "punteggio INT NULL)";                   // Punteggio ottenuto

        // Usa try-with-resources per ottenere una connessione e uno statement,
        // assicurando che vengano chiusi automaticamente anche in caso di errore.
        try (Connection conn = dataSource.getConnection(); // Ottiene connessione dal pool
             Statement stmt = conn.createStatement()) {    // Crea uno statement SQL
            // Esegue lo statement SQL per creare la tabella
            stmt.execute(createTableSQL);
            logger.info("Tabella 'players' verificata/creata con successo.");
        } catch (SQLException e) {
            // Errore durante l'esecuzione SQL (sintassi, permessi, connessione persa, etc.)
            logger.error("FATAL: Errore SQL durante l'inizializzazione dello schema (tabella 'players').", e);
            // Rilancia l'eccezione per essere catturata dal blocco static principale
            throw e;
        }
    }

    /**
     * Metodo pubblico statico per ottenere una connessione al database dal pool HikariCP.
     * I chiamanti sono responsabili della chiusura di questa connessione
     * (preferibilmente usando un blocco try-with-resources).
     *
     * @return Una {@link Connection} attiva dal pool.
     * @throws SQLException Se si verifica un errore durante l'ottenimento della connessione dal pool
     * (es. pool esaurito e timeout raggiunto, problemi di rete).
     */
    public static Connection getConnection() throws SQLException {
        // Delega la richiesta di connessione direttamente al DataSource HikariCP.
        // HikariCP gestisce internamente l'attesa, la validazione e il logging delle connessioni.
        return dataSource.getConnection();
    }

    /**
     * Metodo pubblico statico per chiudere il pool di connessioni HikariDataSource.
     * Questo metodo DOVREBBE essere chiamato quando l'applicazione viene terminata
     * per rilasciare tutte le connessioni al database e le risorse del pool in modo pulito.
     * È tipicamente chiamato dalla logica di shutdown dell'applicazione (es. in `PoggioServer.main`).
     */
    public static void shutdown() {
        // Controlla se il dataSource è stato inizializzato prima di tentare la chiusura
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Inizio chiusura pool di connessioni HikariDataSource ('{}')...", dataSource.getPoolName());
            // Chiama il metodo close() di HikariDataSource per chiudere il pool
            dataSource.close();
            logger.info("Pool di connessioni ('{}') chiuso correttamente.", dataSource.getPoolName()); // Usa getPoolName qui per sicurezza se dataSource fosse null dopo close
        } else if (dataSource != null && dataSource.isClosed()) {
             logger.warn("Tentativo di chiudere un pool di connessioni già chiuso ('{}').", dataSource.getPoolName());
        } else {
             logger.warn("Tentativo di chiudere DatabaseManager, ma il dataSource non è stato inizializzato.");
        }
    }

    /**
     * Metodo getter pubblico statico per ottenere il path del file di properties utilizzato.
     * Utile per scopi informativi o di logging da altre parti dell'applicazione (es. PoggioServer).
     *
     * @return La stringa contenente il path del file properties (es. "/db.properties").
     */
    public static String getPropFile(){
        return PROPERTIES_FILE;
    }
}