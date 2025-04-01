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
 * 
 * @author Strix89
 * @version 1.1
 */
public class ResourceLoader {
    
    /**
     * Percorso assoluto del file contenente le stopwords.
     * <p>Le stopwords sono termini ignorati durante l'analisi del testo.
     * <p>Percorso predefinito: {@code resources/stopwords}
     */
    public static final Path STOPWORDS_PATH = Paths.get("resources", "stopwords").toAbsolutePath();
    
    /**
     * Percorso assoluto della directory dei salvataggi del gioco.
     * <p>Contiene tutti gli stati di gioco salvati.
     * <p>Percorso predefinito: {@code sav/}
     * <p>Viene creata automaticamente se non esiste.
     */
    public static final Path SAVES_DIRECTORY = Paths.get("sav").toAbsolutePath();
    
    /**
     * Percorso assoluto della directory dei log dell'applicazione.
     * <p>Contiene i file di log delle sessioni.
     * <p>Percorso predefinito: {@code resources/logs/}
     * <p>Viene creata automaticamente se non esiste.
     */
    public static final Path LOGS_DIRECTORY = Paths.get("resources","logs").toAbsolutePath();

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
                "Impossibile creare la cartella dei log: {0}", LOGS_DIRECTORY);
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
}
