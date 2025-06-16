package com.mycompany.poggioadventure.ui.gui.views;

import com.mycompany.poggioadventure.persistence.ResourceLoader;
import com.mycompany.poggioadventure.core.utils.Utils;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe di configurazione centralizzata per l'interfaccia grafica.
 * Contiene tutte le costanti UI, le risorse grafiche e i parametri di stile.
 * 
 * <p>Caratteristiche principali:
 * <ul>
 *   <li>Definisce la palette di colori dell'applicazione</li>
 *   <li>Gestisce il caricamento di font e immagini</li>
 *   <li>Fornisce rapporti dimensionali responsive</li>
 *   <li>Configura spaziature e margini standard</li>
 * </ul>
 */
public final class UI_Config {

    // ==================== PALETTE COLORI ====================
    
    /** Colore di sfondo principale (grigio scuro - RGB 45,45,45) */
    public static final Color BACKGROUND_COLOR = new Color(45, 45, 45);
    
    /** Colore base dei pulsanti (grigio medio - RGB 100,100,100) */
    public static final Color BUTTON_BASE_COLOR = new Color(100, 100, 100);
    
    /** Colore al passaggio del mouse sui pulsanti (grigio chiaro - RGB 130,130,130) */
    public static final Color BUTTON_HOVER_COLOR = new Color(130, 130, 130);
    
    /** Colore quando il pulsante è premuto (blu scuro - RGB 0,82,164) */
    public static final Color BUTTON_PRESSED_COLOR = new Color(0, 82, 164);
    
    /** Colore per bordi e separatori (grigio chiaro - RGB 150,150,150) */
    public static final Color BORDER_COLOR = new Color(150, 150, 150);
    
    /** Colore principale per il testo (bianco) */
    public static final Color TEXT_COLOR = Color.WHITE;

    // ============== RAPPORTI DIMENSIONALI ==============
    // Tutti i valori sono percentuali relative alla dimensione dello schermo
    
    /** Rapporto altezza font titoli (7% altezza finestra) */
    public static final float TITLE_FONT_RATIO = 0.07f;
    
    /** Rapporto altezza font pulsanti (3% altezza finestra) */
    public static final float BUTTON_FONT_RATIO = 0.03f;
    
    /** Rapporto larghezza finestra (35% larghezza schermo) */
    public static final float WINDOW_WIDTH_RATIO = 0.35f;
    
    /** Rapporto altezza finestra (85% altezza schermo) */
    public static final float WINDOW_HEIGHT_RATIO = 0.85f;
    
    /** Rapporto larghezza pulsanti (30% larghezza finestra) */
    public static final float BUTTON_WIDTH_RATIO = 0.3f;
    
    /** Rapporto altezza pulsanti (7% altezza finestra) */
    public static final float BUTTON_HEIGHT_RATIO = 0.07f;
    
    /** Rapporto dimensione immagine scudo (25% larghezza finestra) */
    public static final float SHIELD_SIZE_RATIO = 0.25f;

    // ============== MARGINI E SPAZIATURE ==============
    
    /** Margini standard per pulsanti (top:15, left:20, bottom:15, right:20) */
    public static final Insets BUTTON_INSETS = new Insets(15, 20, 15, 20);
    
    /** Margini speciali per pulsante uscita (top:30, left:20, bottom:0, right:20) */
    public static final Insets EXIT_BUTTON_INSETS = new Insets(30, 20, 0, 20);
    
    /** Rapporto margine superiore contenuti */
    public static final int TOP_MARGIN_RATIO = 2;

    // ============== RISORSE GRAFICHE ==============
    private static BufferedImage shieldImage;
    private static BufferedImage asciiFlipper;
    private static Font normalFont;
    private static Font boldFont;
    private static Font italicFont;

    // ============== PERCORSI RISORSE ==============
    private static final Path SHIELD_IMAGE_PATH = ResourceLoader.IMG_PATH.resolve("scudopoggiolevante.png");
    private static final Path ASCII_FLIPPER_PATH = ResourceLoader.IMG_PATH.resolve("flipper.jpg");
    private static final Path FONT_NORMAL_PATH = ResourceLoader.FONTS_PATH.resolve("BarlowCondensed-Medium.ttf");
    private static final Path FONT_BOLD_PATH = ResourceLoader.FONTS_PATH.resolve("BarlowCondensed-Bold.ttf");
    private static final Path FONT_ITALIC_PATH = ResourceLoader.FONTS_PATH.resolve("BarlowCondensed-SemiBoldItalic.ttf");

    // ============== INIZIALIZZAZIONE ==============
    static {
        try {
            ResourceLoader.loadResources();
        } catch (IOException | FontFormatException ex) {
            Logger.getLogger(UI_Config.class.getName()).log(
                Level.SEVERE, 
                "ERRORE: Caricamento risorse UI fallito", 
                ex
            );
            Utils.exitApplication(Utils.EXIT_CODE_RESOURCE_ERROR);
        }
    }
    
    /**
     * Restituisce l'immagine dello scudo
     * @return BufferedImage o null se non caricata
     */
    public static BufferedImage getShieldImage() {
        return shieldImage;
    }

    /**
     * Restituisce il font normale dell'applicazione
     * @return Font caricato o null se errore
     */
    public static Font getNormalFont() {
        return normalFont;
    }

    /**
     * Restituisce il font in grassetto
     * @return Font caricato o null se errore
     */
    public static Font getBoldFont() {
        return boldFont;
    }

    /**
     * Restituisce il font in corsivo
     * @return Font caricato o null se errore
     */
    public static Font getItalicFont() {
        return italicFont;
    }

    /**
     * Restituisce l'immagine ASCII del flipper
     * @return BufferedImage o null se non caricata
     */
    public static BufferedImage getAsciiImage() {
        return asciiFlipper;
    }

    // ============== METODI DI CONFIGURAZIONE ==============
    
    /**
     * Imposta l'immagine dello scudo (uso interno)
     * @param shieldImage Immagine
     */
    public static void setShieldImage(BufferedImage shieldImage) {
        UI_Config.shieldImage = shieldImage;
    }

    public static void setAsciiFlipper(BufferedImage asciiFlipper) {
        UI_Config.asciiFlipper = asciiFlipper;
    }

    public static void setNormalFont(Font normalFont) {
        UI_Config.normalFont = normalFont;
    }

    public static void setBoldFont(Font boldFont) {
        UI_Config.boldFont = boldFont;
    }

    public static void setItalicFont(Font italicFont) {
        UI_Config.italicFont = italicFont;
    }

    // ============== PERCORSI ASSOLUTI ==============
    
    /**
     * Restituisce il percorso assoluto dell'immagine scudo
     * @return percorso dello immagine Scudo di poggiolevante
     */
    public static String getSHIELD_IMAGE_PATH() {
        return SHIELD_IMAGE_PATH.toString();
    }

    public static String getFONT_NORMAL_PATH() {
        return FONT_NORMAL_PATH.toString();
    }

    public static String getFONT_BOLD_PATH() {
        return FONT_BOLD_PATH.toString();
    }

    public static String getFONT_ITALIC_PATH() {
        return FONT_ITALIC_PATH.toString();
    }

    public static String getASCII_FLIPPER_PATH() {
        return ASCII_FLIPPER_PATH.toString();
    }

    // ============== CONTROLLO INSTANZIAZIONE ==============
    private UI_Config() {
        throw new AssertionError("Classe di utilità non istanziabile");
    }
}