package com.mycompany.poggioadventure.persistence;

import com.mycompany.poggioadventure.core.Engine;
import com.mycompany.poggioadventure.ui.gui.views.UI_Config;
import com.mycompany.poggioadventure.core.utils.Utils;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

/**
 * Classe utilitaria per il caricamento delle risorse dell'applicazione.
 * Gestisce il caricamento di immagini, font, file di configurazione e verifica
 * l'esistenza delle directory necessarie per il funzionamento dell'applicazione.
 * 
 * @author Strix89
 */
public class ResourceLoader {
    
    // Path assoluto per il file delle stopwords (parole da ignorare nell'elaborazione del testo)
    public static final Path STOPWORDS_PATH = Paths.get("resources", "stopwords").toAbsolutePath();
    
    // Path assoluto per la directory dei salvataggi del gioco
    public static final Path SAVES_DIRECTORY = Paths.get("sav").toAbsolutePath();
    
    // Path assoluto per la directory dei log dell'applicazione
    public static final Path LOGS_DIRECTORY = Paths.get("resources","logs").toAbsolutePath();

    /**
     * Carica un'immagine dal filesystem.
     * 
     * @param path Percorso del file immagine da caricare
     * @return Oggetto BufferedImage contenente l'immagine caricata
     * @throws IOException Se il file non esiste o non può essere letto
     */
    public static BufferedImage loadImage(String path) throws IOException {
        File imageFile = new File(path);
        if (!imageFile.exists()) {
            throw new IOException("Immagine non trovata: " + path);
        }
        return ImageIO.read(imageFile);
    }

    /**
     * Carica un font dal filesystem.
     * 
     * @param path Percorso del file del font da caricare
     * @return Oggetto Font caricato
     * @throws IOException Se il file non esiste o non può essere letto
     * @throws FontFormatException Se il file non contiene un font valido
     */
    public static Font loadFont(String path) throws IOException, FontFormatException {
        File fontFile = new File(path);
        if (!fontFile.exists()) {
            throw new IOException("Font non trovato: " + path);
        }
        return Font.createFont(Font.TRUETYPE_FONT, fontFile);
    }
    
    /**
     * Verifica l'esistenza della directory per i salvataggi e la crea se non esiste.
     * Se la creazione fallisce, termina l'applicazione con un codice di errore critico.
     */
    private static void checkSavesDirectory() {
        File savesDir = new File(SAVES_DIRECTORY.toString());
        if (!savesDir.exists()) {
            if (savesDir.mkdirs()) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, 
                    "Cartella dei salvataggi creata: {0}", savesDir.getAbsolutePath());
            } else {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, 
                    "Impossibile creare la cartella dei salvataggi: {0}", SAVES_DIRECTORY);
                Utils.exitApplication(Utils.EXIT_CODE_CRITICAL);
            }
        }
    }
    
    /**
     * Verifica l'esistenza della directory per i log e la crea se non esiste.
     */
    private static void checkLogsDirectory() {
        File logsDir = new File(LOGS_DIRECTORY.toString());
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }
    }

    /**
     * Carica il contenuto di un file di testo in un Set di stringhe.
     * Ogni riga del file diventa un elemento del Set.
     * 
     * @param file File da caricare
     * @return Set contenente le righe del file
     * @throws IOException Se si verificano errori nella lettura del file
     */
    public static Set<String> loadFileListInSet(File file) throws IOException {
        Set<String> set = new HashSet<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while (reader.ready()) {
            set.add(reader.readLine().trim().toLowerCase());
        }
        reader.close();
        return set;
    }

    /**
     * Carica tutte le risorse necessarie per l'applicazione.
     * Verifica le directory, carica immagini e font, e configura l'interfaccia utente.
     * 
     * @throws IOException Se si verificano errori nel caricamento delle risorse
     * @throws FontFormatException Se i font caricati non sono validi
     */
    public static void loadResources() throws IOException, FontFormatException {
        // Verifica e crea le directory necessarie
        checkSavesDirectory();
        checkLogsDirectory();
        cleanOrphanedLogs();
        
        // Caricamento delle immagini per l'interfaccia utente
        UI_Config.setShieldImage(loadImage(UI_Config.getSHIELD_IMAGE_PATH()));
        UI_Config.setAsciiFlipper(loadImage(UI_Config.getASCII_FLIPPER_PATH()));

        // Caricamento e configurazione dei font per l'interfaccia utente
        UI_Config.setNormalFont(loadFont(UI_Config.getFONT_NORMAL_PATH()));
        UI_Config.setBoldFont(loadFont(UI_Config.getFONT_BOLD_PATH()));
        UI_Config.setItalicFont(loadFont(UI_Config.getFONT_ITALIC_PATH()));
    }
    
    
    // Sostituisci cleanOrphanedLogs() con:
    public static void cleanOrphanedLogs() {
        try {
            Set<Path> activeLogs = Files.list(SAVES_DIRECTORY)
                .map(savePath -> {
                    try {
                        return SaveGame.findLogFileName(savePath);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

            Files.list(LOGS_DIRECTORY)
                .filter(logPath -> !activeLogs.contains(logPath.toAbsolutePath()))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException ex) {
                        Logger.getLogger(ResourceLoader.class.getName())
                            .log(Level.WARNING, "Errore eliminazione log orfano", ex);
                    }
                });
        } catch (IOException ex) {
            Logger.getLogger(ResourceLoader.class.getName())
                .log(Level.SEVERE, "Pulizia log fallita", ex);
        }
    }
}