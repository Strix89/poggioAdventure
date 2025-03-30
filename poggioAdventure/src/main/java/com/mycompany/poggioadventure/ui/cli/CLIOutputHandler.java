package com.mycompany.poggioadventure.ui.cli;

import com.mycompany.poggioadventure.ui.ColorText;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import com.mycompany.poggioadventure.ui.OutputHandler;

/**
 * Implementazione CLI dell'OutputHandler che gestisce l'output a console.
 * Fornisce funzionalità per:
 * - Scrittura di testo con colori ANSI
 * - Pulizia della console
 * - Gestione corretta dell'encoding UTF-8
 * 
 * @author Strix89
 */
public class CLIOutputHandler implements OutputHandler {
    
    /**
     * Blocco di inizializzazione statico che configura System.out
     * per usare l'encoding UTF-8 e auto-flush.
     */
    static {
        System.setOut(new PrintStream(
            new FileOutputStream(FileDescriptor.out), 
            true, 
            StandardCharsets.UTF_8
        ));
    }

    /**
     * Scrive un messaggio sulla console senza newline finale.
     * Nota: in questa implementazione base il parametro color non viene utilizzato.
     * 
     * @param message Il messaggio da stampare
     * @param color Il colore del testo (non utilizzato)
     */
    @Override
    public void write(String message, ColorText color) {
        System.out.print(message);
    }

    /**
     * Scrive un messaggio sulla console con newline finale e colore ANSI.
     * Aggiunge automaticamente il codice di reset del colore dopo il messaggio.
     * 
     * @param message Il messaggio da stampare
     * @param color Il colore del testo da applicare
     */
    @Override
    public void writeln(String message, ColorText color) {
        System.out.println(color.getANSICode() + message + ColorText.RESET.getANSICode());
    }
    
    /**
     * Scrive una linea vuota sulla console.
     */
    @Override
    public void writeln() {
        System.out.println();
    }

    /**
     * Pulisce la console utilizzando codici di controllo ANSI.
     * Compatibile con la maggior parte dei terminali moderni.
     */
    @Override
    public void clear() {
        // Codice ANSI per: 
        // \033[H - Sposta il cursore in home position (0,0)
        // \033[2J - Cancella l'intero schermo
        System.out.print("\033[H\033[2J");
        System.out.flush(); // Assicura l'output immediato
    }
}