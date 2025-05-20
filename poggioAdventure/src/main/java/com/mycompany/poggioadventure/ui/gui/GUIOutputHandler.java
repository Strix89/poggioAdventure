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

public class GUIOutputHandler implements OutputHandler {

    private final JTextPane outputPane;

    public GUIOutputHandler(JTextPane outputPane) {
        if (outputPane == null) {
            throw new IllegalArgumentException("Il componente di output non può essere null");
        }
        this.outputPane = outputPane;
    }

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

            // Processa il blocco colorato
            try {
                currentColor = ColorText.fromString(matcher.group(1));
                String content = matcher.group(2);
                addTextSegment(doc, content, currentColor);
            } catch (IllegalArgumentException e) {
                // Se il colore non esiste, aggiunge il contenuto originale
                addTextSegment(doc, matcher.group(0), baseColor);
            }

            lastPos = matcher.end();
        }

        // Testo rimanente fuori da blocchi colorati
        if (lastPos < text.length()) {
            String remaining = text.substring(lastPos);
            addTextSegment(doc, remaining, baseColor);
        }
    }

    private void addTextSegment(StyledDocument doc, String text, ColorText color) 
        throws BadLocationException {
        
        if (!text.isEmpty()) {
            Style style = doc.addStyle(color.name(), null);
            StyleConstants.setForeground(style, color.getSwingColor());
            doc.insertString(doc.getLength(), text, style);
        }
    }

    private void handleImage(String imagePath, StyledDocument doc) {
        try {
            BufferedImage image = ResourceLoader.loadImage(imagePath);
            Image scaled = scaleImage(image, 300);
            ImageIcon icon = new ImageIcon(scaled);

            Style style = doc.addStyle("ImageStyle", null);
            StyleConstants.setIcon(style, icon);
            // Rimuoviamo l'allineamento al centro
            // StyleConstants.setAlignment(style, StyleConstants.ALIGN_CENTER);

            // Aggiunge una riga vuota prima dell'immagine
            if (doc.getLength() > 0 && !doc.getText(doc.getLength() - 1, 1).equals("\n")) {
                doc.insertString(doc.getLength(), "\n", null);
            }

            // Inseriamo un singolo spazio con l'icona
            doc.insertString(doc.getLength(), " ", style);

            // Aggiunge una riga vuota dopo l'immagine
            doc.insertString(doc.getLength(), "\n", null);
        } catch (IOException | BadLocationException ex) {
            new GUIErrorHandler().handleRecoverableError("Immagine non trovata: " + imagePath);
        }
    }

    private Image scaleImage(BufferedImage original, int targetWidth) {
        double ratio = (double) targetWidth / original.getWidth();
        int newHeight = (int) (original.getHeight() * ratio);
        return original.getScaledInstance(targetWidth, newHeight, Image.SCALE_SMOOTH);
    }

    @Override
    public void write(String message, ColorText color) {
        writeFormatted(message, color);
    }
    
    @Override
    public void write(String message){
        writeFormatted(message, ColorText.RESET);
    }

    @Override
    public void writeln(String message, ColorText color) {
        writeFormatted(message + "\n", color);
    }
    
    @Override
    public void writeln(String message) {
        writeFormatted(message + "\n", ColorText.RESET);
    }

    @Override
    public void writeln() {
        writeFormatted("\n", ColorText.RESET);
    }

    @Override
    public void clear() {
        SwingUtilities.invokeLater(() -> outputPane.setText(""));
    }
}