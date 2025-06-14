package com.mycompany.poggioadventure.ui;

import java.util.regex.Pattern;

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
    
    static final Pattern COLOR_BLOCK_PATTERN = Pattern.compile("\\[([A-Za-z_]+)\\](.*?)\\[/\\]", Pattern.CASE_INSENSITIVE);
    static final Pattern IMAGE_TAG_PATTERN = Pattern.compile("^IMAGE:.*$", Pattern.MULTILINE);

    /**
     * Scrive un messaggio senza andare a capo.
     * 
     * @param message Il messaggio da visualizzare 
     * @param color Il colore del testo
     * 
     */
    void write(String message, ColorText color);
    
    void write(String message);

    /**
     * Scrive un messaggio andando a capo.
     * 
     * @param message Il messaggio da visualizzare
     * @param color Il colore del testo 
     * 
     */
    void writeln(String message, ColorText color);
    
    void writeln(String message);

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
    
    default void writeFormatted(String message) {
        writeFormatted(message, ColorText.RESET);
    }

    void writeFormatted(String formattedMessage, ColorText baseColor);
}