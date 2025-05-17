package com.mycompany.poggioadventure.core.utils;

import com.mycompany.poggioadventure.core.Engine;
import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.ui.InputHandler;
import com.mycompany.poggioadventure.ui.OutputHandler;
import com.mycompany.poggioadventure.ui.ErrorHandler;
import com.mycompany.poggioadventure.core.PoggioAdventureDesc;
import com.mycompany.poggioadventure.persistence.LoggerInput;
import com.mycompany.poggioadventure.ui.cli.CLIOutputHandler;

/**
 * Factory per la creazione di istanze del motore di gioco (Engine).
 * 
 * <p>Responsabilità principali:
 * <ul>
 *   <li>Creazione di nuove partite con stato iniziale</li>
 *   <li>Ripristino di partite salvate</li>
 *   <li>Configurazione degli handler essenziali</li>
 * </ul>
 * 
 * <p>Pattern utilizzati:
 * <ul>
 *   <li>Factory Method (per la creazione di Engine)</li>
 *   <li>Dependency Injection</li>
 * </ul>
 * 
 * @author Strix89
 */
public class EngineFactory {

    /**
     * Crea un nuovo motore di gioco per una partita iniziale.
     * 
     * @param playerName Nome del giocatore
     * @param output Handler per l'output di gioco
     * @param input Handler per l'input del giocatore
     * @param errorHandler Gestore degli errori
     * @param logger Logger per tracciamento attività
     * @return Engine configurato per una nuova partita
     * @throws Exception Se l'inizializzazione del gioco fallisce
     */
    public static Engine createNewGame(String playerName, OutputHandler output, 
                                     InputHandler input, ErrorHandler errorHandler, 
                                     LoggerInput logger) throws Exception {
        // Inizializza la descrizione del gioco
        GameDescription game = new PoggioAdventureDesc();
        game.init(); // Configura stanze, oggetti e stato iniziale
        if (output instanceof CLIOutputHandler){
            game.getGameMap().alterateNPCImages(true);
        }
        return new Engine(game, playerName, output, input, errorHandler, logger);
    }

    /**
     * Crea un motore di gioco da un salvataggio esistente.
     * 
     * @param savedGame Stato del gioco salvato
     * @param playerName Nome del giocatore
     * @param output Handler per l'output di gioco
     * @param input Handler per l'input del giocatore
     * @param errorHandler Gestore degli errori
     * @param logger Logger per tracciamento attività
     * @param time Gestore del tempo di gioco
     * @param gameTime Tempo trascorso nella partita salvata (in millisecondi)
     * @return Engine configurato con lo stato salvato
     */
    public static Engine createFromSave(GameDescription savedGame, String playerName,
                                       OutputHandler output, InputHandler input, 
                                       ErrorHandler errorHandler, LoggerInput logger, 
                                       TimeManager time, long gameTime) {
        // Crea l'engine con lo stato salvato
        Engine gameEngine = new Engine(savedGame, playerName, output, input, errorHandler, logger);
        if (output instanceof CLIOutputHandler) {
            savedGame.getGameMap().alterateNPCImages(true);
        } else{ 
            savedGame.getGameMap().alterateNPCImages(false);
        }
        // Ripristina lo stato temporale
        gameEngine.setTimeManager(time);
        gameEngine.setGameTime(gameTime);
        
        return gameEngine;
    }
}