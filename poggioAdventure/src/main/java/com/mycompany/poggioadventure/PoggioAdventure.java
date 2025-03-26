package com.mycompany.poggioadventure;
import com.mycompany.poggioadventure.ui.UI_Init;
import di.uniba.map.b.adventure.ColorText;
import di.uniba.map.b.adventure.impl.CLIMenu;
import di.uniba.map.b.adventure.impl.CLIOutputHandler;
import java.util.Arrays;

/**
 *
 * @author Strix89
 */
public class PoggioAdventure {
    public static void main(String[] args) {
         if (args.length > 0 && !args[0].equalsIgnoreCase("--gui") && !args[0].equalsIgnoreCase("--cli")) {
            new CLIOutputHandler().writeln("Argomento non valido. Usa --gui o --cli.", ColorText.RED);
            return;
        }
        // Determina la modalità in base agli argomenti
        boolean guiMode = Arrays.stream(args)
            .anyMatch(arg -> arg.equalsIgnoreCase("--gui"));
        if(guiMode) {
            java.awt.EventQueue.invokeLater(() -> {
                UI_Init uiInit = new UI_Init();
                uiInit.setVisible(true);
            });
        } else {
            new CLIMenu().showMainMenu();
        }
    }
}