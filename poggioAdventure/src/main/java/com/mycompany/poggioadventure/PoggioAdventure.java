package com.mycompany.poggioadventure;

import com.mycompany.poggioadventure.ui.UI_Init;
import di.uniba.map.b.adventure.ColorText;
import di.uniba.map.b.adventure.impl.CLIMenu;
import di.uniba.map.b.adventure.impl.CLIOutputHandler;
import java.util.Arrays;

/**
 * Classe principale che avvia l'applicazione PoggioAdventure.
 * Supporta due modalità di esecuzione: interfaccia grafica (GUI) e linea di comando (CLI).
 * 
 * @author Strix89
 */
public class PoggioAdventure {

    /**
     * Metodo main - punto d'ingresso dell'applicazione.
     * @param args argomenti da riga di comando (--gui o --cli)
     */
    public static void main(String[] args) {
        // Controllo validità degli argomenti
        if (args.length > 0 && !args[0].equalsIgnoreCase("--gui") && !args[0].equalsIgnoreCase("--cli")) {
            new CLIOutputHandler().writeln("Argomento non valido. Usa --gui o --cli.", ColorText.RED);
            return;
        }
        
        // Determina la modalità di esecuzione
        boolean guiMode = Arrays.stream(args)
            .anyMatch(arg -> arg.equalsIgnoreCase("--gui"));
        
        // Avvia l'interfaccia appropriata
        if(guiMode) {
            // Modalità GUI (Swing)
            java.awt.EventQueue.invokeLater(() -> {
                UI_Init uiInit = new UI_Init(); // Inizializza la GUI
                uiInit.setVisible(true);         // Mostra la finestra
            });
        } else {
            // Modalità CLI
            new CLIMenu().showMainMenu(); // Avvia il menu testuale
        }
    }
}