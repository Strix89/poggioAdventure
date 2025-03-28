package di.uniba.map.b.adventure;

import com.mycompany.poggioadventure.ui.UI_Config;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class ResourceLoader {
    
    public static final Path STOPWORDS_PATH = Paths.get("resources", "stopwords").toAbsolutePath();
    public static final Path SAVES_DIRECTORY = Paths.get("sav").toAbsolutePath();
    public static final Path LOGS_DIRECTORY = Paths.get("resources","logs").toAbsolutePath();

    public static BufferedImage loadImage(String path) throws IOException {
        File imageFile = new File(path);
        if (!imageFile.exists()) {
            throw new IOException("Immagine non trovata: " + path);
        }
        return ImageIO.read(imageFile);
    }

    public static Font loadFont(String path) throws IOException, FontFormatException {
        File fontFile = new File(path);
        if (!fontFile.exists()) {
            throw new IOException("Font non trovato: " + path);
        }
        return Font.createFont(Font.TRUETYPE_FONT, fontFile);
    }
    
    // Verifica e crea la cartella dei salvataggi se non esiste
    private static void checkSavesDirectory() {
        File savesDir = new File(SAVES_DIRECTORY.toString());
        if (!savesDir.exists()) {
            if (savesDir.mkdirs()) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, "Cartella dei salvataggi creata: {0}", savesDir.getAbsolutePath());
            } else {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, "Impossibile creare la cartella dei salvataggi: {0}", SAVES_DIRECTORY);
                Utils.exitApplication(Utils.EXIT_CODE_CRITICAL);
            }
        }
    }
    
    private static void checkLogsDirectory() {
        File logsDir = new File(LOGS_DIRECTORY.toString());
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }
    }

    /**
     *
     * @param file
     * @return
     * @throws IOException
     */
    static Set<String> loadFileListInSet(File file) throws IOException {
        Set<String> set = new HashSet<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while (reader.ready()) {
            set.add(reader.readLine().trim().toLowerCase());
        }
        reader.close();
        return set;
    }

    // Carica tutte le risorse necessarie
    public static void loadResources() throws IOException, FontFormatException {
        checkSavesDirectory();
        checkLogsDirectory();
        // Caricamento delle immagini
        UI_Config.setShieldImage(loadImage(UI_Config.getSHIELD_IMAGE_PATH()));
        UI_Config.setAsciiFlipper(loadImage(UI_Config.getASCII_FLIPPER_PATH()));

        // Caricamento dei font
        UI_Config.setNormalFont(loadFont(UI_Config.getFONT_NORMAL_PATH()));
        UI_Config.setBoldFont(loadFont(UI_Config.getFONT_BOLD_PATH()));
        UI_Config.setItalicFont(loadFont(UI_Config.getFONT_ITALIC_PATH()));
    }
}
