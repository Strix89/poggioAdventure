package com.mycompany.poggioadventure.ui;

import di.uniba.map.b.adventure.ResourceLoader;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import static java.lang.System.exit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe statica di configurazione UI con caricamento risorse centralizzato.
 * Implementa il pattern Singleton per garantire un'unica istanza, senza instanziazione.
 * 
 * Questa classe è responsabile della gestione delle configurazioni dell'interfaccia utente (UI),
 * inclusi colori, dimensioni, font e immagini. Inoltre, carica le risorse necessarie
 * (come immagini e font) da file esterni e fornisce metodi per accedere a queste risorse.
 * 
 * La classe è progettata come una classe di utilità (utility class) e non può essere istanziata.
 */
public final class UI_Config {

    // ### Costanti di configurazione ###

    // Colori utilizzati nell'interfaccia utente
    public static final Color BACKGROUND_COLOR = new Color(45, 45, 45); // Colore di sfondo
    public static final Color BUTTON_BASE_COLOR = new Color(100, 100, 100); // Colore base dei pulsanti
    public static final Color BUTTON_HOVER_COLOR = new Color(130, 130, 130); // Colore dei pulsanti al passaggio del mouse
    public static final Color BORDER_COLOR = new Color(150, 150, 150); // Colore dei bordi
    public static final Color TEXT_COLOR = Color.WHITE; // Colore del testo

    // Rapporti per le dimensioni dei font rispetto alla finestra
    public static final float TITLE_FONT_RATIO = 0.07f; // Rapporto per il font del titolo
    public static final float BUTTON_FONT_RATIO = 0.03f; // Rapporto per il font dei pulsanti

    // Rapporti per le dimensioni della finestra e dei componenti
    public static final float WINDOW_WIDTH_RATIO = 0.35f; // Rapporto per la larghezza della finestra
    public static final float WINDOW_HEIGHT_RATIO = 0.85f; // Rapporto per l'altezza della finestra
    public static final float BUTTON_WIDTH_RATIO = 0.3f; // Rapporto per la larghezza dei pulsanti
    public static final float BUTTON_HEIGHT_RATIO = 0.07f; // Rapporto per l'altezza dei pulsanti
    public static final float SHIELD_SIZE_RATIO = 0.25f; // Rapporto per la dimensione dello scudo

    // Margini e spaziature
    public static final Insets BUTTON_INSETS = new Insets(15, 20, 15, 20); // Margini interni dei pulsanti
    public static final Insets EXIT_BUTTON_INSETS = new Insets(30, 20, 0, 20); // Margini interni del pulsante di uscita
    public static final int TOP_MARGIN_RATIO = 2; // Rapporto per il margine superiore

    // ### Risorse caricate ###

    private static BufferedImage shieldImage; // Immagine dello scudo
    private static BufferedImage asciiFlipper; // Immagine ASCII del flipper
    private static Font normalFont; // Font normale
    private static Font boldFont; // Font in grassetto
    private static Font italicFont; // Font in corsivo

    // ### Percorsi delle risorse ###

    private static final String SHIELD_IMAGE_PATH = "./resources/img/scudopoggiolevante.png"; // Percorso dell'immagine dello scudo
    private static final String FONT_NORMAL_PATH = "./resources/fonts/BarlowCondensed-Medium.ttf"; // Percorso del font normale
    private static final String FONT_BOLD_PATH = "./resources/fonts/BarlowCondensed-Bold.ttf"; // Percorso del font in grassetto
    private static final String FONT_ITALIC_PATH = "./resources/fonts/BarlowCondensed-SemiBoldItalic.ttf"; // Percorso del font in corsivo
    private static final String ASCII_FLIPPER_PATH = "./resources/img/flipper.jpg"; // Percorso dell'immagine ASCII del flipper

    // ### Blocco di inizializzazione statico ###

    static {
        try {
            // Carica le risorse dell'interfaccia utente (immagini e font)
            ResourceLoader.loadUIResources();
        } catch (IOException | FontFormatException ex) {
            // Se si verifica un errore durante il caricamento delle risorse, registra l'errore e termina il programma
            Logger.getLogger(UI_Config.class.getName()).log(Level.SEVERE, 
                    "Errore caricamento risorse UI", ex);
            getExitDefaultOp(); // Termina il programma con codice di uscita 1
        }
    }

    // ### Costruttore privato ###

    /**
     * Costruttore privato per impedire l'istanziazione della classe.
     * Questa è una classe di utilità e non deve essere istanziata.
     */
    private UI_Config() {
        throw new AssertionError("Classe di utilità, non istanziabile");
    }

    // ### Metodi getter per le risorse ###

    /**
     * Restituisce l'immagine dello scudo.
     * @return L'immagine dello scudo.
     */
    public static BufferedImage getShieldImage() {
        return shieldImage;
    }

    /**
     * Restituisce il font normale.
     * @return Il font normale.
     */
    public static Font getNormalFont() {
        return normalFont;
    }

    /**
     * Restituisce il font in grassetto.
     * @return Il font in grassetto.
     */
    public static Font getBoldFont() {
        return boldFont;
    }

    /**
     * Restituisce il font in corsivo.
     * @return Il font in corsivo.
     */
    public static Font getItalicFont() {
        return italicFont;
    }

    /**
     * Restituisce l'immagine ASCII del flipper.
     * @return L'immagine ASCII del flipper.
     */
    public static BufferedImage getAsciiImage(){
        return asciiFlipper;
    }

    // ### Metodi setter per le risorse ###

    /**
     * Imposta l'immagine dello scudo.
     * @param shieldImage L'immagine dello scudo da impostare.
     */
    public static void setShieldImage(BufferedImage shieldImage) {
        UI_Config.shieldImage = shieldImage;
    }

    /**
     * Imposta l'immagine ASCII del flipper.
     * @param asciiFlipper L'immagine ASCII del flipper da impostare.
     */
    public static void setAsciiFlipper(BufferedImage asciiFlipper) {
        UI_Config.asciiFlipper = asciiFlipper;
    }

    /**
     * Imposta il font normale.
     * @param normalFont Il font normale da impostare.
     */
    public static void setNormalFont(Font normalFont) {
        UI_Config.normalFont = normalFont;
    }

    /**
     * Imposta il font in grassetto.
     * @param boldFont Il font in grassetto da impostare.
     */
    public static void setBoldFont(Font boldFont) {
        UI_Config.boldFont = boldFont;
    }

    /**
     * Imposta il font in corsivo.
     * @param italicFont Il font in corsivo da impostare.
     */
    public static void setItalicFont(Font italicFont) {
        UI_Config.italicFont = italicFont;
    }

    // ### Metodi getter per i percorsi delle risorse ###

    /**
     * Restituisce il percorso dell'immagine dello scudo.
     * @return Il percorso dell'immagine dello scudo.
     */
    public static String getSHIELD_IMAGE_PATH() {
        return SHIELD_IMAGE_PATH;
    }

    /**
     * Restituisce il percorso del font normale.
     * @return Il percorso del font normale.
     */
    public static String getFONT_NORMAL_PATH() {
        return FONT_NORMAL_PATH;
    }

    /**
     * Restituisce il percorso del font in grassetto.
     * @return Il percorso del font in grassetto.
     */
    public static String getFONT_BOLD_PATH() {
        return FONT_BOLD_PATH;
    }

    /**
     * Restituisce il percorso del font in corsivo.
     * @return Il percorso del font in corsivo.
     */
    public static String getFONT_ITALIC_PATH() {
        return FONT_ITALIC_PATH;
    }

    /**
     * Restituisce il percorso dell'immagine ASCII del flipper.
     * @return Il percorso dell'immagine ASCII del flipper.
     */
    public static String getASCII_FLIPPER_PATH() {
        return ASCII_FLIPPER_PATH;
    }

    // ### Metodo per la gestione degli errori ###

    /**
     * Termina il programma con un codice di uscita 1.
     * Questo metodo viene chiamato in caso di errore irreversibile.
     */
    public static void getExitDefaultOp(){
        exit(1);
    }
}