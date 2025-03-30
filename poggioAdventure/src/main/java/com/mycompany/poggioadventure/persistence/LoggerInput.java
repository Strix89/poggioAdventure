package com.mycompany.poggioadventure.persistence;

import di.uniba.map.b.adventure.ErrorHandler;
import com.mycompany.poggioadventure.persistence.ResourceLoader;
import com.mycompany.poggioadventure.core.utils.Utils;
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
 * Gestisce la registrazione e il recupero degli input utente in file di log cifrati.
 * 
 * <p>Caratteristiche principali:
 * <ul>
 *   <li>Genera file di log con nomi univoci basati su UUID</li>
 *   <li>Registra gli input in formato Base64 per maggiore sicurezza</li>
 *   <li>Supporta operazioni di lettura/scrittura/eliminazione</li>
 *   <li>Integra gestione degli errori tramite ErrorHandler</li>
 *   <li>Utilizza la directory dei log definita in ResourceLoader</li>
 * </ul>
 * 
 * @author Strix89
 */
public class LoggerInput {
    /**
     * Percorso completo del file di log corrente.
     * Viene generato automaticamente o impostato durante il caricamento.
     */
    private String fileName;
    
    /**
     * Gestore degli errori per la segnalazione di problemi.
     */
    private final ErrorHandler errorHandler;

    /**
     * Costruttore principale che inizializza un nuovo file di log.
     * 
     * @param errorHandler Gestore degli errori (non può essere null)
     * @throws IllegalArgumentException Se errorHandler è null
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
     * @param errorHandler Gestore degli errori (non può essere null)
     * @param fileName Nome del file di log da caricare
     * @throws IllegalArgumentException Se errorHandler è null
     */
    public LoggerInput(ErrorHandler errorHandler, String fileName) {
        if (errorHandler == null) {
            throw new IllegalArgumentException("ErrorHandler non può essere null");
        }
        this.errorHandler = errorHandler;
        this.fileName = ResourceLoader.LOGS_DIRECTORY.resolve(fileName).toString();
    }

    /**
     * Genera un nome file univoco nella directory dei log.
     * 
     * @return Percorso completo del nuovo file
     * @throws IllegalStateException Se la directory dei log non può essere creata
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

    /**
     * Crea fisicamente il file di log sul filesystem.
     */
    private void createLogFile() {
        if (fileName == null) return;

        try {
            new File(fileName).createNewFile();
        } catch (IOException ex) {
            errorHandler.handleFatalError("Errore durante la creazione del file di log", ex);
            Utils.exitApplication(Utils.EXIT_CODE_LOG_ERROR);
        }
    }

    /**
     * Registra un input utente nel log in formato Base64.
     * 
     * @param input Comando da registrare (ignorato se null o vuoto)
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
     * Legge e decodifica il contenuto di un file di log.
     * 
     * @param fileName Percorso completo del file da leggere
     * @return Lista di comandi decodificati (lista vuota se file non esiste)
     * @throws IOException Se si verifica un errore di lettura
     * @throws IllegalArgumentException Se fileName è null
     */
    public static List<String> readAndDecodeLogFile(String fileName) throws IOException {
        if (fileName == null) {
            throw new IllegalArgumentException("Nome file non può essere null");
        }
        
        if (!Files.exists(Paths.get(fileName))) {
            return Collections.emptyList();
        }
        
        return Files.readAllLines(Paths.get(fileName))
                .stream()
                .map(encoded -> new String(Base64.getDecoder().decode(encoded)))
                .collect(Collectors.toList());
    }
    
    /**
     * Elimina un file di log in modo sicuro.
     * 
     * @param fileName Percorso completo del file da eliminare
     * @return true se eliminato con successo, false altrimenti
     * @throws SecurityException Se si tenta di eliminare file al di fuori della directory dei log
     */
    public static boolean deleteLogFile(String fileName) {
        if (fileName == null) return false;
        
        Path path = Paths.get(fileName);
        
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
    * Verifica l'esistenza di un file di log e controlla se contiene comandi.
    * 
    * @param fileName Percorso completo del file da verificare
    * @return true se il file esiste e contiene comandi, false altrimenti
    * @throws SecurityException Se si tenta di accedere a file non nella directory dei log
    */
   public static boolean checkLog(String fileName) {
       if (fileName == null) return false;

       Path path = Paths.get(fileName);

       // Controllo sicurezza
       if (!path.startsWith(ResourceLoader.LOGS_DIRECTORY)) {
           throw new SecurityException("Accesso non autorizzato alla directory dei log");
       }
       
       return Files.exists(path);
   }

    /**
     * Restituisce il nome del file di log corrente.
     * 
     * @return Percorso completo del file di log
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
     * Imposta un nuovo file di log.
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
}