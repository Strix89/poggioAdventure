package com.mycompany.gamestatapp.repository;

import com.mycompany.gamestatapp.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
/**
 *
 * @author Strix89
 */

@Repository // Indica che è un componente Repository gestito da Spring
public interface PlayerRepository extends JpaRepository<Player, Long> {
    // JpaRepository fornisce metodi come findAll(), findById(), save(), deleteById()
    // Possiamo aggiungere metodi custom basati sui nomi (Spring Data li implementa)
    Optional<Player> findByUsername(String username); // Trova un giocatore per username
}