package com.mycompany.poggioserver.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configurazione centralizzata dei percorsi dell'applicazione.
 * Tutti i percorsi sono relativi alla posizione del jar finale.
 */
public class PathConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(PathConfiguration.class);
    
    // Percorso base dell'applicazione (directory del jar)
    private static final Path BASE_PATH;
    
    // Percorsi relativi
    private static final String RESOURCES_DIR = "resources";
    private static final String UPLOADED_LOGS_DIR = "uploaded_logs";
    private static final String CONFIG_DIR = "config";
    private static final String DB_PROPERTIES_FILE = "db.properties";
    
    // Inizializzazione statica del percorso base
    static {
        try {
            // Determina la posizione del jar corrente
            URI jarUri = PathConfiguration.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();
            
            Path jarPath = Paths.get(jarUri);
            
            // Se è un jar, prende la directory padre; se è una directory (sviluppo), la usa direttamente
            if (jarPath.toString().endsWith(".jar")) {
                BASE_PATH = jarPath.getParent();
                logger.info("Modalità JAR: percorso base impostato a {}", BASE_PATH.toAbsolutePath());
            } else {
                // In modalità sviluppo, usa la directory del progetto
                BASE_PATH = jarPath;
                logger.info("Modalità sviluppo: percorso base impostato a {}", BASE_PATH.toAbsolutePath());
            }
            
        } catch (URISyntaxException e) {
            logger.error("FATAL: Impossibile determinare il percorso base dell'applicazione", e);
            throw new RuntimeException("Errore inizializzazione percorsi", e);
        }
    }
    
    /**
     * @return Il percorso base dell'applicazione (directory del jar)
     */
    public static Path getBasePath() {
        return BASE_PATH;
    }
    
    /**
     * @return Il percorso della directory resources
     */
    public static Path getResourcesPath() {
        return BASE_PATH.resolve(RESOURCES_DIR);
    }
    
    /**
     * @return Il percorso della directory per i log uploadati
     */
    public static Path getUploadedLogsPath() {
        return getResourcesPath().resolve(UPLOADED_LOGS_DIR);
    }
    
    /**
     * @return Il percorso della directory di configurazione
     */
    public static Path getConfigPath() {
        return BASE_PATH.resolve(CONFIG_DIR);
    }
    
    /**
     * @return Il percorso completo del file db.properties
     */
    public static Path getDbPropertiesPath() {
        return getConfigPath().resolve(DB_PROPERTIES_FILE);
    }
    
    /**
     * @return Il percorso assoluto della directory per i log uploadati come stringa
     */
    public static String getUploadedLogsPathString() {
        return getUploadedLogsPath().toAbsolutePath().toString();
    }
    
    /**
     * Verifica che tutti i percorsi necessari esistano, creandoli se necessario.
     */
    public static void ensureDirectoriesExist() {
        try {
            File resourcesDir = getResourcesPath().toFile();
            File uploadedLogsDir = getUploadedLogsPath().toFile();
            File configDir = getConfigPath().toFile();
            
            if (!resourcesDir.exists()) {
                boolean created = resourcesDir.mkdirs();
                logger.info("Directory resources creata: {}", created);
            }
            
            if (!uploadedLogsDir.exists()) {
                boolean created = uploadedLogsDir.mkdirs();
                logger.info("Directory uploaded_logs creata: {}", created);
            }
            
            if (!configDir.exists()) {
                boolean created = configDir.mkdirs();
                logger.info("Directory config creata: {}", created);
            }
            
            logger.info("Verifica directory completata");
            
        } catch (Exception e) {
            logger.error("Errore durante la creazione delle directory", e);
            throw new RuntimeException("Impossibile creare le directory necessarie", e);
        }
    }
    
    /**
     * Log dei percorsi configurati per debugging.
     */
    public static void logConfiguredPaths() {
        logger.info("=== PERCORSI CONFIGURATI ===");
        logger.info("Base path: {}", getBasePath().toAbsolutePath());
        logger.info("Resources path: {}", getResourcesPath().toAbsolutePath());
        logger.info("Uploaded logs path: {}", getUploadedLogsPath().toAbsolutePath());
        logger.info("Config path: {}", getConfigPath().toAbsolutePath());
        logger.info("DB properties path: {}", getDbPropertiesPath().toAbsolutePath());
        logger.info("=============================");
    }
}
