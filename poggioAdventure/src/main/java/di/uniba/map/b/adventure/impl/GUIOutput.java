package di.uniba.map.b.adventure.impl;

import di.uniba.map.b.adventure.ColorText;
import di.uniba.map.b.adventure.FlowOutput;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Strix89
 */
public class GUIOutput implements FlowOutput{
   private final JTextPane outputPane;

    public GUIOutput(JTextPane outputPane) {
        this.outputPane = outputPane;
    }

    @Override
    public void write(String message, ColorText color) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = outputPane.getStyledDocument();
                Style style = outputPane.addStyle("ColorStyle", null);
                StyleConstants.setForeground(style, color.getSwingColor());
                doc.insertString(doc.getLength(), message, style);
            } catch (BadLocationException ex) {
                JOptionPane.showMessageDialog(null,
                    "Errore critico nella stampa del gioco: " + ex.getMessage(),
                    "Errore", JOptionPane.ERROR_MESSAGE);  // Mostra un messaggio di errore
            }
        });
    }
    
    @Override
    public void writeln(String message, ColorText color) {
        write(message + "\n", color);
    }
    
    @Override
    public void writeln() {
        write("\n", ColorText.RESET);
    }
}
