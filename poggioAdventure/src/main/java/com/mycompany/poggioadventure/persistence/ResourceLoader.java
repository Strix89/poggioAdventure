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
import java.net.URISyntaxException;
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
 * Classe centrale per la gestione delle risorse dell'applicazione.
 * 
 * <p>Responsabile del caricamento e gestione di:
 * <ul>
 *   <li>Immagini e asset grafici</li>
 *   <li>Font e tipografie</li>
 *   <li>File di configurazione e dati</li>
 *   <li>Directory di sistema (salvataggi, log, risorse)</li>
 * </ul>
 * 
 * <p>Funzionalità principali:
 * <ul>
 *   <li>Caricamento lazy delle risorse</li>
 *   <li>Verifica automatica delle strutture directory</li>
 *   <li>Pulizia delle risorse orfane</li>
 *   <li>Gestione centralizzata dei percorsi risorse</li>
 * </ul>
 * 
 * <p><b>Sicurezza:</b> Implementa controlli per:
 * <ul>
 *   <li>Verifica esistenza file prima del caricamento</li>
 *   <li>Gestione degli errori di I/O</li>
 *   <li>Validazione delle risorse caricate</li>
 * </ul>
 */
public class ResourceLoader {

    /**
     * Percorso della directory contenente il file JAR eseguibile.
     * Tutti gli altri percorsi sono relativi a questa directory.
     */
    private static final Path JAR_DIRECTORY = getJarDirectory();
    
    /**
     * Determina la directory contenente il file JAR eseguibile.
     * 
     * @return Path della directory contenente il JAR
     */
    private static Path getJarDirectory() {
        try {
            // Ottieni il percorso del JAR corrente
            Path jarPath = Paths.get(ResourceLoader.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());
            
            // Se è un file JAR, restituisci la directory padre
            if (jarPath.toString().endsWith(".jar")) {
                return jarPath.getParent();
            } else {
                // Durante lo sviluppo (esecuzione da IDE), usa la directory di lavoro corrente
                return Paths.get("").toAbsolutePath();
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(ResourceLoader.class.getName())
                .log(Level.WARNING, "Impossibile determinare la directory del JAR, uso directory corrente", ex);
            return Paths.get("").toAbsolutePath();
        }
    }
    
    /**
     * Percorso del file contenente le stopwords, relativo al JAR.
     */
    public static final Path STOPWORDS_PATH = JAR_DIRECTORY.resolve("resources/stopwords");
    
    /**
     * Percorso della directory dei salvataggi del gioco, relativo al JAR.
     */
    public static final Path SAVES_DIRECTORY = JAR_DIRECTORY.resolve("resources/sav");
    
    /**
     * Percorso della directory dei log scaricati del gioco, relativo al JAR.
     */
    public static final Path LOGS_DW_DIRECTORY = JAR_DIRECTORY.resolve("resources/down_logs");
    
    /**
     * Percorso della directory delle immagini, relativo al JAR.
     */
    public static final Path IMG_PATH = JAR_DIRECTORY.resolve("resources/img");
    
    /**
     * Percorso della directory dei font, relativo al JAR.
     */
    public static final Path FONTS_PATH = JAR_DIRECTORY.resolve("resources/fonts");
    
    /**
     * Percorso della directory dei log dell'applicazione, relativo al JAR.
     */
    public static final Path LOGS_DIRECTORY = JAR_DIRECTORY.resolve("resources/logs");
;

    /**
     * Carica un'immagine dal filesystem.
     * 
     * @param path Percorso assoluto o relativo del file immagine
     * @return BufferedImage con l'immagine caricata
     * @throws IOException Se:
     * <ul>
     *   <li>Il file non esiste</li>
     *   <li>Non si hanno i permessi di lettura</li>
     *   <li>Il file non è un'immagine valida</li>
     * </ul>
     * @throws IllegalArgumentException Se il path è null o vuoto
     */
    public static BufferedImage loadImage(String path) throws IOException {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Il percorso dell'immagine non può essere nullo o vuoto");
        }
        
        File imageFile = new File(path);
        if (!imageFile.exists()) {
            throw new IOException("Immagine non trovata: " + path);
        }
        
        BufferedImage image = ImageIO.read(imageFile);
        if (image == null) {
            throw new IOException("Il file non è un'immagine valida: " + path);
        }
        
        return image;
    }

    /**
     * Carica un font TrueType (.ttf) dal filesystem.
     * 
     * @param path Percorso assoluto o relativo del file del font
     * @return Oggetto Font caricato
     * @throws IOException Se:
     * <ul>
     *   <li>Il file non esiste</li>
     *   <li>Non si hanno i permessi di lettura</li>
     * </ul>
     * @throws FontFormatException Se il file non contiene un font TrueType valido
     * @throws IllegalArgumentException Se il path è null o vuoto
     */
    public static Font loadFont(String path) throws IOException, FontFormatException {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Il percorso del font non può essere nullo o vuoto");
        }
        
        File fontFile = new File(path);
        if (!fontFile.exists()) {
            throw new IOException("Font non trovato: " + path);
        }
        
        return Font.createFont(Font.TRUETYPE_FONT, fontFile);
    }
    
    /**
     * Verifica e crea la directory dei salvataggi se non esiste.
     * <p>Se la creazione fallisce, termina l'applicazione con codice di errore critico.
     * 
     * @throws SecurityException Se non si hanno i permessi per creare la directory
     */
    private static void checkSavesDirectory() {
        try {
            if (!Files.exists(SAVES_DIRECTORY)) {
                Files.createDirectories(SAVES_DIRECTORY);
                Logger.getLogger(Engine.class.getName()).log(Level.INFO, 
                    "Cartella dei salvataggi creata: {0}", SAVES_DIRECTORY);
            }
        } catch (IOException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, 
                "Impossibile creare la cartella dei salvataggi: {0}", SAVES_DIRECTORY);
            Utils.exitApplication(Utils.EXIT_CODE_CRITICAL);
        }
    }
    
    /**
     * Verifica e crea la directory dei log se non esiste.
     * <p>Logga un avviso se la creazione fallisce, ma non termina l'applicazione.
     */
    private static void checkLogsDirectory() {
        try {
            if (!Files.exists(LOGS_DIRECTORY)) {
                Files.createDirectories(LOGS_DIRECTORY);
            }
        } catch (IOException ex) {
            Logger.getLogger(ResourceLoader.class.getName()).log(Level.WARNING,
                "Impossibile creare la cartella dei log del gioco: {0}", LOGS_DIRECTORY);
        }
        try {
            if (!Files.exists(LOGS_DW_DIRECTORY)) {
                Files.createDirectories(LOGS_DW_DIRECTORY);
            }
        } catch (IOException ex) {
            Logger.getLogger(ResourceLoader.class.getName()).log(Level.WARNING,
                "Impossibile creare la cartella dei log scaricati: {0}", LOGS_DW_DIRECTORY);
        }
    }
    
     /**
     * Verifica e crea la directory delle immagini se non esiste.
     * <p>Logga un errore se la creazione fallisce, ma non termina l'applicazione.
     */
    public static void checkImagesDirectory() {
        try {
            if (!Files.exists(IMG_PATH)) {
                Files.createDirectories(IMG_PATH);
                Logger.getLogger(ResourceLoader.class.getName()).log(Level.INFO,
                    "Cartella delle immagini creata: {0}", IMG_PATH);
            }
        } catch (IOException ex) {
            Logger.getLogger(ResourceLoader.class.getName()).log(Level.SEVERE,
                "Impossibile creare la cartella delle immagini: {0}", IMG_PATH);
        }
    }
    
    /**
     * Verifica e crea la directory dei font se non esiste.
     * <p>Logga un errore se la creazione fallisce, ma non termina l'applicazione.
     */
    public static void checkFontsDirectory() {
        try {
            if (!Files.exists(FONTS_PATH)) {
                Files.createDirectories(FONTS_PATH);
                Logger.getLogger(ResourceLoader.class.getName()).log(Level.INFO,
                    "Cartella dei font creata: {0}", FONTS_PATH);
            }
        } catch (IOException ex) {
            Logger.getLogger(ResourceLoader.class.getName()).log(Level.SEVERE,
                "Impossibile creare la cartella dei font: {0}", FONTS_PATH);
        }
    }

    /**
     * Carica un file di testo in un Set di stringhe.
     * <p>Ogni riga del file diventa un elemento del Set.
     * 
     * @param file File da caricare (non null)
     * @return Set<String> contenente le righe del file (mai null)
     * @throws IOException Se si verificano errori di lettura
     * @throws IllegalArgumentException Se il file è null
     */
    public static Set<String> loadFileListInSet(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("Il file non può essere null");
        }
        
        Set<String> set = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                set.add(line.trim().toLowerCase());
            }
        }
        return set;
    }

    /**
     * Carica tutte le risorse necessarie per l'applicazione.
     * <p>Operazioni eseguite:
     * <ol>
     *   <li>Verifica/crea directory necessarie</li>
     *   <li>Pulisce log orfani</li>
     *   <li>Carica immagini dell'interfaccia</li>
     *   <li>Carica e configura i font</li>
     * </ol>
     * 
     * @throws IOException Se fallisce il caricamento di immagini/font
     * @throws FontFormatException Se i font caricati non sono validi
     */
    public static void loadResources() throws IOException, FontFormatException {
        // Verifica e crea le directory necessarie
        checkSavesDirectory();
        checkLogsDirectory();
        cleanOrphanedLogs();
        checkImagesDirectory();
        checkFontsDirectory();
        
        // Caricamento delle immagini per l'interfaccia utente
        UI_Config.setShieldImage(loadImage(UI_Config.getSHIELD_IMAGE_PATH()));
        UI_Config.setAsciiFlipper(loadImage(UI_Config.getASCII_FLIPPER_PATH()));

        // Caricamento e configurazione dei font
        UI_Config.setNormalFont(loadFont(UI_Config.getFONT_NORMAL_PATH()));
        UI_Config.setBoldFont(loadFont(UI_Config.getFONT_BOLD_PATH()));
        UI_Config.setItalicFont(loadFont(UI_Config.getFONT_ITALIC_PATH()));
    }
    
    /**
     * Elimina i file di log non associati a salvataggi esistenti.
     * <p>Un log è considerato orfano se:
     * <ul>
     *   <li>Non esiste un salvataggio corrispondente</li>
     * </ul>
     * 
     * <p>Logga gli errori ma non interrompe l'esecuzione in caso di problemi.
     */
    public static void cleanOrphanedLogs() {
        try {
            // Recupera tutti i log associati a salvataggi validi
            Set<Path> activeLogs = Files.list(SAVES_DIRECTORY)
                .map(savePath -> {
                    try {
                        return SaveGame.findLogFileName(savePath);
                    } catch (Exception e) {
                        Logger.getLogger(ResourceLoader.class.getName())
                            .log(Level.FINE, "Salvataggio non valido: {0}", savePath);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

            // Elimina i log non presenti nella lista dei log attivi
            Files.list(LOGS_DIRECTORY)
                .filter(logPath -> !activeLogs.contains(logPath.toAbsolutePath()))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        Logger.getLogger(ResourceLoader.class.getName())
                            .log(Level.FINE, "Log orfano eliminato: {0}", path);
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

    /**
     * Carica le risorse necessarie per l'interfaccia CLI.
     * <p>Operazioni eseguite:
     * <ol>
     *   <li>Verifica/crea directory necessarie</li>
     *   <li>Pulisce log orfani</li>
     * </ol>
     * 
     * @throws IOException Se fallisce la creazione delle directory critiche
     */
    public static void loadResourcesForCLI() throws IOException {
        // Verifica e crea le directory necessarie
        checkSavesDirectory();
        checkLogsDirectory();
        
        // Pulizia dei log orfani
        cleanOrphanedLogs();
    }
}
