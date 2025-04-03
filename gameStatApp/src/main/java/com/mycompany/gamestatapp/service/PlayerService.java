package com.mycompany.gamestatapp.service;

import com.mycompany.gamestatapp.exception.ResourceNotFoundException;
import com.mycompany.gamestatapp.repository.PlayerRepository;
import com.mycompany.gamestatapp.model.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 *
 * @author Strix89
 */
@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    public List<Player> getAllPlayers() {
        return playerRepository.findAll(); // Usa Lambda/Streams internamente in findAll
    }

    public Player getPlayerById(Long id) {
        // Usa Optional e Lambda per gestire il caso "non trovato"
        return playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + id));
    }

     public Player getPlayerByUsername(String username) {
        // Usa Optional e Lambda
        return playerRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with username: " + username));
    }


    public Player createPlayer(Player player) {
         // Potrebbe aggiungere logica di validazione qui
        return playerRepository.save(player);
    }

    public void deletePlayer(Long id) {
        Player player = getPlayerById(id); // Verifica se esiste prima di cancellare
        // La relazione cascade=ALL e orphanRemoval=true nell'entity Player
        // dovrebbe occuparsi di cancellare anche i Completion associati.
        // Altrimenti, dovremmo cancellarli manualmente qui o gestire la foreign key constraint.
        playerRepository.delete(player);
    }
    
    
}