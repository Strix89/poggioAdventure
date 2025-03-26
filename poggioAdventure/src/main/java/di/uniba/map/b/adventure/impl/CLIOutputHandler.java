package di.uniba.map.b.adventure.impl;
import di.uniba.map.b.adventure.ColorText;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import di.uniba.map.b.adventure.OutputHandler;

/**
 *
 * @author Strix89
 */
public class CLIOutputHandler implements OutputHandler{
    static{
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
    }
    @Override
    public void write(String message, ColorText color) {
        System.out.print(message);
    }

    @Override
    public void writeln(String message, ColorText color) {
        System.out.println(color.getANSICode() + message + ColorText.RESET.getANSICode());
    }
    
    @Override
    public void writeln() {
        System.out.println();
    }

    @Override
    public void clear() {
        // Codice ANSI per pulire la console
        System.out.print("\033[H\033[2J");
        System.out.flush(); // Assicura l'output immediatos
    }
}
