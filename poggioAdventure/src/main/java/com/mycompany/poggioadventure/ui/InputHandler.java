package com.mycompany.poggioadventure.ui;

/**
 * Interfaccia per la gestione dell'input dell'utente nel gioco.
 * 
 * <p>Definisce un contratto unificato per la lettura dell'input indipendentemente
 * dall'origine (console, GUI, rete, ecc.).</p>
 * 
 * <p>Implementazioni tipiche:
 * <ul>
 *   <li>{@code CLIInputHandler} - per input da terminale/console</li>
 *   <li>{@code GUIInputHandler} - per input da interfaccia grafica</li>
 * </ul>
 * 
 * @author Strix89
 */
public interface InputHandler {
    
    /**
     * Acquisisce l'input dall'utente.
     * 
     * @return La stringa inserita dall'utente, mai null
     * 
     */
    String getInput();
}