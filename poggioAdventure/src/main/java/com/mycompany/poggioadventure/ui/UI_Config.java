package com.mycompany.poggioadventure.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Classe di configurazione UI con caricamento risorse centralizzato.
 * Implementa il pattern Singleton per garantire un'unica istanza.
 */
public final class UI_Config {
    private static final Logger LOGGER = Logger.getLogger(UI_Config.class.getName());
    
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
            loadResources();
        } catch (InitializationException ex) {
            LOGGER.log(Level.SEVERE, "Errore critico durante l'inizializzazione", ex);
            System.exit(1);
        }
    }

    private UI_Config() {
        throw new AssertionError("Classe di utilità, non istanziabile");
    }

    private static void loadResources() throws InitializationException {
        try {
            shieldImage = loadImage(SHIELD_IMAGE_PATH);
            normalFont = loadFont(FONT_NORMAL_PATH);
            boldFont = loadFont(FONT_BOLD_PATH);
            italicFont = loadFont(FONT_ITALIC_PATH);
            asciiFlipper = loadImage(ASCII_FLIPPER_PATH);
        } catch (IOException | FontFormatException e) {
            throw new InitializationException("Caricamento risorse fallito", e);
        }
    }

    private static BufferedImage loadImage(String path) throws IOException {
        File imageFile = new File(path);
        if (!imageFile.exists()) {
            throw new IOException("File immagine non trovato: " + path);
        }
        
        BufferedImage image = ImageIO.read(imageFile);
        if (image == null) {
            throw new IOException("Formato immagine non supportato: " + path);
        }
        return image;
    }

    private static Font loadFont(String path) throws IOException, FontFormatException {
        File fontFile = new File(path);
        if (!fontFile.exists()) {
            throw new IOException("File font non trovato: " + path);
        }
        
        Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
        return font;
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
    
    private static class InitializationException extends Exception {
        public InitializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
