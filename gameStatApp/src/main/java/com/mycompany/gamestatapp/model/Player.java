package com.mycompany.gamestatapp.model;

import jakarta.persistence.*;
import lombok.Data; // Lombok per getter/setter/costruttori/etc.
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import com.mycompany.gamestatapp.model.Completion;

/**
 *
 * @author Strix89
 */
@Entity // Indica che è una classe mappata su una tabella DB
@Table(name = "players") // Specifica il nome della tabella
@Data // Genera getter, setter, toString, equals, hashCode
@NoArgsConstructor // Genera costruttore senza argomenti
@AllArgsConstructor // Genera costruttore con tutti gli argomenti
public class Player {

    @Id // Chiave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment
    private Long id;

    @Column(nullable = false, unique = true) // Non nullo e univoco
    private String username;

    // Relazione One-to-Many con Completion
    // Un giocatore può avere molti completamenti
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Completion> completions;

    // Costruttore senza ID per la creazione di nuovi giocatori
    public Player(String username) {
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Completion> getCompletions() {
        return completions;
    }

    public void setCompletions(List<Completion> completions) {
        this.completions = completions;
    }
    
    
}