package com.mycompany.poggioadventure.ui.cli;

import com.mycompany.poggioadventure.ui.InputHandler;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Implementazione CLI per acquisizione input utente da console standard.
 * 
 * <p>Gestisce input testuale tramite System.in con Scanner dedicato per
 * interfacce a riga di comando. Fornisce lettura robusta con gestione
 * errori per stream closure e interruzioni input.
 * 
 * <p><b>Caratteristiche:</b>
 * <ul>
 *   <li>Singleton Scanner per riutilizzo efficiente risorsa</li>
 *   <li>Trimming automatico whitespace per input pulito</li>
 *   <li>Gestione EOF e stream closure con eccezioni appropriate</li>
 *   <li>Validation stream availability prima di lettura</li>
 * </ul>
 * 
 * <p><b>Pattern:</b> Adapter per standardizzare input console,
 * Strategy per implementazione specifica CLI dell'InputHandler.
 */
public class CLIInputHandler implements InputHandler {
    
    /** Scanner singleton per lettura efficiente da System.in */
    private final Scanner scanner;

    /** Inizializza Scanner dedicato per standard input console */
    public CLIInputHandler() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Acquisisce linea input da console con validation e sanitization.
     * Verifica availability stream e gestisce condizioni error gracefully.
     * 
     * @return Input utente sanitizzato (trimmed whitespace)
     * @throws RuntimeException Se stream chiuso o input interrotto
     */
    @Override
    public String getInput() {
        try {
            if (scanner.hasNextLine()) {
                return scanner.nextLine().trim();
            } else {
                throw new RuntimeException("Input stream closed");
            }
        } catch (NoSuchElementException e) {
            throw new RuntimeException("Input interrupted", e);
        }
    }
}