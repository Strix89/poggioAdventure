package com.mycompany.poggioadventure.persistence;

import com.mycompany.poggioadventure.ui.ErrorHandler;
import com.mycompany.poggioadventure.core.utils.Utils;
import static com.mycompany.poggioadventure.persistence.ResourceLoader.LOGS_DIRECTORY;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Sistema logging sicuro per registrazione input utente con crittografia Base64.
 * 
 * <p>Gestisce CRUD operations su file log con protezioni di sicurezza integrate:
 * path traversal prevention, isolamento directory, validazione formati.
 * Implementa crittografia automatica per protezione dati sensibili.
 * 
 * <p><b>Funzionalità principali:</b>
 * <ul>
 *   <li>Generazione file log con UUID per unicità</li>
 *   <li>Crittografia automatica Base64 per input utente</li>
 *   <li>Validazione pattern filename e sicurezza path</li>
 *   <li>Verifica integrità dati e decodifica sicura</li>
 *   <li>Gestione file temporanei per export/sync</li>
 * </ul>
 * 
 * <p><b>Sicurezza:</b> Prevenzione path traversal, validazione UUID,
 * isolamento directory logs, controlli accesso filesystem.
 * 
 * <p><b>Pattern:</b> Repository per persistenza logs, Factory per
 * generazione filename, Strategy per encoding/decoding.
 */
public class LoggerInput {
    
    /** Path completo file log corrente */
    private String fileName;
    
    /** Handler centralizzato per gestione errori */
    private final ErrorHandler errorHandler;
    
    /** Pattern validazione filename log con UUID e estensione standard */
    public static final String LOGS_DIR_PATTERN = 
        Pattern.quote(LOGS_DIRECTORY.toString().replace(File.separatorChar, '/')) + 
        "/[a-f0-9-]{36}_Input\\.txt";

    /**
     * Costruttore factory per nuovo file log con generazione automatica filename.
     * Crea directory se necessario e inizializza file fisico.
     * 
     * @param errorHandler Handler errori per gestione eccezioni
     */
    public LoggerInput(ErrorHandler errorHandler) {
        if (errorHandler == null) {
            throw new IllegalArgumentException("ErrorHandler non può essere null");
        }
        this.errorHandler = errorHandler;
        this.fileName = generateUniqueFileName();
        createLogFile();
    }
    
    /**
     * Costruttore per caricamento file log esistente.
     * Non verifica esistenza fino a primo utilizzo per lazy loading.
     * 
     * @param errorHandler Handler errori per gestione eccezioni
     * @param fileName Nome file relativo a directory logs
     */
    public LoggerInput(ErrorHandler errorHandler, String fileName) {
        if (errorHandler == null) {
            throw new IllegalArgumentException("ErrorHandler non può essere null");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("Nome file non può essere null");
        }
        this.errorHandler = errorHandler;
        this.fileName = ResourceLoader.LOGS_DIRECTORY.resolve(fileName).toString();
    }

    /**
     * Generatore filename univoco con UUID e pattern standardizzato.
     * Format: [UUID]_Input.txt nella directory logs dedicata.
     * 
     * @return Path completo nuovo file log
     */
    private String generateUniqueFileName() {
        try {
            Files.createDirectories(ResourceLoader.LOGS_DIRECTORY);
            Path logPath = ResourceLoader.LOGS_DIRECTORY.resolve(UUID.randomUUID() + "_Input.txt");
            return logPath.toString();
        } catch (IOException ex) {
            errorHandler.handleFatalError("Impossibile creare la cartella dei log", ex);
            Utils.exitApplication(Utils.EXIT_CODE_LOG_ERROR);
            throw new IllegalStateException("Impossibile generare nome file", ex);
        }
    }

    /** Creazione fisica file log con gestione duplicati */
    private void createLogFile() {
        if (fileName == null) return;

        try {
            File logFile = new File(fileName);
            if (!logFile.createNewFile()) {
                errorHandler.handleRecoverableError("File di log già esistente: " + fileName);
            }
        } catch (IOException ex) {
            errorHandler.handleFatalError("Errore durante la creazione del file di log", ex);
            Utils.exitApplication(Utils.EXIT_CODE_LOG_ERROR);
        }
    }

    /**
     * Persistenza comandi con encoding Base64 automatico.
     * Append mode per preservare cronologia sessioni.
     * 
     * @param commands Lista comandi da persistere
     * @throws IOException Errori I/O durante scrittura
     */
    public void logInput(List<String> commands) throws IOException {
        if (commands == null || commands.isEmpty()) return;
        
        Files.write(Path.of(fileName),
            commands.stream()
                .map(cmd -> Base64.getEncoder().encodeToString(cmd.getBytes()))
                .collect(Collectors.toList()),
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND,
            StandardOpenOption.WRITE
        );
    }

    /**
     * Lettura e decodifica sicura contenuti log con validazione path.
     * Prevenzione path traversal e controllo integrità encoding.
     * 
     * @param fileName Path completo file da decodificare
     * @return Lista comandi decodificati o lista vuota se file assente
     * @throws IOException Errori lettura/decodifica
     * @throws SecurityException Tentativo accesso file non autorizzato
     */
    public static List<String> readAndDecodeLogFile(String fileName) throws IOException {
        if (fileName == null) {
            throw new IllegalArgumentException("Nome file non può essere null");
        }
        
        Path filePath = Paths.get(fileName);
        if (!Files.exists(filePath)) {
            return Collections.emptyList();
        }
        
        // Controllo sicurezza: isolamento directory logs
        if (!filePath.startsWith(ResourceLoader.LOGS_DIRECTORY.toRealPath())) {
            throw new SecurityException("Tentativo di accesso a file non autorizzato");
        }
        
        return Files.readAllLines(filePath)
                .stream()
                .map(encoded -> {
                    try {
                        return new String(Base64.getDecoder().decode(encoded));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Contenuto del log non valido", e);
                    }
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Eliminazione sicura file log con validazione path.
     * 
     * @param path Path file da eliminare
     * @return true se eliminazione riuscita
     * @throws SecurityException Tentativo eliminazione file non autorizzato
     */
    public static boolean deleteLogFile(Path path) {
        if (path == null) return false;
        
        if (!path.startsWith(ResourceLoader.LOGS_DIRECTORY)) {
            throw new SecurityException("Tentativo di eliminare file non autorizzato");
        }
        
        try {
            return Files.deleteIfExists(path);
        } catch (IOException ex) {
            return false;
        }
    }
    
    /**
     * Verifica integrità file log: esistenza, validità Base64, path sicuro.
     * 
     * @param fileName Path completo file da verificare
     * @return true se file integro e valido
     * @throws SecurityException Tentativo accesso file non autorizzato
     */
    public static boolean checkLog(String fileName) {
        Path logPath = Paths.get(fileName);
        
        if (!logPath.startsWith(ResourceLoader.LOGS_DIRECTORY)) {
            throw new SecurityException("Tentativo di accesso a file non autorizzato");
        }
        
        if (!Files.exists(logPath)) return false;
        
        try {
            Files.readAllLines(logPath).forEach(
                line -> Base64.getDecoder().decode(line)
            );
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /** Accessor path completo file log corrente */
    public String getFileName() {
        return fileName;
    }
    
    /** Accessor Path object file log corrente */
    public Path getPathFile() {
        return Paths.get(fileName);
    }
    
    /** Mutator filename con validazione null */
    public void setFileName(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("Nome file non può essere null");
        }
        this.fileName = fileName;
    }
    
    /**
     * Validazione filename log: pattern UUID, estensione standard, tipo file regolare.
     * 
     * @param logPath Path da validare
     * @return true se formato valido e file regolare
     * @throws IOException Errori accesso filesystem
     */
    public static boolean isValidLogFile(Path logPath) throws IOException {
        if (logPath == null) return false;
        
        String filename = logPath.getFileName().toString();
        return filename.endsWith("_Input.txt") && 
               filename.matches("[a-f0-9-]{36}_Input\\.txt") &&
               Files.isRegularFile(logPath);
    }

    /**
     * Factory file temporaneo decrittato per export/sync server.
     * Decodifica contenuto e crea file temp nella directory download logs.
     * 
     * @param originalLogPath Path file log crittato originale
     * @param playerName Nome giocatore per naming file temp
     * @return Path file temporaneo decrittato
     * @throws Exception Errori decodifica o creazione file temp
     */
    public static Path createDecryptedTempLogFile(String originalLogPath, String playerName) throws Exception {
        List<String> decryptedCommands = LoggerInput.readAndDecodeLogFile(originalLogPath);
        
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile(
            ResourceLoader.LOGS_DW_DIRECTORY, 
            playerName + "_temp_", 
            "_decrypted.txt"
        );
        
        java.nio.file.Files.write(
            tempFile, 
            decryptedCommands, 
            java.nio.charset.StandardCharsets.UTF_8,
            java.nio.file.StandardOpenOption.CREATE,
            java.nio.file.StandardOpenOption.WRITE,
            java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
        );
        
        return tempFile;
    }
}
