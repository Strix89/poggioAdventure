package com.mycompany.poggioadventure.ui.gui;

import com.mycompany.poggioadventure.persistence.ResourceLoader;
import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.ui.OutputHandler;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Implementazione GUI per output formattato con supporto rich text e immagini.
 * 
 * <p>Gestisce rendering avanzato in JTextPane con processing markup colori,
 * embedding immagini scalate e gestione thread-safe via Event Dispatch Thread.
 * Supporta StyledDocument per formattazione complessa e auto-scrolling.
 * 
 * <p><b>Funzionalità principali:</b>
 * <ul>
 *   <li>Processing markup color blocks per Swing color mapping</li>
 *   <li>Embedding immagini con scaling automatico proporzionale</li>
 *   <li>Thread-safe updates via SwingUtilities.invokeLater</li>
 *   <li>StyledDocument manipulation per rich text formatting</li>
 *   <li>Auto-caret positioning per scroll automatico</li>
 * </ul>
 * 
 * <p><b>Pattern:</b> Strategy per output GUI, Decorator per text styling,
 * Observer pattern tramite EDT per UI updates thread-safe.
 */
public class GUIOutputHandler implements OutputHandler {

    /** JTextPane target per rendering output formattato */
    private final JTextPane outputPane;

    /**
     * Inizializza handler con validazione componente target.
     * 
     * @param outputPane JTextPane per output rendering
     * @throws IllegalArgumentException Se outputPane è null
     */
    public GUIOutputHandler(JTextPane outputPane) {
        if (outputPane == null) {
            throw new IllegalArgumentException("Il componente di output non può essere null");
        }
        this.outputPane = outputPane;
    }

    /**
     * Core method per rendering thread-safe con StyledDocument manipulation.
     * Processa line-by-line per gestire newlines, immagini e color blocks.
     * Utilizza EDT per garantire thread safety nelle operazioni UI.
     * 
     * @param formattedMessage Testo con markup da renderizzare
     * @param baseColor Colore default per testo senza markup
     */
    @Override
    public void writeFormatted(String formattedMessage, ColorText baseColor) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = outputPane.getStyledDocument();
                String[] lines = formattedMessage.split("\n", -1);
                List<String> lineList = new ArrayList<>(List.of(lines));

                // Rimuove l'ultimo elemento se è una riga vuota (dovuta allo split con -1)
                if (!lineList.isEmpty() && lineList.get(lineList.size()-1).isEmpty()) {
                    lineList.remove(lineList.size()-1);
                }

                for (int i = 0; i < lineList.size(); i++) {
                    String line = lineList.get(i);
                    boolean isLastLine = (i == lineList.size() - 1);
                    
                    if (IMAGE_TAG_PATTERN.matcher(line).matches()) {
                        handleImage(line.substring(6).trim(), doc);
                    } else {
                        processStyledText(line, doc, baseColor);
                    }
                    
                    // Aggiunge newline solo se non è l'ultima riga o il messaggio originale termina con \n
                    if (!isLastLine || formattedMessage.endsWith("\n")) {
                        doc.insertString(doc.getLength(), "\n", null);
                    }
                }
                outputPane.setCaretPosition(doc.getLength());
            } catch (BadLocationException ex) {
                new GUIErrorHandler().handleRecoverableError("Errore di visualizzazione: " + ex.getMessage());
            }
        });
    }

    /**
     * Processa text segments con color blocks usando regex matching.
     * Applica Swing Style attributes per ogni segmento colorato identificato.
     * 
     * @param text Testo da processare per color styling
     * @param doc StyledDocument target per inserimento
     * @param baseColor Colore default per segmenti non marcati
     * @throws BadLocationException Se inserimento document fallisce
     */
    private void processStyledText(String text, StyledDocument doc, ColorText baseColor) 
        throws BadLocationException {
        
        Matcher matcher = COLOR_BLOCK_PATTERN.matcher(text);
        int lastPos = 0;
        ColorText currentColor = baseColor;

        while (matcher.find()) {
            // Testo prima del blocco colorato
            if (lastPos < matcher.start()) {
                String segment = text.substring(lastPos, matcher.start());
                addTextSegment(doc, segment, baseColor);
            }
            currentColor = ColorText.fromString(matcher.group(1));
            String content = matcher.group(2);
            addTextSegment(doc, content, currentColor);

            lastPos = matcher.end();
        }

        // Testo rimanente fuori da blocchi colorati
        if (lastPos < text.length()) {
            String remaining = text.substring(lastPos);
            addTextSegment(doc, remaining, baseColor);
        }
    }

    /**
     * Inserisce text segment con Style attributes specifici per colore.
     * Crea Style dinamico con mapping ColorText -> Swing Color.
     * 
     * @param doc StyledDocument target
     * @param text Testo da inserire
     * @param color Colore per styling del testo
     * @throws BadLocationException Se inserimento fallisce
     */
    private void addTextSegment(StyledDocument doc, String text, ColorText color) 
        throws BadLocationException {
        
        if (!text.isEmpty()) {
            Style style = doc.addStyle(color.name(), null);
            StyleConstants.setForeground(style, color.getSwingColor());
            doc.insertString(doc.getLength(), text, style);
        }
    }

    /**
     * Gestisce embedding immagini con scaling automatico e error handling.
     * Carica immagine via ResourceLoader, applica scaling proporzionale
     * e inserisce come ImageIcon in StyledDocument.
     * 
     * @param imagePath Path relativo immagine da caricare
     * @param doc StyledDocument per inserimento icona
     */
    private void handleImage(String imagePath, StyledDocument doc) {
        try {
            BufferedImage image = ResourceLoader.loadImage(imagePath);
            Image scaled = scaleImage(image, 300);
            ImageIcon icon = new ImageIcon(scaled);

            Style style = doc.addStyle("ImageStyle_" + imagePath.hashCode(), null); // Usa un nome di stile univoco
            StyleConstants.setIcon(style, icon);

            // Aggiunge una riga vuota prima dell'immagine
            if (doc.getLength() > 0 && !doc.getText(doc.getLength() - 1, 1).equals("\n")) {
                doc.insertString(doc.getLength(), "\n", null);
            }

            // Inseriamo un singolo spazio con l'icona
            doc.insertString(doc.getLength(), " ", style);

            // Aggiunge una riga vuota dopo l'immagine
            doc.insertString(doc.getLength(), "\n", null);
            outputPane.setCaretPosition(doc.getLength()); // Assicurati che anche questo sia qui se necessario
        } catch (IOException | BadLocationException ex) {
            // Considera di mostrare l'errore anche tramite SwingUtilities.invokeLater se GUIErrorHandler lo richiede
            new GUIErrorHandler().handleRecoverableError("Immagine non trovata: " + imagePath);
        }
    }

    /**
     * Applica scaling proporzionale mantenendo aspect ratio originale.
     * Utilizza SCALE_SMOOTH per qualità ottimale rendering.
     * 
     * @param original BufferedImage sorgente
     * @param targetWidth Larghezza target per scaling
     * @return Image scalata con aspect ratio preservato
     */
    private Image scaleImage(BufferedImage original, int targetWidth) {
        double ratio = (double) targetWidth / original.getWidth();
        int newHeight = (int) (original.getHeight() * ratio);
        return original.getScaledInstance(targetWidth, newHeight, Image.SCALE_SMOOTH);
    }

    /** Wrapper per output singolo con colore specifico */
    @Override
    public void write(String message, ColorText color) {
        writeFormatted(message, color);
    }
    
    /** Wrapper per output singolo con colore default */
    @Override
    public void write(String message){
        writeFormatted(message, ColorText.RESET);
    }

    /** Wrapper per output con newline e colore specifico */
    @Override
    public void writeln(String message, ColorText color) {
        writeFormatted(message + "\n", color);
    }
    
    /** Wrapper per output con newline e colore default */
    @Override
    public void writeln(String message) {
        writeFormatted(message + "\n", ColorText.RESET);
    }

    /** Output newline singolo */
    @Override
    public void writeln() {
        writeFormatted("\n", ColorText.RESET);
    }

    /** Clear completo JTextPane content via EDT */
    @Override
    public void clear() {
        SwingUtilities.invokeLater(() -> outputPane.setText(""));
    }
}