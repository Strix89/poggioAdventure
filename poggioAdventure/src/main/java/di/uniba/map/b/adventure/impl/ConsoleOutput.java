package di.uniba.map.b.adventure.impl;
import di.uniba.map.b.adventure.ColorText;
import di.uniba.map.b.adventure.FlowOutput;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Strix89
 */
public class ConsoleOutput implements FlowOutput{
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
}
