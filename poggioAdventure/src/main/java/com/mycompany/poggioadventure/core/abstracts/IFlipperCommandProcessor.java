package com.mycompany.poggioadventure.core.abstracts;

import com.mycompany.poggioadventure.core.utils.FlipperResult;

/**
 * Interfaccia per processare comandi del Flipper Zero.
 * Seguendo il principio di Single Responsibility e Dependency Inversion.
 */
public interface IFlipperCommandProcessor {
    /**
     * Processa un comando del flipper.
     * @param input Il comando da processare
     * @return Il risultato dell'esecuzione
     */
    FlipperResult processCommand(String input);
    
    /**
     * Restituisce le informazioni sui comandi disponibili.
     */
    String getManualInfo();
}