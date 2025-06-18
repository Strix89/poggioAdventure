package com.mycompany.poggioserver.db;

import com.mycompany.poggioserver.config.PathConfiguration;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Gestisce il pool di connessioni al database usando HikariCP.
 * Inizializza lo schema del database al caricamento della classe.
 * 
 */
public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static HikariDataSource dataSource;

    /**
     * Inizializzazione statica - eseguita una sola volta al caricamento della classe.
     * Configura HikariCP e crea lo schema del database.
     */
    static {
        try {
            logger.info("Inizializzazione DatabaseManager...");

            // Carica configurazione usando percorso centralizzato
            Properties dbProps = loadProperties();

            // Configura HikariCP
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbProps.getProperty("db.url"));
            config.setUsername(dbProps.getProperty("db.user"));
            config.setPassword(dbProps.getProperty("db.password"));
            config.setDriverClassName(dbProps.getProperty("db.driverClassName"));

            // Parametri del pool con valori di default
            config.setMaximumPoolSize(Integer.parseInt(dbProps.getProperty("hikaricp.maximumPoolSize", "10")));
            config.setConnectionTimeout(Long.parseLong(dbProps.getProperty("hikaricp.connectionTimeout", "30000")));
            config.setIdleTimeout(Long.parseLong(dbProps.getProperty("hikaricp.idleTimeout", "600000")));
            config.setMaxLifetime(Long.parseLong(dbProps.getProperty("hikaricp.maxLifetime", "1800000")));
            config.setPoolName(dbProps.getProperty("hikaricp.poolName", "PoggioPool"));

            logger.info("Configurazione HikariCP completata per pool '{}'", config.getPoolName());

            // Crea il pool di connessioni
            dataSource = new HikariDataSource(config);
            logger.info("Pool di connessioni creato");

            // Inizializza lo schema del database
            initializeDatabaseSchema();

            logger.info("Inizializzazione DatabaseManager completata");

        } catch (IOException e) {
            logger.error("FATAL: Errore caricamento file properties", e);
            throw new RuntimeException("Impossibile caricare la configurazione del database.", e);
        } catch (NumberFormatException e) {
            logger.error("FATAL: Errore formato numerico nelle proprietà HikariCP", e);
            throw new RuntimeException("Configurazione pool HikariCP non valida.", e);
        } catch (Exception e) {
            logger.error("FATAL: Errore durante l'inizializzazione DatabaseManager", e);
            throw new RuntimeException("Inizializzazione DatabaseManager fallita.", e);
        }
    }

    /**
     * Carica le proprietà di configurazione dal file esterno o dal classpath.
     */
    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        
        // Prima prova a caricare dal file esterno usando percorso centralizzato
        Path externalPropertiesPath = PathConfiguration.getDbPropertiesPath();
        if (externalPropertiesPath.toFile().exists()) {
            logger.info("Caricamento properties da file esterno: {}", externalPropertiesPath.toAbsolutePath());
            try (InputStream input = Files.newInputStream(externalPropertiesPath)) {
                properties.load(input);
                logger.info("File properties esterno caricato con successo");
                return properties;
            }
        }
        
        // Fallback al file nel classpath
        String classpathFile = "/db.properties";
        logger.info("File esterno non trovato, caricamento da classpath: {}", classpathFile);
        try (InputStream input = DatabaseManager.class.getResourceAsStream(classpathFile)) {
            if (input == null) {
                logger.error("File properties '{}' non trovato né nel filesystem né nel classpath", classpathFile);
                throw new IOException("File properties non trovato: " + classpathFile);
            }
            properties.load(input);
            logger.info("File properties caricato da classpath");
        }
        
        return properties;
    }

    /**
     * Crea la tabella players se non esiste.
     */
    private static void initializeDatabaseSchema() throws SQLException {
        logger.info("Verifica/creazione schema database...");
        
        String createTableSQL = "CREATE TABLE IF NOT EXISTS players (" +
                                "username VARCHAR(255) PRIMARY KEY, " +
                                "data DATE NULL, " +
                                "ora TIME NULL, " +
                                "percorso_file_log VARCHAR(1024) NULL, " +
                                "durata_ms BIGINT NULL, " +
                                "punteggio INT NULL)";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            logger.info("Tabella 'players' verificata/creata");
        } catch (SQLException e) {
            logger.error("FATAL: Errore durante l'inizializzazione schema", e);
            throw e;
        }
    }

    /**
     * Ottiene una connessione dal pool.
     * Il chiamante è responsabile della chiusura.
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Chiude il pool di connessioni.
     * Da chiamare durante lo shutdown dell'applicazione.
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Chiusura pool di connessioni '{}'...", dataSource.getPoolName());
            dataSource.close();
            logger.info("Pool chiuso");
        } else if (dataSource != null && dataSource.isClosed()) {
            logger.warn("Pool già chiuso");
        } else {
            logger.warn("DataSource non inizializzato");
        }
    }

    /**
     * Restituisce il path del file properties utilizzato.
     */
    public static String getPropFile(){
        Path externalPath = PathConfiguration.getDbPropertiesPath();
        if (externalPath.toFile().exists()) {
            return externalPath.toString();
        }
        return "/db.properties (classpath)";
    }
}