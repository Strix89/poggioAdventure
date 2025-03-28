package di.uniba.map.b.adventure;

import di.uniba.map.b.adventure.impl.PoggioAdventure;
import di.uniba.map.b.adventure.parser.LoggerInput;

/**
 *
 * @author tomma
 */
public class EngineFactory {
    // Crea un Engine per una NUOVA partita
    public static Engine createNewGame(String playerName, OutputHandler output, 
                                     InputHandler input, ErrorHandler errorHandler, LoggerInput logger) throws Exception {
        GameDescription game = new PoggioAdventure();
        game.init(); // Inizializza stanze/oggetti
        return new Engine(game, playerName, output, input, errorHandler, logger);
    }

    // Crea un Engine da un salvataggio ESISTENTE
    public static Engine createFromSave(GameDescription savedGame, String playerName,
                                       OutputHandler output, InputHandler input, 
                                       ErrorHandler errorHandler, LoggerInput logger, TimeManager time, long gameTime) {
        Engine gameEngine = new Engine(savedGame, playerName, output, input, errorHandler, logger);
        gameEngine.setTimeManager(time);
        gameEngine.setGameTime(gameTime);
        return gameEngine;
    }
}