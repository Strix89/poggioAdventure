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
 * Factory per creazione e configurazione di istanze del motore di gioco.
 * 
 * <p>Centralizza la logica di inizializzazione dell'Engine fornendo metodi
 * per creare nuove partite e ripristinare salvataggi esistenti. Gestisce
 * automaticamente la configurazione degli handler e l'adattamento per
 * diverse modalità di interfaccia (CLI vs GUI).
 * 
 * <p><b>Responsabilità:</b>
 * <ul>
 *   <li>Inizializzazione completa del mondo di gioco</li>
 *   <li>Configurazione automatica degli handler</li>
 *   <li>Ripristino stato da salvataggi serializzati</li>
 *   <li>Adattamento interfaccia per CLI/GUI</li>
 * </ul>
 * 
 * <p><b>Pattern implementati:</b>
 * <ul>
 *   <li>Factory Method: creazione standardizzata di Engine</li>
 *   <li>Dependency Injection: configurazione handler esterni</li>
 *   <li>Template Method: struttura comune per inizializzazione</li>
 * </ul>
 */
public class EngineFactory {

    /**
     * Crea nuovo motore di gioco con inizializzazione completa del mondo.
     * Configura automaticamente le immagini NPC in base al tipo di output.
     * 
     * @param playerName Nome del giocatore per personalizzazione
     * @param output Handler per visualizzazione messaggi e interfaccia
     * @param input Handler per raccolta input utente
     * @param errorHandler Gestore centralizzato degli errori
     * @param logger Sistema di logging per debug e tracciamento
     * @return Engine completamente configurato per nuova partita
     * @throws Exception se l'inizializzazione del mondo di gioco fallisce
     */
    public static Engine createNewGame(String playerName, OutputHandler output, 
                                     InputHandler input, ErrorHandler errorHandler, 
                                     LoggerInput logger) throws Exception {
        // Inizializzazione completa del mondo di gioco
        GameDescription game = new PoggioAdventureDesc();
        game.init();
        
        // Configurazione immagini NPC in base al tipo di interfaccia
        if (output instanceof CLIOutputHandler){
            game.getGameMap().alterateNPCImages(true); // Usa ASCII art per CLI
        }
        
        return new Engine(game, playerName, output, input, errorHandler, logger);
    }

    /**
     * Ripristina motore di gioco da stato serializzato con configurazione adattiva.
     * Gestisce la riconfigurazione degli handler e il ripristino del tempo di gioco.
     * 
     * @param savedGame Stato del gioco deserializzato da file di salvataggio
     * @param playerName Nome del giocatore (può differire dal salvataggio originale)
     * @param output Handler per output configurato per la sessione corrente
     * @param input Handler per input della sessione corrente
     * @param errorHandler Gestore errori per la sessione corrente
     * @param logger Logger per tracciamento della sessione ripristinata
     * @param gameTime Tempo di gioco accumulato da ripristinare (millisecondi)
     * @return Engine configurato con stato ripristinato e handler aggiornati
     */
    public static Engine createFromSave(GameDescription savedGame, String playerName,
                                       OutputHandler output, InputHandler input, 
                                       ErrorHandler errorHandler, LoggerInput logger, long gameTime) {
        // Creazione engine con flag di ripristino da salvataggio
        Engine gameEngine = new Engine(savedGame, playerName, output, input, errorHandler, logger, true);
        
        // Riconfigurazione adattiva interfaccia NPC
        if (output instanceof CLIOutputHandler) {
            savedGame.getGameMap().alterateNPCImages(true);  // ASCII per CLI
        } else{ 
            savedGame.getGameMap().alterateNPCImages(false); // Immagini per GUI
        }
        
        // Ripristino tempo di gioco accumulato
        gameEngine.setGameTime(gameTime);
        
        return gameEngine;
    }
}