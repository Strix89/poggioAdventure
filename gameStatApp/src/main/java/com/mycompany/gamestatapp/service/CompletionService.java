package com.mycompany.gamestatapp.service;

import com.mycompany.gamestatapp.exception.ResourceNotFoundException;
import com.mycompany.gamestatapp.repository.CompletionRepository;
import com.mycompany.gamestatapp.repository.PlayerRepository;
import com.mycompany.gamestatapp.model.Completion;
import com.mycompany.gamestatapp.model.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Per gestire le transazioni
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author Strix89
 */
@Service
public class CompletionService {

    @Autowired
    private CompletionRepository completionRepository;

    @Autowired
    private PlayerRepository playerRepository; // Necessario per associare il Player

    @Autowired
    private FileStorageService fileStorageService; // Per salvare il file

    public List<Completion> getAllCompletions() {
        return completionRepository.findAll();
    }

    public List<Completion> getCompletionsByPlayerId(Long playerId) {
        if (!playerRepository.existsById(playerId)) {
             throw new ResourceNotFoundException("Player not found with id: " + playerId);
        }
        return completionRepository.findByPlayerId(playerId);
    }

    public Completion getCompletionById(Long id) {
        Completion completion = completionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Completion not found with id: " + id));

        // Esempio uso RTTI (chiamata al metodo nell'entity)
        completion.printPlayerInfo();

        return completion;
    }

    @Transactional // Assicura che o tutto va a buon fine (DB + file) o nulla (rollback)
    public Completion createCompletion(Long playerId, int score, MultipartFile file) {
        // 1. Trova il giocatore
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + playerId));

        // 2. Crea l'oggetto Completion (senza path file inizialmente)
        Completion completion = new Completion();
        completion.setPlayer(player);
        completion.setScore(score);
        completion.setCompletionDateTime(LocalDateTime.now()); // Data/ora correnti

        // 3. Salva temporaneamente per ottenere l'ID (necessario per nome file univoco)
        Completion savedCompletion = completionRepository.save(completion);

        // 4. Salva il file usando l'ID del completion
        String fileName = fileStorageService.storeFile(file, savedCompletion.getId());

        // 5. Aggiorna l'oggetto Completion con il path del file e salva di nuovo
        savedCompletion.setCommandFilePath(fileName);
        return completionRepository.save(savedCompletion); // Salva l'aggiornamento
    }

     @Transactional
     public void deleteCompletion(Long id) {
         Completion completion = getCompletionById(id); // Verifica esistenza

         // Cancella il file associato se esiste
         if (completion.getCommandFilePath() != null && !completion.getCommandFilePath().isEmpty()) {
            fileStorageService.deleteFile(completion.getCommandFilePath());
         }

         // Cancella il record dal DB
         completionRepository.delete(completion);
     }
}
