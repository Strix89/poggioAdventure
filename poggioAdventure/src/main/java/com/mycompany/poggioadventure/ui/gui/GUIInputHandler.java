package com.mycompany.poggioadventure.ui.gui;

import com.mycompany.poggioadventure.ui.InputHandler;
import javax.swing.JTextField;

/**
 * Implementazione GUI dell'interfaccia InputHandler che gestisce l'input dell'utente
 * attraverso un campo di testo Swing (JTextField).
 * 
 * <p>Questa classe si occupa di:
 * <ul>
 *   <li>Ricevere l'input da un componente grafico</li>
 *   <li>Pulire l'input rimuovendo spazi bianchi iniziali/finali</li>
 *   <li>Fornire l'input al sistema di gioco</li>
 * </ul>
 * 
 * @author Strix89
 */
public class GUIInputHandler implements InputHandler {
    
    /**
     * Campo di testo Swing da cui ricevere l'input dell'utente.
     * Viene passato al costruttore e mantenuto come riferimento finale.
     */
    private final JTextField commandInputField;

    /**
     * Costruttore che inizializza l'handler con il campo di testo specificato.
     * 
     * @param commandInputField Il componente JTextField da cui leggere l'input
     * @throws IllegalArgumentException Se commandInputField è null
     */
    public GUIInputHandler(JTextField commandInputField) {
        if (commandInputField == null) {
            throw new IllegalArgumentException("Il campo di testo non può essere null");
        }
        this.commandInputField = commandInputField;
    }

    /**
     * Recupera l'input dell'utente dal campo di testo associato.
     * Il testo viene ripulito rimuovendo gli spazi bianchi iniziali e finali.
     * 
     * @return La stringa inserita dall'utente, senza spazi iniziali/finali
     */
    @Override
    public String getInput() {
        return commandInputField.getText().trim();
    }
}