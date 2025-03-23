package di.uniba.map.b.adventure.impl;

import di.uniba.map.b.adventure.FlowOutput;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author tomma
 */
public class GUIOutput implements FlowOutput{
   private final JTextArea outputArea;

    public GUIOutput(JTextArea outputArea) {
        this.outputArea = outputArea;
    }

    @Override
    public void write(String message) {
        SwingUtilities.invokeLater(() -> outputArea.append(message));
    }

    @Override
    public void writeln() {
        SwingUtilities.invokeLater(() -> outputArea.append("\n"));
    }
    
    @Override
    public void writeln(String message) {
        SwingUtilities.invokeLater(() -> outputArea.append(message + "\n"));
    }  
}
