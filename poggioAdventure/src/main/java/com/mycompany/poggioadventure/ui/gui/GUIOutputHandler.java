package com.mycompany.poggioadventure.ui.gui;

import com.mycompany.poggioadventure.ui.ColorText;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import com.mycompany.poggioadventure.ui.OutputHandler;

/**
 * Implementazione GUI dell'interfaccia OutputHandler che gestisce l'output del gioco
 * attraverso un componente JTextPane Swing.
 * 
 * <p>Questa classe permette di:
 * <ul>
 *   <li>Visualizzare testo formattato con colori</li>
 *   <li>Gestire l'output in modo thread-safe</li>
 *   <li>Pulire l'area di output</li>
 *   <li>Mostrare messaggi su nuove linee</li>
 * </ul>
 * 
 * @author Strix89
 */
public class GUIOutputHandler implements OutputHandler {
    
    /**
     * Componente JTextPane che mostra l'output del gioco.
     * Viene inizializzato nel costruttore e non può essere modificato.
     */
    private final JTextPane outputPane;

    /**
     * Costruttore che inizializza l'handler con il componente di output specificato.
     * 
     * @param outputPane Il componente JTextPane su cui visualizzare l'output
     * @throws IllegalArgumentException Se outputPane è null
     */
    public GUIOutputHandler(JTextPane outputPane) {
        if (outputPane == null) {
            throw new IllegalArgumentException("Il componente di output non può essere null");
        }
        this.outputPane = outputPane;
    }

    /**
     * Scrive un messaggio nell'area di output con il colore specificato.
     * L'operazione viene eseguita in modo thread-safe tramite SwingUtilities.
     * 
     * @param message Il messaggio da visualizzare
     * @param color Il colore del testo (vedi ColorText)
     */
    @Override
    public void write(String message, ColorText color) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = outputPane.getStyledDocument();
                Style style = outputPane.addStyle("ColorStyle", null);
                StyleConstants.setForeground(style, color.getSwingColor());
                doc.insertString(doc.getLength(), message, style);
                
                // Auto-scroll to bottom
                outputPane.setCaretPosition(doc.getLength());
            } catch (BadLocationException ex) {
                JOptionPane.showMessageDialog(null,
                    "Errore critico nella stampa del gioco: " + ex.getMessage(),
                    "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    /**
     * Scrive un messaggio seguito da un newline nell'area di output.
     * 
     * @param message Il messaggio da visualizzare
     * @param color Il colore del testo (vedi ColorText)
     */
    @Override
    public void writeln(String message, ColorText color) {
        write(message + "\n", color);
    }
    
    /**
     * Scrive una linea vuota nell'area di output.
     */
    @Override
    public void writeln() {
        write("\n", ColorText.RESET);
    }

    /**
     * Pulisce completamente l'area di output.
     * L'operazione viene eseguita in modo thread-safe tramite SwingUtilities.
     */
    @Override
    public void clear() {
        SwingUtilities.invokeLater(() -> {
            outputPane.setText("");
        });
    }
}