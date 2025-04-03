package com.mycompany.gamestatapp.repository;

import com.mycompany.gamestatapp.model.Completion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


/**
 *
 * @author Strix89
 */
@Repository
public interface CompletionRepository extends JpaRepository<Completion, Long> {
    // Trova tutti i completamenti per un dato ID giocatore
    List<Completion> findByPlayerId(Long playerId);
}
