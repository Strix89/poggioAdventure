package com.mycompany.gamestatapp.controller;

import com.mycompany.gamestatapp.service.PlayerService;
import com.mycompany.gamestatapp.model.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * @author Strix89
 */
@RestController // Indica che è un controller REST
@RequestMapping("/api/players") // Mapping base per tutte le richieste a questo controller
public class PlayerController {

    @Autowired
    private PlayerService playerService;

    // GET /api/players - Lista tutti i giocatori
    @GetMapping
    public List<Player> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    // GET /api/players/{id} - Ottiene un giocatore per ID
    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable Long id) {
        Player player = playerService.getPlayerById(id);
        return ResponseEntity.ok(player); // Risposta 200 OK con il player nel body
    }

     // GET /api/players/byUsername?username=... - Ottiene un giocatore per Username
     @GetMapping("/byUsername")
     public ResponseEntity<Player> getPlayerByUsername(@RequestParam String username) {
         Player player = playerService.getPlayerByUsername(username);
         return ResponseEntity.ok(player);
     }


    // POST /api/players - Crea un nuovo giocatore
    @PostMapping
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
         // @RequestBody mappa il JSON della richiesta all'oggetto Player
        Player createdPlayer = playerService.createPlayer(new Player(player.getUsername())); // Assicura che si crei solo con username
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlayer); // Risposta 201 Created
    }

    // DELETE /api/players/{id} - Cancella un giocatore
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
        playerService.deletePlayer(id);
        return ResponseEntity.noContent().build(); // Risposta 204 No Content
    }
}