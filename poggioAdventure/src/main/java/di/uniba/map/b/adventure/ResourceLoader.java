package di.uniba.map.b.adventure;

import com.mycompany.poggioadventure.ui.UI_Config;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ResourceLoader {

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

    // Carica tutte le risorse necessarie
    public static void loadUIResources() throws IOException, FontFormatException {
        // Caricamento delle immagini
        UI_Config.setShieldImage(loadImage(UI_Config.getSHIELD_IMAGE_PATH()));
        UI_Config.setAsciiFlipper(loadImage(UI_Config.getASCII_FLIPPER_PATH()));

        // Caricamento dei font
        UI_Config.setNormalFont(loadFont(UI_Config.getFONT_NORMAL_PATH()));
        UI_Config.setBoldFont(loadFont(UI_Config.getFONT_BOLD_PATH()));
        UI_Config.setItalicFont(loadFont(UI_Config.getFONT_ITALIC_PATH()));
    }
}
