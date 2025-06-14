package com.mycompany.poggioadventure.ui.cli;

import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.ui.OutputHandler;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;

/**
 * Implementazione CLI per output console con processing avanzato colori ANSI.
 * 
 * <p>Gestisce rendering testo colorato tramite escape sequences ANSI con supporto
 * per markup inline e filtering contenuti non compatibili CLI. Implementa
 * parsing regex per color blocks e gestione automatica reset sequences.
 * 
 * <p><b>Funzionalità principali:</b>
 * <ul>
 *   <li>Processing markup color blocks format [COLOR]text[/]</li>
 *   <li>Filtering IMAGE tags per compatibilità CLI</li>
 *   <li>Gestione automatica ANSI reset sequences</li>
 *   <li>Preservazione line endings e formatting originale</li>
 *   <li>Fallback graceful per colori non riconosciuti</li>
 * </ul>
 * 
 * <p><b>Pattern:</b> Strategy per output CLI, Template Method per
 * processing formattazione, Adapter per ANSI terminal compatibility.
 */
public class CLIOutputHandler implements OutputHandler {

    /**
     * Core method per rendering testo formattato con color processing.
     * Processa line-by-line per gestire newlines correttamente e filtra
     * contenuti incompatibili con CLI (es. IMAGE tags).
     * 
     * @param formattedMessage Testo con markup colori da processare
     * @param baseColor Colore default per testo senza markup specifico
     */
    @Override
    public void writeFormatted(String formattedMessage, ColorText baseColor) {
        StringBuilder output = new StringBuilder();
        String[] lines = formattedMessage.split("\n", -1);
        List<String> lineList = new ArrayList<>(List.of(lines));

        // Cleanup trailing empty line se presente
        if (!lineList.isEmpty() && lineList.get(lineList.size()-1).isEmpty()) {
            lineList.remove(lineList.size()-1);
        }

        for (int i = 0; i < lineList.size(); i++) {
            String line = lineList.get(i);
            boolean isLastLine = (i == lineList.size() - 1);
            
            // Skip IMAGE tags non compatibili con CLI
            if (IMAGE_TAG_PATTERN.matcher(line).matches()) {
                continue;
            }
            
            String processedLine = processColorBlocks(line, baseColor);
            output.append(processedLine);
            
            // Preserva newlines originali eccetto ultima linea se non termina con \n
            if (!isLastLine || formattedMessage.endsWith("\n")) {
                output.append("\n");
            }
        }
        
        System.out.print(output.toString());
    }

    /**
     * Processa color blocks inline con regex matching e ANSI code insertion.
     * Format supportato: [COLOR_NAME]text[/] convertito in escape sequences.
     * Gestisce nesting e fallback per colori non validi.
     * 
     * @param line Linea singola da processare
     * @param baseColor Colore default per testo normale
     * @return Linea con ANSI codes applicati
     */
    private String processColorBlocks(String line, ColorText baseColor) {
        Matcher matcher = COLOR_BLOCK_PATTERN.matcher(line);
        StringBuilder result = new StringBuilder();
        int lastPos = 0;
        ColorText currentColor = baseColor;

        while (matcher.find()) {
            // Applica base color a testo precedente il block
            if (lastPos < matcher.start()) {
                result.append(baseColor.getANSICode());
                result.append(line.substring(lastPos, matcher.start()));
                result.append(ColorText.RESET.getANSICode());
            }

            // Processing color block con validation
            try {
                currentColor = ColorText.fromString(matcher.group(1));
                result.append(currentColor.getANSICode());
                result.append(matcher.group(2)); // Contenuto block
                result.append(ColorText.RESET.getANSICode());
            } catch (IllegalArgumentException e) {
                // Fallback per colori non riconosciuti: mantieni testo originale
                result.append(matcher.group(0));
            }

            lastPos = matcher.end();
        }

        // Applica base color a testo rimanente
        if (lastPos < line.length()) {
            result.append(baseColor.getANSICode());
            result.append(line.substring(lastPos));
            result.append(ColorText.RESET.getANSICode());
        }

        return result.toString();
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
    public void writeln(String message) {
        writeFormatted(message + "\n", ColorText.RESET);
    }

    /** Output newline singolo */
    @Override
    public void writeln() {
        writeFormatted("\n", ColorText.RESET);
    }

    /** Clear screen usando ANSI escape sequences */
    @Override
    public void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}