package com.mycompany.poggioadventure.ui;

import com.mycompany.poggioadventure.ui.gui.views.UI_Config;
import com.formdev.flatlaf.FlatLightLaf; // Libreria per il look moderno FlatLaf
import com.mycompany.poggioadventure.core.utils.Utils; // Utility generali
import com.mycompany.poggioadventure.ui.gui.GUIErrorHandler; // Gestione errori GUI-specifica
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Classe astratta base per tutte le finestre GUI dell'applicazione.
 * Implementa un template pattern per l'inizializzazione delle finestre,
 * centralizzando la configurazione comune e delegando parti specifiche alle sottoclassi.
 */
public abstract class UI_Abstract extends JFrame {

    /**
     * Costruttore che avvia la catena di inizializzazione:
     * 1. Configurazione tema UI
     * 2. Impostazioni base della finestra
     * 3. Inizializzazione componenti specifici (delegato alle sottoclassi)
     */
    public UI_Abstract() {
        initUI(); // Template method
    }

    /**
     * Metodo astratto che forza le sottoclassi a implementare
     * la logica di creazione dei componenti specifici.
     */
    protected abstract void initComponents();

    /**
     * Template method che definisce il flusso di inizializzazione:
     * 1. Setup tema FlatLaf
     * 2. Configurazione parametri JFrame
     * 3. Inizializzazione componenti
     * 4. Applicazione stili aggiuntivi
     */
    private void initUI() {
        try {
            FlatLightLaf.setup(); // Configura il tema FlatLaf light
        } catch (Exception ex) {
            // Gestione errori centralizzata con fallback grafico
            ErrorHandler errorHandler = new GUIErrorHandler();
            errorHandler.handleFatalError("Errore nel caricamento dello stile UI", ex);
            Utils.exitApplication(Utils.EXIT_CODE_CRITICAL);
        }
        
        // Configurazione base della finestra
        setTitle(getWindowTitle()); // Titolo dinamico
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Comportamento chiusura
        getContentPane().setLayout(new BorderLayout(10, 10)); // Layout principale
        getContentPane().setBackground(UI_Config.BACKGROUND_COLOR); // Colore sfondo
        setIconImage(UI_Config.getShieldImage()); // Icona applicazione
        initComponents(); // Hook per le sottoclassi
        applyDialogStyles(); // Stili per i dialoghi
        setResizable(false); // Finestra non ridimensionabile
        setLocationRelativeTo(null); // Centra sullo schermo
    }

    /**
     * Metodo astratto per ottenere il titolo dinamico della finestra.
     * @return Stringa col titolo specifico della finestra
     */
    protected abstract String getWindowTitle();

    /**
     * Factory method per la creazione di pulsanti standardizzati.
     * @param text Testo del pulsante
     * @param action Listener per l'azione
     * @return JButton configurato con lo stile dell'applicazione
     */
    protected JButton createCommonButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(UI_Config.getNormalFont().deriveFont(14f)); // Font personalizzato
        button.setForeground(UI_Config.TEXT_COLOR); // Colore testo
        button.setBackground(UI_Config.BUTTON_BASE_COLOR); // Colore sfondo
        button.setFocusPainted(false); // Rimuove bordo focus
        button.addActionListener(action); // Collegamento azione
        return button;
    }
    
    /**
     * Applica stili globali ai componenti Swing tramite UIManager.
     * Garantisce coerenza visiva in tutta l'applicazione.
     */
    private void applyDialogStyles() {
        UIManager.put("OptionPane.background", UI_Config.BACKGROUND_COLOR);
        UIManager.put("Panel.background", UI_Config.BACKGROUND_COLOR);
        UIManager.put("Button.background", UI_Config.BUTTON_BASE_COLOR);
        UIManager.put("Button.foreground", UI_Config.TEXT_COLOR);
        UIManager.put("OptionPane.messageFont", UI_Config.getNormalFont().deriveFont(14f));
        UIManager.put("OptionPane.messageForeground", UI_Config.TEXT_COLOR);
    }
}