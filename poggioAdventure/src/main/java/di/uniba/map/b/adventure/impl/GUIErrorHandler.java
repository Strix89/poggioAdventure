package di.uniba.map.b.adventure.impl;

import di.uniba.map.b.adventure.ErrorHandler;
import javax.swing.JOptionPane;

/**
 *
 * @author Strix89
 */
public class GUIErrorHandler implements ErrorHandler {
    @Override
    public void handleFatalError(String message, Throwable ex) {
        JOptionPane.showMessageDialog(null,
            message + "\nDettagli: " + ex.getMessage(),
            "Errore Fatale", 
            JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void handleRecoverableError(String message) {
        JOptionPane.showMessageDialog(null,
            message,
            "Errore",
            JOptionPane.WARNING_MESSAGE);
    }
}
