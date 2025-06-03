package com.mycompany.poggioadventure.ui.cli;

import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.ui.OutputHandler;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;

public class CLIOutputHandler implements OutputHandler {

    @Override
    public void writeFormatted(String formattedMessage, ColorText baseColor) {
        StringBuilder output = new StringBuilder();
        String[] lines = formattedMessage.split("\n", -1);
        List<String> lineList = new ArrayList<>(List.of(lines));

        // Rimuove l'ultimo elemento se è una riga vuota
        if (!lineList.isEmpty() && lineList.get(lineList.size()-1).isEmpty()) {
            lineList.remove(lineList.size()-1);
        }

        for (int i = 0; i < lineList.size(); i++) {
            String line = lineList.get(i);
            boolean isLastLine = (i == lineList.size() - 1);
            
            if (IMAGE_TAG_PATTERN.matcher(line).matches()) {
                continue;
            }
            
            String processedLine = processColorBlocks(line, baseColor);
            output.append(processedLine);
            
            if (!isLastLine || formattedMessage.endsWith("\n")) {
                output.append("\n");
            }
        }
        
        System.out.print(output.toString());
    }

    private String processColorBlocks(String line, ColorText baseColor) {
        Matcher matcher = COLOR_BLOCK_PATTERN.matcher(line);
        StringBuilder result = new StringBuilder();
        int lastPos = 0;
        ColorText currentColor = baseColor; // Colore iniziale

        while (matcher.find()) {
            // Aggiunge il testo prima del blocco colorato con il colore corrente
            if (lastPos < matcher.start()) {
                result.append(baseColor.getANSICode()); // Applica il colore base
                result.append(line.substring(lastPos, matcher.start()));
                result.append(ColorText.RESET.getANSICode()); // Resetta dopo il testo normale
            }

            // Applica il colore del blocco
            try {
                currentColor = ColorText.fromString(matcher.group(1));
                result.append(currentColor.getANSICode());
                result.append(matcher.group(2)); // Contenuto del blocco
                result.append(ColorText.RESET.getANSICode());
            } catch (IllegalArgumentException e) {
                // Se il colore non esiste, aggiunge solo il contenuto originale
                result.append(matcher.group(0));
            }

            lastPos = matcher.end();
        }

    // Aggiunge il testo rimanente con il colore base
    if (lastPos < line.length()) {
        result.append(baseColor.getANSICode());
        result.append(line.substring(lastPos));
        result.append(ColorText.RESET.getANSICode());
    }

    return result.toString();
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
    
    public void writeln(String message) {
        writeFormatted(message + "\n", ColorText.RESET);
    }

    @Override
    public void writeln() {
        writeFormatted("\n", ColorText.RESET);
    }

    @Override
    public void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}