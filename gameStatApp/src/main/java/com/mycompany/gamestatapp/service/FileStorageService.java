package com.mycompany.gamestatapp.service;

import com.mycompany.gamestatapp.config.FileStorageConfig;
import com.mycompany.gamestatapp.exception.FileStorageException;
import com.mycompany.gamestatapp.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 *
 * @author Strix89
 */
@Service // Indica che è un componente Service gestito da Spring
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageConfig fileStorageConfig) {
        // Ottiene il path dalla configurazione (application.properties)
        this.fileStorageLocation = Paths.get(fileStorageConfig.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            // Crea la directory se non esiste (NIO I/O)
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file, Long completionId) {
        // Normalizza il nome del file
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        try {
             if (originalFileName.contains(".")) {
                 fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
             }
            // Genera un nome univoco per evitare collisioni
            String uniqueFileName = "completion-" + completionId + "-" + UUID.randomUUID().toString() + fileExtension;

            // Controlli di sicurezza base sul nome file (opzionale)
            if(uniqueFileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + originalFileName);
            }

            // Copia il file nella directory target (NIO I/O con try-with-resources implicito per l'InputStream)
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            return uniqueFileName; // Restituisce il nome univoco del file salvato
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri()); // Crea una risorsa per il file (I/O)
            if(resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File not found " + fileName, ex);
        }
    }

    public void deleteFile(String fileName) {
         try {
             Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
             Files.deleteIfExists(filePath); // NIO I/O per cancellare
         } catch (IOException ex) {
             // Loggare l'errore, ma potremmo non voler bloccare l'operazione
             System.err.println("Could not delete file: " + fileName + " -> " + ex.getMessage());
             // throw new FileStorageException("Could not delete file " + fileName, ex); // Opzionale
         }
    }
}

