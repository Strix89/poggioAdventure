package com.mycompany.poggioadventure.ui.cli;

import com.mycompany.poggioadventure.ui.InputHandler;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Implementazione concreta dell'interfaccia InputHandler per interfaccia a riga di comando (CLI).
 * Gestisce l'input dell'utente tramite System.in utilizzando la classe Scanner.
 * 
 * @author Strix89
 */
public class CLIInputHandler implements InputHandler {
    
    /**
     * Scanner per la lettura dell'input da console.
     * Viene inizializzato una sola volta nel costruttore e riutilizzato.
     */
    private final Scanner scanner;

    /**
     * Costruttore che inizializza lo Scanner per leggere da System.in.
     * System.in rappresenta lo standard input (tipicamente la tastiera).
     */
    public CLIInputHandler() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Legge una linea di input dall'utente e la restituisce dopo aver rimosso
     * gli spazi bianchi iniziali e finali.
     * 
     * @return Stringa inserita dall'utente, senza spazi iniziali/finali
     */
    @Override
    public String getInput() {
        try {
            if (scanner.hasNextLine()) {
                return scanner.nextLine().trim();
            } else {
                // Stream chiuso o EOF
                throw new RuntimeException("Input stream closed");
            }
        } catch (NoSuchElementException e) {
            // Gestisce la chiusura improvvisa dell'input
            throw new RuntimeException("Input interrupted", e);
        }
    }
}