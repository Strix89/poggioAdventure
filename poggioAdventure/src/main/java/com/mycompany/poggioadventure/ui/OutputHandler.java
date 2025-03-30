package com.mycompany.poggioadventure.ui;

/**
 * Interfaccia per la gestione dell'output di gioco su console e interfaccia grafica.
 * 
 * <p>Fornisce un contratto unificato per la visualizzazione di messaggi:
 * <ul>
 *   <li>Su terminale/console (CLI)</li>
 *   <li>In interfaccia grafica (GUI)</li>
 * </ul>
 * 
 * <p>Implementazioni tipiche:
 * <ul>
 *   <li>{@code CLIOutputHandler} - output a console</li>
 *   <li>{@code GUIOutputHandler} - output in componenti grafici</li>
 * </ul>
 * 
 * @author Strix89
 */
public interface OutputHandler {

    /**
     * Scrive un messaggio senza andare a capo.
     * 
     * @param message Il messaggio da visualizzare 
     * @param color Il colore del testo
     * 
     */
    void write(String message, ColorText color);

    /**
     * Scrive un messaggio andando a capo.
     * 
     * @param message Il messaggio da visualizzare
     * @param color Il colore del testo 
     * 
     */
    void writeln(String message, ColorText color);

    /**
     * Va semplicemente a capo.
     * 
     * <p>Utile per creare spaziature nell'output.
     */
    void writeln();

    /**
     * Pulisce l'area di output.
     * 
     * <p>Comportamento specifico:
     * <ul>
     *   <li>Su CLI: pulisce la console</li>
     *   <li>Su GUI: svuota l'area di testo</li>
     * </ul>
     */
    void clear();
}