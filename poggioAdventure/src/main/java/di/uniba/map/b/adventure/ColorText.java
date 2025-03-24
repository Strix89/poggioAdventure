package di.uniba.map.b.adventure;

import java.awt.Color;

/**
 *
 * @author Strix89
 */
/**
 * Enum per la gestione dei colori di testo in console (ANSI) e GUI (Swing).
 * @author Strix89
 */
public enum ColorText {
    // Reset/Base
    RESET("\u001B[0m", Color.WHITE),
    
    // Standard ANSI + Swing equivalents
    BLACK("\u001B[30m", Color.BLACK),
    RED("\u001B[31m", new Color(255, 0, 0)),
    GREEN("\u001B[32m", new Color(0, 255, 0)),
    YELLOW("\u001B[33m", new Color(255, 255, 0)),
    BLUE("\u001B[34m", new Color(0, 120, 215)), // Blu Windows
    MAGENTA("\u001B[35m", new Color(255, 0, 255)),
    CYAN("\u001B[36m", new Color(0, 255, 255)),
    WHITE("\u001B[37m", Color.WHITE),
    
    // Bright variants (ANSI high intensity)
    BRIGHT_BLACK("\u001B[90m", new Color(100, 100, 100)),
    BRIGHT_RED("\u001B[91m", new Color(255, 50, 50)),
    BRIGHT_GREEN("\u001B[92m", new Color(50, 255, 50)),
    BRIGHT_YELLOW("\u001B[93m", new Color(255, 255, 100)),
    BRIGHT_BLUE("\u001B[94m", new Color(100, 150, 255)),
    BRIGHT_MAGENTA("\u001B[95m", new Color(255, 100, 255)),
    BRIGHT_CYAN("\u001B[96m", new Color(100, 255, 255)),
    BRIGHT_WHITE("\u001B[97m", new Color(255, 255, 255)),
    
    // Semantic colors (per il gioco)
    NPC("\u001B[36m", new Color(0, 255, 255)),       // Ciano (come prima)
    ITEM("\u001B[33m", new Color(255, 185, 0)),      // Oro (come prima)
    WARNING("\u001B[93m", new Color(255, 200, 0)),   // Giallo acceso
    ERROR("\u001B[91m", new Color(255, 80, 80)),     // Rosso acceso
    SUCCESS("\u001B[92m", new Color(50, 220, 50)),   // Verde acceso
    HIGHLIGHT("\u001B[95m", new Color(255, 150, 255)); // Magenta chiaro

    private final String ansiCode;
    private final Color swingColor;

    ColorText(String ansi, Color swing) {
        this.ansiCode = ansi;
        this.swingColor = swing;
    }

    public String getANSICode() { 
        return ansiCode; 
    }

    public Color getSwingColor() { 
        return swingColor; 
    }
}