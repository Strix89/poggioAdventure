package com.mycompany.gamestatapp.controller;

import com.mycompany.gamestatapp.service.CompletionService;
import com.mycompany.gamestatapp.service.FileStorageService;
import com.mycompany.gamestatapp.model.Completion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest; // Per determinare Content-Type

import java.io.IOException;
import java.util.List;

/**
 *
 * @author Strix89
 */
@RestController
@RequestMapping("/api") // Base path
public class CompletionController {

    @Autowired
    private CompletionService completionService;

    @Autowired
    private FileStorageService fileStorageService;

    // POST /api/players/{playerId}/completions - Crea un nuovo completamento con upload file
    @PostMapping("/players/{playerId}/completions")
    public ResponseEntity<Completion> createCompletion(
            @PathVariable Long playerId,
            @RequestParam("score") int score, // Dato dalla form-data
            @RequestParam("file") MultipartFile file) { // File dalla form-data

        Completion createdCompletion = completionService.createCompletion(playerId, score, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCompletion);
    }

    // GET /api/completions - Lista tutti i completamenti
    @GetMapping("/completions")
    public List<Completion> getAllCompletions() {
        return completionService.getAllCompletions();
    }

    // GET /api/players/{playerId}/completions - Lista completamenti per giocatore
    @GetMapping("/players/{playerId}/completions")
    public List<Completion> getCompletionsByPlayer(@PathVariable Long playerId) {
        return completionService.getCompletionsByPlayerId(playerId);
    }

    // GET /api/completions/{id} - Ottiene un singolo completamento
    @GetMapping("/completions/{id}")
    public ResponseEntity<Completion> getCompletionById(@PathVariable Long id) {
        Completion completion = completionService.getCompletionById(id);
        return ResponseEntity.ok(completion);
    }

    // GET /api/completions/{id}/commandfile - Scarica il file associato
    @GetMapping("/completions/{id}/commandfile")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id, HttpServletRequest request) {
        // Trova il record completion
        Completion completion = completionService.getCompletionById(id);
        String fileName = completion.getCommandFilePath();

        if (fileName == null || fileName.isEmpty()) {
             return ResponseEntity.notFound().build(); // File non associato
        }

        // Carica il file come Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Prova a determinare il content type del file
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            System.err.println("Could not determine file type.");
        }

        // Fallback a content type generico se non determinato
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                // Header per suggerire al browser di scaricare il file
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // DELETE /api/completions/{id} - Cancella un completamento e il file associato
    @DeleteMapping("/completions/{id}")
    public ResponseEntity<Void> deleteCompletion(@PathVariable Long id) {
        completionService.deleteCompletion(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
