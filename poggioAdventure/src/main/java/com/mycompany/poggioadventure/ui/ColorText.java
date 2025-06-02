package com.mycompany.poggioadventure.ui;

import java.awt.Color;

/**
 * Enumerazione per la gestione centralizzata dei colori di testo.
 * 
 * <p>Fornisce mappature tra codici ANSI (per console) e colori Swing (per GUI),
 * mantenendo coerenza visiva tra le diverse interfacce.</p>
 * 
 * <p>Caratteristiche principali:
 * <ul>
 *   <li>Supporto per 8 colori base ANSI + varianti bright</li>
 *   <li>Colori semantici per elementi di gioco (NPC, oggetti, ecc.)</li>
 *   <li>Estesa palette di colori specifici (arancioni, rossi, blu, gialli)</li>
 *   <li>Metodi di accesso per entrambi i sistemi di colore</li>
 * </ul>
 * 
 * @author Strix89
 */
public enum ColorText {
    // ============== COLORI BASE ==============
    RESET("\u001B[0m", Color.WHITE),  // Codice reset ANSI + colore default
    
    // Standard ANSI colors (ISO 6429)
    BLACK("\u001B[30m", Color.BLACK),
    RED("\u001B[31m", new Color(255, 0, 0)),
    GREEN("\u001B[32m", new Color(0, 255, 0)),
    YELLOW("\u001B[33m", new Color(255, 255, 0)),
    BLUE("\u001B[34m", new Color(0, 120, 215)),
    MAGENTA("\u001B[35m", new Color(255, 0, 255)),
    CYAN("\u001B[36m", new Color(0, 255, 255)),
    WHITE("\u001B[37m", Color.WHITE),
    
    // ============== VARIANTI BRIGHT ==============
    BRIGHT_BLACK("\u001B[90m", new Color(100, 100, 100)),
    BRIGHT_RED("\u001B[91m", new Color(255, 50, 50)),
    BRIGHT_GREEN("\u001B[92m", new Color(50, 255, 50)),
    BRIGHT_YELLOW("\u001B[93m", new Color(255, 255, 100)),
    BRIGHT_BLUE("\u001B[94m", new Color(100, 150, 255)),
    BRIGHT_MAGENTA("\u001B[95m", new Color(255, 100, 255)),
    BRIGHT_CYAN("\u001B[96m", new Color(100, 255, 255)),
    BRIGHT_WHITE("\u001B[97m", Color.WHITE),
    
    // ============== SFUMATURE DI ARANCIONE ==============
    ORANGE("\u001B[38;2;255;165;0m", new Color(255, 165, 0)),            // Standard
    DARK_ORANGE("\u001B[38;2;255;140;0m", new Color(255, 140, 0)),       // Scuro
    LIGHT_ORANGE("\u001B[38;2;255;160;122m", new Color(255, 160, 122)),  // Chiaro
    BURNT_ORANGE("\u001B[38;2;177;73;7m", new Color(177, 73, 7)),        // Bruciato
    NEON_ORANGE("\u001B[38;2;255;127;39m", new Color(255, 127, 39)),     // Neon
    
    // ============== SFUMATURE DI ROSSO ==============
    CRIMSON("\u001B[38;2;220;20;60m", new Color(220, 20, 60)),       // Rosso sangue
    MAROON("\u001B[38;2;128;0;0m", new Color(128, 0, 0)),            // Bordeaux
    FIREBRICK("\u001B[38;2;178;34;34m", new Color(178, 34, 34)),     // Rosso mattone
    ROSE("\u001B[38;2;255;192;203m", new Color(255, 192, 203)),      // Rosato

    // ============== SFUMATURE DI BLU ==============
    NAVY("\u001B[38;2;0;0;128m", new Color(0, 0, 128)),              // Blu navy
    SKYBLUE("\u001B[38;2;135;206;235m", new Color(135, 206, 235)),   // Celeste
    TURQUOISE("\u001B[38;2;64;224;208m", new Color(64, 224, 208)),   // Turchese
    INDIGO("\u001B[38;2;75;0;130m", new Color(75, 0, 130)),          // Indaco

    // ============== SFUMATURE DI GIALLO ==============
    GOLD("\u001B[38;2;255;215;0m", new Color(255, 215, 0)),          // Oro
    LEMON("\u001B[38;2;255;250;205m", new Color(255, 250, 205)),     // Limone
    OLIVE("\u001B[38;2;128;128;0m", new Color(128, 128, 0)),         // Oliva
    SUNFLOWER("\u001B[38;2;244;213;64m", new Color(244, 213, 64)),   // Girasole
    
    // ============== SFUMATURE DI ROSA ==============
    PINK("\u001B[38;2;255;192;203m", new Color(255, 192, 203)),      // Rosa standard
    HOT_PINK("\u001B[38;2;255;105;180m", new Color(255, 105, 180)),  // Rosa acceso
    DEEP_PINK("\u001B[38;2;255;20;147m", new Color(255, 20, 147)),   // Rosa intenso
    LIGHT_PINK("\u001B[38;2;255;182;193m", new Color(255, 182, 193)), // Rosa chiaro
    SALMON("\u001B[38;2;250;128;114m", new Color(250, 128, 114)),    // Salmone
    
    // ============== SFUMATURE DI VERDE ==============
    FOREST_GREEN("\u001B[38;2;34;139;34m", new Color(34, 139, 34)),  // Verde foresta
    LIME("\u001B[38;2;50;205;50m", new Color(50, 205, 50)),          // Verde lime
    EMERALD("\u001B[38;2;80;200;120m", new Color(80, 200, 120)),     // Smeraldo
    MINT("\u001B[38;2;152;251;152m", new Color(152, 251, 152)),      // Verde menta
    
    // ============== SFUMATURE DI VIOLA ==============
    PURPLE("\u001B[38;2;128;0;128m", new Color(128, 0, 128)),        // Viola standard
    LAVENDER("\u001B[38;2;230;230;250m", new Color(230, 230, 250)),  // Lavanda
    VIOLET("\u001B[38;2;138;43;226m", new Color(138, 43, 226)),      // Violetto
    PLUM("\u001B[38;2;221;160;221m", new Color(221, 160, 221)),      // Prugna
    
    // ============== COLORI NEUTRI ==============
    GRAY("\u001B[38;2;128;128;128m", new Color(128, 128, 128)),      // Grigio
    SILVER("\u001B[38;2;192;192;192m", new Color(192, 192, 192)),    // Argento
    BEIGE("\u001B[38;2;245;245;220m", new Color(245, 245, 220)),     // Beige
    BROWN("\u001B[38;2;165;42;42m", new Color(165, 42, 42)),         // Marrone
    
    // ============== COLORI SEMANTICI ==============
    NPC("\u001B[36m", new Color(0, 255, 255)),       // Personaggi non giocanti
    ITEM("\u001B[33m", new Color(255, 185, 0)),      // Oggetti di gioco
    WARNING("\u001B[93m", new Color(255, 200, 0)),   // Avvertimenti
    ERROR("\u001B[91m", new Color(255, 80, 80)),     // Messaggi di errore
    SUCCESS("\u001B[92m", new Color(50, 220, 50)),   // Operazioni riuscite
    HIGHLIGHT("\u001B[95m", new Color(255, 150, 255)),// Evidenziazioni
    PLAYER("\u001B[34m", new Color(0, 120, 215)); // Colore per il giocatore

    private final String ansiCode;    // Codice ANSI per terminali
    private final Color swingColor;   // Colore Swing per GUI

    /**
     * Costruttore privato per l'enum.
     * @param ansi Codice ANSI per la console
     * @param swing Colore Swing equivalente
     */
    ColorText(String ansi, Color swing) {
        this.ansiCode = ansi;
        this.swingColor = swing;
    }

    /**
     * Restituisce il codice ANSI per il colore.
     * @return Stringa con il codice di escape ANSI
     */
    public String getANSICode() { 
        return ansiCode; 
    }

    /**
     * Restituisce il colore Swing equivalente.
     * @return Oggetto Color per uso in GUI
     */
    public Color getSwingColor() { 
        return swingColor; 
    }
    
    public static ColorText fromString(String colorName) {
        try {
            return ColorText.valueOf(colorName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return RESET;
        }
    }
}