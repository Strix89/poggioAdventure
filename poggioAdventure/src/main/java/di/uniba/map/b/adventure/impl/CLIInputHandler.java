package di.uniba.map.b.adventure.impl;

import di.uniba.map.b.adventure.InputHandler;
import java.util.Scanner;

/**
 *
 * @author Strix89
 */
public class CLIInputHandler implements InputHandler {
    private final Scanner scanner;

    public CLIInputHandler() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public String getInput() {
        return scanner.nextLine().trim();
    }
}
