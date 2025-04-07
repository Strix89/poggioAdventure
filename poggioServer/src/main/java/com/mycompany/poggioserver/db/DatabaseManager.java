package com.mycompany.poggioserver.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

// Gestisce il pool di connessioni e l'inizializzazione
/**
 *
 * @author Strix89
 */
public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String PROPERTIES_FILE = "/db.properties"; // Path nel classpath
    private static HikariDataSource dataSource; // Il pool di connessioni

    // Blocco statico per inizializzare il pool e la tabella
    static {
        try {
            logger.info("Inizializzazione DatabaseManager...");
            Properties dbProps = loadProperties();

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbProps.getProperty("db.url"));
            config.setUsername(dbProps.getProperty("db.user"));
            config.setPassword(dbProps.getProperty("db.password"));
            config.setDriverClassName(dbProps.getProperty("db.driverClassName")); // Hikari preferisce questo a Class.forName

            // Configura HikariCP con valori dal file o default
            config.setMaximumPoolSize(Integer.parseInt(dbProps.getProperty("hikaricp.maximumPoolSize", "10")));
            config.setConnectionTimeout(Long.parseLong(dbProps.getProperty("hikaricp.connectionTimeout", "30000")));
            config.setIdleTimeout(Long.parseLong(dbProps.getProperty("hikaricp.idleTimeout", "600000")));
            config.setMaxLifetime(Long.parseLong(dbProps.getProperty("hikaricp.maxLifetime", "1800000")));
            config.setPoolName(dbProps.getProperty("hikaricp.poolName", "PoggioPool"));

            logger.info("Configurazione HikariCP: {}", config);
            dataSource = new HikariDataSource(config); // Crea il pool
            logger.info("HikariDataSource creato con successo.");

            // Inizializza (crea/verifica) la tabella dopo aver creato il pool
            initializeDatabaseSchema();

        } catch (IOException e) {
            logger.error("Errore durante il caricamento del file di properties '{}'", PROPERTIES_FILE, e);
            throw new RuntimeException("Impossibile caricare la configurazione del database.", e);
        } catch (NumberFormatException e) {
             logger.error("Errore nel formato numerico delle proprietà HikariCP nel file '{}'", PROPERTIES_FILE, e);
            throw new RuntimeException("Configurazione pool non valida.", e);
        } catch (Exception e) { // Catch generico per errori Hikari o DB
            logger.error("Errore critico durante l'inizializzazione del DatabaseManager.", e);
            throw new RuntimeException("Inizializzazione DatabaseManager fallita.", e);
        }
    }

    // Carica le proprietà dal file nel classpath
    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = DatabaseManager.class.getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                logger.error("File di properties '{}' non trovato nel classpath.", PROPERTIES_FILE);
                throw new IOException("File properties non trovato: " + PROPERTIES_FILE);
            }
            properties.load(input);
            logger.info("File properties '{}' caricato con successo.", PROPERTIES_FILE);
        }
        return properties;
    }

    // Metodo per creare/verificare la tabella
    private static void initializeDatabaseSchema() throws SQLException {
        logger.info("Verifica/Creazione tabella 'players'...");
        String createTableSQL = "CREATE TABLE IF NOT EXISTS players (" +
                                "username VARCHAR(255) PRIMARY KEY, " +
                                "data DATE NULL, " +
                                "ora TIME NULL, " +
                                "percorso_file_log VARCHAR(1024) NULL)";

        // Usa try-with-resources per ottenere e chiudere la connessione/statement
        try (Connection conn = dataSource.getConnection(); // Ottiene connessione dal pool
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            logger.info("Tabella 'players' verificata/creata con successo.");
        } catch (SQLException e) {
             logger.error("Errore SQL durante l'inizializzazione dello schema", e);
             throw e; // Rilancia per bloccare l'avvio se fallisce
        }
    }

    // Metodo pubblico per ottenere una connessione dal pool
    public static Connection getConnection() throws SQLException {
        // Non è più necessario loggare qui ogni volta, HikariCP ha il suo logging
        return dataSource.getConnection();
    }

    // Metodo per chiudere il pool di connessioni (da chiamare allo shutdown)
    public static void shutdown() {
        if (dataSource != null) {
            logger.info("Chiusura del pool di connessioni HikariDataSource...");
            dataSource.close();
            logger.info("Pool di connessioni chiuso.");
        }
    }
    
    public static String getPropFile(){
        return PROPERTIES_FILE;
    }
    
}