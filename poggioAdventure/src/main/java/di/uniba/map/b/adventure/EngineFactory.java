package di.uniba.map.b.adventure;

import di.uniba.map.b.adventure.impl.PoggioAdventure;

/**
 *
 * @author tomma
 */
public class EngineFactory {
    // Crea un Engine per una NUOVA partita
    public static Engine createNewGame(String playerName, OutputHandler output, 
                                     InputHandler input, ErrorHandler errorHandler) throws Exception {
        GameDescription game = new PoggioAdventure();
        game.init(); // Inizializza stanze/oggetti
        return new Engine(game, playerName, output, input, errorHandler);
    }

    // Crea un Engine da un salvataggio ESISTENTE
    public static Engine createFromSave(GameDescription savedGame, String playerName,
                                       OutputHandler output, InputHandler input, 
                                       ErrorHandler errorHandler) {
        return new Engine(savedGame, playerName, output, input, errorHandler);
    }
}