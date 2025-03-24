package com.mycompany.poggioadventure.ui;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Classe astratta che rappresenta una finestra base dell'interfaccia grafica.
 * Definisce una struttura comune per l'inizializzazione dell'UI e metodi utility,
 * lasciando l'implementazione specifica di alcuni elementi alle sottoclassi.
 */
public abstract class UI_Abstract extends JFrame {

    /**
     * Costruttore principale che avvia l'inizializzazione dell'interfaccia grafica.
     */
    public UI_Abstract() {
        initUI();
    }

    /**
     * Metodo astratto da implementare nelle sottoclassi per inizializzare
     * i componenti specifici della finestra.
     */
    protected abstract void initComponents();

    /**
     * Inizializza i parametri base della finestra:
     * - Titolo della finestra
     * - Comportamento alla chiusura
     * - Layout principale
     * - Colore di sfondo
     * - Altre proprietà generali
     */
    private void initUI() {
        try {
            FlatLightLaf.setup();  // Configura il tema FlatLaf light per l'interfaccia
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, 
                "Errore nel caricamento dello stile UI", 
                "Errore", 
                JOptionPane.ERROR_MESSAGE);
            UI_Config.getExitDefaultOp();
        }
        setTitle(getWindowTitle());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UI_Config.BACKGROUND_COLOR);
        setIconImage(UI_Config.getShieldImage());
        initComponents();
        applyDialogStyles();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    /**
     * Metodo astratto per ottenere il titolo della finestra.
     * @return Stringa contenente il titolo da visualizzare
     */
    protected abstract String getWindowTitle();

    /**
     * Crea un pulsante con lo stile visivo standard dell'applicazione.
     * @param text Testo da visualizzare sul pulsante
     * @param action ActionListener da associare al pulsante
     * @return JButton configurato con lo stile comune
     */
    protected JButton createCommonButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(UI_Config.getNormalFont().deriveFont(14f));
        button.setForeground(UI_Config.TEXT_COLOR);
        button.setBackground(UI_Config.BUTTON_BASE_COLOR);
        button.setFocusPainted(false);
        button.addActionListener(action);
        return button;
    }
    
    /**
     * Applica le impostazioni di stile personalizzate ai dialoghi Swing.
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