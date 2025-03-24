package di.uniba.map.b.adventure;

import java.awt.Color;

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
    
    ORANGE("\u001B[38;2;255;165;0m", new Color(255, 165, 0)),            // Arancione standard
    DARK_ORANGE("\u001B[38;2;255;140;0m", new Color(255, 140, 0)),       // Arancione scuro
    LIGHT_ORANGE("\u001B[38;2;255;160;122m", new Color(255, 160, 122)),  // Arancione chiaro
    BURNT_ORANGE("\u001B[38;2;177;73;7m", new Color(177, 73, 7)),        // Arancione bruciato
    NEON_ORANGE("\u001B[38;2;255;127;39m", new Color(255, 127, 39)),     // Arancione neone
    
    CRIMSON("\u001B[38;2;220;20;60m", new Color(220, 20, 60)),       // Rosso sangue
    MAROON("\u001B[38;2;128;0;0m", new Color(128, 0, 0)),            // Rosso scuro
    FIREBRICK("\u001B[38;2;178;34;34m", new Color(178, 34, 34)),     // Rosso arancione
    ROSE("\u001B[38;2;255;192;203m", new Color(255, 192, 203)),      // Rosso rosa

    // Nuove sfumature di BLU
    NAVY("\u001B[38;2;0;0;128m", new Color(0, 0, 128)),              // Blu navy
    SKYBLUE("\u001B[38;2;135;206;235m", new Color(135, 206, 235)),   // Blu cielo
    TURQUOISE("\u001B[38;2;64;224;208m", new Color(64, 224, 208)),   // Blu-verde acqua
    INDIGO("\u001B[38;2;75;0;130m", new Color(75, 0, 130)),          // Blu violaceo

    // Nuove sfumature di GIALLO
    GOLD("\u001B[38;2;255;215;0m", new Color(255, 215, 0)),          // Giallo oro
    LEMON("\u001B[38;2;255;250;205m", new Color(255, 250, 205)),     // Giallo limone chiaro
    OLIVE("\u001B[38;2;128;128;0m", new Color(128, 128, 0)),         // Giallo oliva
    SUNFLOWER("\u001B[38;2;244;213;64m", new Color(244, 213, 64)),   // Giallo girasole
    
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