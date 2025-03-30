package com.mycompany.poggioadventure.ui.cli;

import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.persistence.SaveGame;
import com.mycompany.poggioadventure.core.Engine;
import com.mycompany.poggioadventure.core.utils.EngineFactory;
import com.mycompany.poggioadventure.core.abstracts.MenuManager;
import com.mycompany.poggioadventure.ui.InputHandler;
import com.mycompany.poggioadventure.ui.OutputHandler;
import di.uniba.map.b.adventure.*;
import com.mycompany.poggioadventure.persistence.LoggerInput;
import java.util.List;

/**
 * Implementazione CLI del gestore dei menu del gioco.
 * Gestisce tutte le interazioni a menu tramite interfaccia a riga di comando,
 * inclusi menu principale, nuova partita, caricamento e classifica.
 * 
 * @author Strix89
 */
public class CLIMenu implements MenuManager {
    
    /**
     * Handler per l'output a console
     */
    private final OutputHandler output;
    
    /**
     * Handler per l'input da console
     */
    private final InputHandler scanner;

    /**
     * Costruttore che inizializza gli handler per input/output CLI
     */
    public CLIMenu() {
        this.output = new CLIOutputHandler();
        this.scanner = new CLIInputHandler();
    }

    /**
     * Mostra il menu principale con opzioni per:
     * - Nuova partita
     * - Carica partita
     * - Classifica
     * - Uscita
     * 
     * Il menu viene visualizzato in loop fino alla selezione di uscita
     */
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

    /**
     * Gestisce la creazione di una nuova partita.
     * Richiede il nome del giocatore e inizializza il motore di gioco.
     */
    @Override
    public void showNewGame() {
        output.write("\nInserisci nome giocatore: ", ColorText.WHITE);
        String name = scanner.getInput();
        
        CLIErrorHandler error = new CLIErrorHandler();
        try {
            Engine engine = EngineFactory.createNewGame(
                name, 
                output, 
                scanner, 
                error, 
                new LoggerInput(error)
            );
            engine.startGameLoop();
        } catch (Exception ex) {
            error.handleFatalError("Errore critico durante l'inizializzazione della partita", ex);
        }
    }

    /**
     * Mostra la lista dei salvataggi disponibili e gestisce:
     * - Selezione salvataggio da caricare
     * - Eliminazione salvataggi (con prefisso !)
     * - Messaggi di errore per input non validi
     */
    @Override
    public void showLoadGame() {
        List<String> saves = SaveGame.getSaveList();

        if (saves.isEmpty()) {
            output.writeln("Nessun salvataggio disponibile", ColorText.ORANGE);
            return;
        }

        output.writeln("\n=== SALVATAGGI DISPONIBILI ===", ColorText.YELLOW);
        for (int i = 0; i < saves.size(); i++) {
            output.writeln((i + 1) + ") " + saves.get(i), ColorText.WHITE);
        }

        output.write("\nSeleziona salvataggio (es: 2) o !numero per eliminare (es: !2): ", ColorText.WHITE);
        String input = scanner.getInput().trim();

        try {
            if (input.startsWith("!")) {
                // Modalità eliminazione
                int selectedIndex = Integer.parseInt(input.substring(1)) - 1;
                if (selectedIndex >= 0 && selectedIndex < saves.size()) {
                    String saveName = saves.get(selectedIndex);
                    if (SaveGame.deleteSave(saveName)) {
                        output.writeln("Salvataggio eliminato: " + saveName, ColorText.GREEN);
                        showLoadGame(); // Ricarica la lista
                    } else {
                        output.writeln("Errore eliminazione", ColorText.ERROR);
                    }
                } else {
                    output.writeln("Numero non valido", ColorText.ERROR);
                }
            } else {
                // Modalità caricamento normale
                int selectedIndex = Integer.parseInt(input) - 1;
                if (selectedIndex >= 0 && selectedIndex < saves.size()) {
                    SaveGame.loadSave(
                        saves.get(selectedIndex), 
                        engine -> engine.startGameLoop(),
                        error -> output.writeln(error, ColorText.ERROR),
                        new CLIErrorHandler(), 
                        scanner, 
                        output
                    );
                } else {
                    output.writeln("Numero non valido", ColorText.ERROR);
                }
            }
        } catch (NumberFormatException e) {
            output.writeln("Errore input non valido", ColorText.ERROR);
        }
    }

    /**
     * Mostra la classifica (attualmente in sviluppo)
     */
    @Override
    public void showRanking() {
        output.writeln("\n=== CLASSIFICA ===", ColorText.YELLOW);
        output.writeln("Funzionalità in sviluppo...", ColorText.ORANGE);
    }

    /**
     * Chiude l'applicazione
     */
    @Override
    public void exit() {
        Utils.exitApplication();
    }
}