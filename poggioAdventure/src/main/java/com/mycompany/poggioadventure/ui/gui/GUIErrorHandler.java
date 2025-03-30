package com.mycompany.poggioadventure.ui.gui;

import com.mycompany.poggioadventure.ui.ErrorHandler;
import javax.swing.JOptionPane;

/**
 * Implementazione grafica (GUI) dell'interfaccia ErrorHandler.
 * Gestisce la visualizzazione degli errori attraverso finestre di dialogo Swing.
 * 
 * <p>Questa classe differenzia tra:
 * <ul>
 *   <li>Errori fatali (visualizzati come ERROR_MESSAGE)</li>
 *   <li>Errori recuperabili (visualizzati come WARNING_MESSAGE)</li>
 * </ul>
 * 
 * @author Strix89
 */
public class GUIErrorHandler implements ErrorHandler {
    
    /**
     * Gestisce un errore fatale mostrando una finestra di dialogo modale.
     * Includa sia il messaggio principale che i dettagli dell'eccezione.
     * 
     * @param message Il messaggio di errore principale
     * @param ex L'eccezione associata all'errore (può essere null)
     */
    @Override
    public void handleFatalError(String message, Throwable ex) {
        String details = (ex != null) ? ex.getMessage() : "Nessun dettaglio disponibile";
        JOptionPane.showMessageDialog(
            null, // Componente padre (null = centro schermo)
            message + "\nDettagli: " + details,
            "Errore Fatale", 
            JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Gestisce un errore recuperabile mostrando una finestra di dialogo modale.
     * Utilizza un'icona di warning per differenziarlo dagli errori fatali.
     * 
     * @param message Il messaggio di errore/warning da visualizzare
     */
    @Override
    public void handleRecoverableError(String message) {
        JOptionPane.showMessageDialog(
            null, // Componente padre (null = centro schermo)
            message,
            "Errore",
            JOptionPane.WARNING_MESSAGE
        );
    }
}