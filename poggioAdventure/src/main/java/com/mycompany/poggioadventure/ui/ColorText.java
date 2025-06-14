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
    DARK_ORANGE("\u001B[38;2;204;85;0m", new Color(204, 85, 0)),         // Molto più scuro
    LIGHT_ORANGE("\u001B[38;2;255;218;185m", new Color(255, 218, 185)),  // Molto più chiaro
    BURNT_ORANGE("\u001B[38;2;139;69;19m", new Color(139, 69, 19)),      // Marrone-arancio
    NEON_ORANGE("\u001B[38;2;255;95;31m", new Color(255, 95, 31)),       // Rosso-arancio vivace
    
    // ============== SFUMATURE DI ROSSO ==============
    CRIMSON("\u001B[38;2;220;20;60m", new Color(220, 20, 60)),       // Rosa-rosso intenso
    MAROON("\u001B[38;2;128;0;0m", new Color(128, 0, 0)),            // Rosso molto scuro
    FIREBRICK("\u001B[38;2;178;34;34m", new Color(178, 34, 34)),     // Rosso mattone
    ROSE("\u001B[38;2;255;228;225m", new Color(255, 228, 225)),      // Rosa molto tenue

    // ============== SFUMATURE DI BLU ==============
    NAVY("\u001B[38;2;0;0;128m", new Color(0, 0, 128)),              // Blu molto scuro
    SKYBLUE("\u001B[38;2;135;206;250m", new Color(135, 206, 250)),   // Celeste brillante
    TURQUOISE("\u001B[38;2;0;206;209m", new Color(0, 206, 209)),     // Verde-blu vivace
    INDIGO("\u001B[38;2;75;0;130m", new Color(75, 0, 130)),          // Viola-blu scuro

    // ============== SFUMATURE DI GIALLO ==============
    GOLD("\u001B[38;2;255;215;0m", new Color(255, 215, 0)),          // Oro brillante
    LEMON("\u001B[38;2;255;255;224m", new Color(255, 255, 224)),     // Giallo molto chiaro
    OLIVE("\u001B[38;2;107;142;35m", new Color(107, 142, 35)),       // Verde oliva scuro
    SUNFLOWER("\u001B[38;2;255;218;3m", new Color(255, 218, 3)),     // Giallo puro vivace
    
    // ============== SFUMATURE DI ROSA ==============
    PINK("\u001B[38;2;255;192;203m", new Color(255, 192, 203)),      // Rosa standard
    HOT_PINK("\u001B[38;2;255;20;147m", new Color(255, 20, 147)),    // Magenta-rosa intenso
    DEEP_PINK("\u001B[38;2;199;21;133m", new Color(199, 21, 133)),   // Rosa scuro intenso
    LIGHT_PINK("\u001B[38;2;255;240;245m", new Color(255, 240, 245)), // Rosa molto tenue
    SALMON("\u001B[38;2;250;128;114m", new Color(250, 128, 114)),    // Arancio-rosa
    
    // ============== SFUMATURE DI VERDE ==============
    FOREST_GREEN("\u001B[38;2;34;139;34m", new Color(34, 139, 34)),  // Verde scuro
    LIME("\u001B[38;2;50;205;50m", new Color(50, 205, 50)),          // Verde lime brillante
    EMERALD("\u001B[38;2;0;201;87m", new Color(0, 201, 87)),         // Verde smeraldo puro
    MINT("\u001B[38;2;189;252;201m", new Color(189, 252, 201)),      // Verde menta molto chiaro
    
    // ============== SFUMATURE DI VIOLA ==============
    PURPLE("\u001B[38;2;128;0;128m", new Color(128, 0, 128)),        // Viola puro
    LAVENDER("\u001B[38;2;230;230;250m", new Color(230, 230, 250)),  // Viola molto chiaro
    VIOLET("\u001B[38;2;148;0;211m", new Color(148, 0, 211)),        // Viola intenso
    PLUM("\u001B[38;2;139;69;139m", new Color(139, 69, 139)),        // Viola-marrone scuro
    
    // ============== COLORI NEUTRI ==============
    GRAY("\u001B[38;2;128;128;128m", new Color(128, 128, 128)),      // Grigio medio
    SILVER("\u001B[38;2;192;192;192m", new Color(192, 192, 192)),    // Grigio chiaro
    BEIGE("\u001B[38;2;245;245;220m", new Color(245, 245, 220)),     // Beige chiaro
    BROWN("\u001B[38;2;101;67;33m", new Color(101, 67, 33)),         // Marrone scuro
    
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