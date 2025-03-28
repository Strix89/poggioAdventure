package di.uniba.map.b.adventure.parser;

import di.uniba.map.b.adventure.ErrorHandler;
import di.uniba.map.b.adventure.ResourceLoader;
import di.uniba.map.b.adventure.Utils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Classe che registra gli input validi dell'utente in file di log univoci.
 * Utilizza UUID per evitare conflitti e salva nella directory dedicata definita in ResourceLoader.
 */
public class LoggerInput {
    private String fileName;
    private final ErrorHandler errorHandler;

    /**
     * Costruttore principale.
     * @param errorHandler Per la gestione degli errori
     */
    public LoggerInput(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        this.fileName = generateUniqueFileName();
        createLogFile();
    }
    
    public LoggerInput(ErrorHandler errorHandler, String fileName) {
        this.errorHandler = errorHandler;
        this.fileName = ResourceLoader.LOGS_DIRECTORY.resolve(fileName).toString();
    }

    /**
     * Genera un nome file univoco con UUID.
     * Esempio: "a3d8f7b0-2e4a-4fcd-8f9a-1b7c6d3e5f2a_Input.txt"
     */
    private String generateUniqueFileName() {
        try {
            Files.createDirectories(ResourceLoader.LOGS_DIRECTORY);
            Path logPath = ResourceLoader.LOGS_DIRECTORY.resolve(UUID.randomUUID() + "_Input.txt");
            return logPath.toString();
        } catch (IOException ex) {
            errorHandler.handleFatalError("Impossibile creare la cartella dei log", ex);
            Utils.exitApplication(Utils.EXIT_CODE_LOG_ERROR);
            return null;
        }
    }

    /**
     * Crea il file di log fisico sul filesystem.
     */
    private void createLogFile() {
        if (fileName == null) return;

        File file = new File(fileName);
        try {
            file.createNewFile();
        } catch (IOException ex) {
            errorHandler.handleFatalError("Errore durante la creazione del file di log", ex);
            Utils.exitApplication(Utils.EXIT_CODE_LOG_ERROR);
        }
    }

    /**
     * Imposta un nome file specifico (usato durante il caricamento di un salvataggio).
     * @param fileName Percorso completo del file
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Registra un input valido nel file di log in formato Base64.
     * @param input Comando inserito dall'utente
     */
    public void logInput(String input) {
        if (fileName == null || input == null || input.trim().isEmpty()) return;

        String encodedInput = Base64.getEncoder().encodeToString(input.getBytes());
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(encodedInput + "\n");
        } catch (IOException ex) {
            errorHandler.handleFatalError("Errore durante la scrittura sul log", ex);
            Utils.exitApplication(Utils.EXIT_CODE_LOG_ERROR);
        }
    }
    
    /**
     * Metodo statico per leggere e decodificare il contenuto di un file di log.
     * @param fileName Percorso completo del file
     * @return Lista di stringhe decodificate
     * @throws IOException Se si verifica un errore di lettura
     */
    public static List<String> readAndDecodeLogFile(String fileName) throws IOException {
        if (!Files.exists(Paths.get(fileName))) {
            return Collections.emptyList();
        }
        
        List<String> encodedLines = Files.readAllLines(Paths.get(fileName));
        return encodedLines.stream()
                .map(encoded -> new String(Base64.getDecoder().decode(encoded)))
                .collect(Collectors.toList());
    }
    
    /**
     * Elimina un file di log se esiste nella directory dedicata.
     * @param fileName Percorso completo del file da eliminare
     * @return true se il file è stato eliminato con successo, false altrimenti
     */
    public static boolean deleteLogFile(String fileName) {
        Path path = Paths.get(fileName);
        
        // Verifica che il file sia nella cartella dei log autorizzata
        if (!path.startsWith(ResourceLoader.LOGS_DIRECTORY)) {
            return false;
        }
        
        try {
            return Files.deleteIfExists(path);
        } catch (IOException ex) {
            return false;
        }
    }

    // Getter per il nome del file (usato durante il salvataggio)
    public String getFileName() {
        return fileName;
    }
}