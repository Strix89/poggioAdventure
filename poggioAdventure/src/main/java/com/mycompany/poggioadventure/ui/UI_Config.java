package com.mycompany.poggioadventure.ui;

import di.uniba.map.b.adventure.ResourceLoader;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe di configurazione UI con caricamento risorse centralizzato.
 * Implementa il pattern Singleton per garantire un'unica istanza.
 */
public final class UI_Config {
    // Costanti di configurazione
    public static final Color BACKGROUND_COLOR = new Color(45, 45, 45);
    public static final Color BUTTON_BASE_COLOR = new Color(100, 100, 100);
    public static final Color BUTTON_HOVER_COLOR = new Color(130, 130, 130);
    public static final Color BORDER_COLOR = new Color(150, 150, 150);
    public static final Color TEXT_COLOR = Color.WHITE;

    public static final float TITLE_FONT_RATIO = 0.07f;
    public static final float BUTTON_FONT_RATIO = 0.03f;

    public static final float WINDOW_WIDTH_RATIO = 0.35f;
    public static final float WINDOW_HEIGHT_RATIO = 0.85f;
    public static final float BUTTON_WIDTH_RATIO = 0.3f;
    public static final float BUTTON_HEIGHT_RATIO = 0.07f;
    public static final float SHIELD_SIZE_RATIO = 0.25f;

    public static final Insets BUTTON_INSETS = new Insets(15, 20, 15, 20);
    public static final Insets EXIT_BUTTON_INSETS = new Insets(30, 20, 0, 20);
    public static final int TOP_MARGIN_RATIO = 2;

    // Risorse caricate
    private static BufferedImage shieldImage;
    private static BufferedImage asciiFlipper;
    private static Font normalFont;
    private static Font boldFont;
    private static Font italicFont;

    // Percorsi risorse
    private static final String SHIELD_IMAGE_PATH = "./resources/img/scudopoggiolevante.png";
    private static final String FONT_NORMAL_PATH = "./resources/fonts/BarlowCondensed-Medium.ttf";
    private static final String FONT_BOLD_PATH = "./resources/fonts/BarlowCondensed-Bold.ttf";
    private static final String FONT_ITALIC_PATH = "./resources/fonts/BarlowCondensed-SemiBoldItalic.ttf";
    private static final String ASCII_FLIPPER_PATH = "./resources/img/flipper.jpg";

    static {
        try {
            ResourceLoader.loadUIResources();
        } catch (IOException ex) {
            Logger.getLogger(UI_Config.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FontFormatException ex) {
            Logger.getLogger(UI_Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private UI_Config() {
        throw new AssertionError("Classe di utilità, non istanziabile");
    }

    // Getter per le risorse
    public static BufferedImage getShieldImage() {
        return shieldImage;
    }

    public static Font getNormalFont() {
        return normalFont;
    }

    public static Font getBoldFont() {
        return boldFont;
    }

    public static Font getItalicFont() {
        return italicFont;
    }

    public static BufferedImage getAsciiImage(){
        return asciiFlipper;
    }

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

    public static String getSHIELD_IMAGE_PATH() {
        return SHIELD_IMAGE_PATH;
    }

    public static String getFONT_NORMAL_PATH() {
        return FONT_NORMAL_PATH;
    }

    public static String getFONT_BOLD_PATH() {
        return FONT_BOLD_PATH;
    }

    public static String getFONT_ITALIC_PATH() {
        return FONT_ITALIC_PATH;
    }

    public static String getASCII_FLIPPER_PATH() {
        return ASCII_FLIPPER_PATH;
    }
}
