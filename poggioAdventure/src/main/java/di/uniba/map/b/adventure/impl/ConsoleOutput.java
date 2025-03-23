package di.uniba.map.b.adventure.impl;

import di.uniba.map.b.adventure.FlowOutput;

/**
 *
 * @author tomma
 */
public class ConsoleOutput implements FlowOutput{
    @Override
    public void write(String message) {
        System.out.print(message);
    }

    @Override
    public void writeln(String message) {
        System.out.println(message);
    }
    
    @Override
    public void writeln() {
        System.out.println();
    }
}
