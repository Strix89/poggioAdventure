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
 * Gestisce la registrazione, recupero e gestione degli input utente in file di log cifrati.
 * 
 * <p>La classe fornisce un sistema sicuro per:
 * <ul>
 *   <li>Registrare input utente in formato cifrato (Base64)</li>
 *   <li>Garantire l'isolamento dei log nella directory dedicata</li>
 *   <li>Generare nomi file univoci basati su UUID</li>
 *   <li>Verificare l'integrità dei log esistenti</li>
 *   <li>Gestire operazioni CRUD (Create, Read, Update, Delete) sui log</li>
 * </ul>
 * 
 * <p><b>Sicurezza:</b> Tutti gli input vengono automaticamente codificati in Base64 prima della scrittura.
 * Vengono implementati controlli per prevenire:
 * <ul>
 *   <li>Path traversal attacks</li>
 *   <li>Accesso a file esterni alla directory dei log</li>
 *   <li>Corruzione dei dati</li>
 * </ul>
 * 
 * @author Strix89
 * @version 1.1
 */
public class LoggerInput {
    /**
     * Percorso completo del file di log corrente.
     * <p>Viene generato automaticamente al momento della creazione dell'istanza
     * o impostato esplicitamente quando si carica un log esistente.
     * <p>Il formato del nome file è: {@code [UUID]_Input.txt}
     */
    private String fileName;
    
    /**
     * Gestore degli errori centralizzato per la segnalazione di problemi.
     * <p>Viene utilizzato per:
     * <ul>
     *   <li>Errori di I/O durante le operazioni sui file</li>
     *   <li>Problemi di creazione directory</li>
     *   <li>Altri errori runtime critici</li>
     * </ul>
     */
    private final ErrorHandler errorHandler;
    
    /**
     * Pattern regex per validare i nomi dei file di log.
     * <p>Verifica che:
     * <ul>
     *   <li>Il file sia nella directory corretta dei log</li>
     *   <li>Il nome segua il formato UUID + "_Input.txt"</li>
     * </ul>
     */
    protected static final String LOGS_DIR_PATTERN = 
        Pattern.quote(LOGS_DIRECTORY.toString().replace(File.separatorChar, '/')) + 
        "/[a-f0-9-]{36}_Input\\.txt";

    /**
     * Costruttore principale per creare un nuovo file di log.
     * <p>Genera automaticamente un nome file univoco e crea il file fisico.
     * 
     * @param errorHandler Gestore degli errori (non null)
     * @throws IllegalArgumentException Se errorHandler è null
     * @throws IllegalStateException Se la directory dei log non può essere creata
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
     * Costruttore per caricare un file di log esistente.
     * <p>Non verifica l'esistenza del file fino al primo utilizzo.
     * 
     * @param errorHandler Gestore degli errori (non null)
     * @param fileName Nome del file di log (relativo alla directory dei log)
     * @throws IllegalArgumentException Se errorHandler o fileName sono null
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
     * Genera un nome file univoco nella directory dei log.
     * <p>Il nome file segue il formato: {@code [UUID]_Input.txt}
     * 
     * @return Percorso completo del nuovo file
     * @throws IllegalStateException Se la directory dei log non può essere creata
     */
    private String generateUniqueFileName() {
        try {
            // Crea la directory se non esiste
            Files.createDirectories(ResourceLoader.LOGS_DIRECTORY);
            // Genera nome file con UUID
            Path logPath = ResourceLoader.LOGS_DIRECTORY.resolve(UUID.randomUUID() + "_Input.txt");
            return logPath.toString();
        } catch (IOException ex) {
            errorHandler.handleFatalError("Impossibile creare la cartella dei log", ex);
            Utils.exitApplication(Utils.EXIT_CODE_LOG_ERROR);
            throw new IllegalStateException("Impossibile generare nome file", ex);
        }
    }

    /**
     * Crea fisicamente il file di log sul filesystem.
     * <p>Se il file esiste già, non viene sovrascritto.
     * In caso di errore, termina l'applicazione con codice di errore.
     */
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
     * Registra una lista di comandi nel file di log corrente.
     * <p>Ogni comando viene codificato in Base64 prima della scrittura.
     * 
     * @param commands Lista di comandi da registrare (ignorata se null o vuota)
     * @throws IOException Se si verifica un errore di I/O durante la scrittura
     */
    public void logInput(List<String> commands) throws IOException {
        if (commands == null || commands.isEmpty()) return;
        
        // Scrive i comandi codificati in Base64
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
     * Legge e decodifica il contenuto di un file di log.
     * 
     * @param fileName Percorso completo del file da leggere
     * @return Lista di comandi decodificati (lista vuota se file non esiste)
     * @throws IOException Se si verifica un errore di lettura/decodifica
     * @throws IllegalArgumentException Se fileName è null
     * @throws SecurityException Se si tenta di leggere file non nella directory dei log
     */
    public static List<String> readAndDecodeLogFile(String fileName) throws IOException {
        if (fileName == null) {
            throw new IllegalArgumentException("Nome file non può essere null");
        }
        
        Path filePath = Paths.get(fileName);
        if (!Files.exists(filePath)) {
            return Collections.emptyList();
        }
        
        // Verifica sicurezza: solo file nella directory dei log
        if (!filePath.startsWith(ResourceLoader.LOGS_DIRECTORY)) {
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
     * Elimina un file di log in modo sicuro.
     * 
     * @param path Percorso completo del file da eliminare
     * @return true se eliminato con successo, false altrimenti
     * @throws SecurityException Se si tenta di eliminare file al di fuori della directory dei log
     */
    public static boolean deleteLogFile(Path path) {
        if (path == null) return false;
        
        // Verifica sicurezza: solo file nella directory dei log
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
     * Verifica l'integrità di un file di log.
     * <p>Controlla che:
     * <ul>
     *   <li>Il file esista</li>
     *   <li>Ogni riga sia Base64 valido</li>
     *   <li>Il file sia nella directory corretta</li>
     * </ul>
     * 
     * @param fileName Percorso completo del file da verificare
     * @return true se il file è valido, false altrimenti
     * @throws SecurityException Se si tenta di accedere a file non nella directory dei log
     */
    public static boolean checkLog(String fileName) {
        Path logPath = Paths.get(fileName);
        
        // Verifica sicurezza: solo file nella directory dei log
        if (!logPath.startsWith(ResourceLoader.LOGS_DIRECTORY)) {
            throw new SecurityException("Tentativo di accesso a file non autorizzato");
        }
        
        if (!Files.exists(logPath)) return false;
        
        try {
            // Verifica che ogni riga sia Base64 valido
            Files.readAllLines(logPath).forEach(
                line -> Base64.getDecoder().decode(line)
            );
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Restituisce il percorso completo del file di log corrente.
     * 
     * @return Stringa rappresentante il percorso assoluto del file
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
     * Restituisce il Path del file di log corrente.
     * 
     * @return Oggetto Path rappresentante il file di log
     */
    public Path getPathFile() {
        return Paths.get(fileName);
    }
    
    /**
     * Imposta un nuovo file di log come file corrente.
     * 
     * @param fileName Percorso completo del nuovo file
     * @throws IllegalArgumentException Se fileName è null
     */
    public void setFileName(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("Nome file non può essere null");
        }
        this.fileName = fileName;
    }
    
    /**
     * Verifica se un Path rappresenta un file di log valido.
     * <p>Un file è considerato valido se:
     * <ul>
     *   <li>Ha estensione "_Input.txt"</li>
     *   <li>Il nome inizia con un UUID valido</li>
     *   <li>È un file regolare (non directory/symlink)</li>
     * </ul>
     * 
     * @param logPath Path del file da verificare
     * @return true se il file è valido, false altrimenti
     * @throws IOException Se si verificano errori di I/O durante la verifica
     */
    public static boolean isValidLogFile(Path logPath) throws IOException {
        if (logPath == null) return false;
        
        String filename = logPath.getFileName().toString();
        return filename.endsWith("_Input.txt") && 
               filename.matches("[a-f0-9-]{36}_Input\\.txt") &&
               Files.isRegularFile(logPath);
    }
}
