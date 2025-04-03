package com.mycompany.gamestatapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 *
 * @author tomma
 */
@Entity
@Table(name = "completions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Completion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime completionDateTime;

    @Column(nullable = false)
    private int score;

    @Column // Percorso del file di comandi sul server
    private String commandFilePath;

    // Relazione Many-to-One con Player
    // Molti completamenti possono appartenere a un giocatore
    @ManyToOne(fetch = FetchType.LAZY) // LAZY = carica il Player solo quando serve
    @JoinColumn(name = "player_id", nullable = false) // Colonna chiave esterna
    private Player player;
    
    public Completion(){
        // Costruttore senza parametri per le transaction
    }

    // Costruttore utile per la creazione
    public Completion(Player player, LocalDateTime completionDateTime, int score, String commandFilePath) {
        this.player = player;
        this.completionDateTime = completionDateTime;
        this.score = score;
        this.commandFilePath = commandFilePath;
    }

    // Esempio RTTI (Identificazione a run-time)
    public void printPlayerInfo() {
        if (player != null) {
            System.out.println("Processing completion for Player object of type: " + player.getClass().getName());
            System.out.println("Player details: " + player.toString());
        } else {
            System.out.println("Player information not available for this completion.");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCompletionDateTime() {
        return completionDateTime;
    }

    public void setCompletionDateTime(LocalDateTime completionDateTime) {
        this.completionDateTime = completionDateTime;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getCommandFilePath() {
        return commandFilePath;
    }

    public void setCommandFilePath(String commandFilePath) {
        this.commandFilePath = commandFilePath;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}