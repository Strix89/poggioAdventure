package di.uniba.map.b.adventure.impl;

import di.uniba.map.b.adventure.ColorText;
import di.uniba.map.b.adventure.Engine;
import di.uniba.map.b.adventure.EngineFactory;
import di.uniba.map.b.adventure.OutputHandler;
import di.uniba.map.b.adventure.InputHandler;
import di.uniba.map.b.adventure.SaveGame;
import di.uniba.map.b.adventure.MenuManager;
import di.uniba.map.b.adventure.Utils;
import java.util.List;
import java.util.logging.Level;

public class CLIMenu implements MenuManager {
    private final OutputHandler output;
    private final InputHandler scanner;

    public CLIMenu() {
        this.output = new CLIOutputHandler();
        this.scanner = new CLIInputHandler();
    }

    @Override
    public void showMainMenu() {
        output.writeln("\n=== POGGIO ADVENTURE ===", ColorText.NAVY);
        
        while(true) {
            output.writeln("\nMenu Principale:", ColorText.WHITE);
            output.writeln("1) Nuova Partita", ColorText.WHITE);
            output.writeln("2) Carica Partita", ColorText.WHITE);
            output.writeln("3) Classifica", ColorText.WHITE);
            output.writeln("4) Esci", ColorText.WHITE);
            output.write("\nSeleziona un'opzione: ", ColorText.WHITE);

            String choice = scanner.getInput();
            switch(choice) {
                case "1" -> showNewGame();
                case "2" -> showLoadGame();
                case "3" -> showRanking();
                case "4" -> { exit(); }
                default -> output.writeln("Opzione non valida", ColorText.ERROR);
            }
        }
    }

    @Override
    public void showNewGame() {
        output.write("\nInserisci nome giocatore: ", ColorText.WHITE);
        String name = scanner.getInput();
        
        CLIErrorHandler error = new CLIErrorHandler();
        try {
            Engine engine;
            engine = EngineFactory.createNewGame(name, output, scanner, error);
            // Avviare il ciclo di gioco
            engine.startGameLoop();
        } catch (Exception ex) {
            error.handleFatalError("Errore critico durante l'inizializzazione della partita", ex);
        }
    }

    @Override
    public void showLoadGame() {
        List<String> saves = SaveGame.getSaveList();

        if (saves.isEmpty()) {
            output.writeln("Nessun salvataggio disponibile", ColorText.ORANGE);
            return;
        }

        // Visualizza i salvataggi disponibili con un numero
        output.writeln("\n=== SALVATAGGI DISPONIBILI ===", ColorText.YELLOW);
        for (int i = 0; i < saves.size(); i++) {
            // Assegna un numero al salvataggio
            output.writeln((i + 1) + ") " + saves.get(i), ColorText.WHITE);
        }

        output.write("\nSeleziona il numero del salvataggio da caricare: ", ColorText.WHITE);
        String input = scanner.getInput().trim();

        try {
            // Converte l'input dell'utente in un numero
            int selectedIndex = Integer.parseInt(input) - 1;  // Sottrai 1 per avere un indice a partire da 0
            if (selectedIndex >= 0 && selectedIndex < saves.size()) {
                String saveName = saves.get(selectedIndex);  // Ottieni il nome del salvataggio

                // Utilizza il metodo loadSave con callback
                SaveGame.loadSave(saveName, 
                    // Success callback
                    engine -> {
                        if (engine != null) {
                            engine.startGameLoop();  // Avvia il ciclo di gioco per il salvataggio caricato
                        }
                    }, 
                    // Error callback
                    error -> {
                        output.writeln(error, ColorText.ERROR);  // Mostra un errore in caso di problemi nel caricamento
                    }, new CLIErrorHandler(), scanner, output);
            } else {
                output.writeln("Numero di salvataggio non valido.", ColorText.ERROR);
            }
        } catch (NumberFormatException e) {
            output.writeln("Inserisci un numero valido.", ColorText.ERROR);
        }
    }


    @Override
    public void showRanking() {
        // Implementa logica classifica
        output.writeln("\n=== CLASSIFICA ===", ColorText.YELLOW);
        output.writeln("Funzionalità in sviluppo...", ColorText.ORANGE);
    }

    @Override
    public void exit() {
        Utils.exitApplication();
    }
}
