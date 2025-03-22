package com.mycompany.poggioadventure.ui;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Classe che rappresenta una finestra per l'interfaccia utente del "Flipper Zero".
 * Mostra un'immagine ASCII scalata e include un campo di input con un pulsante
 * per inviare comandi. Estende la classe astratta UI_Abstract per ereditare
 * la struttura di base dell'interfaccia grafica.
 */
public class UI_Flipper extends UI_Abstract {

    // Componenti dell'interfaccia utente
    private JTextField inputField;  // Campo di testo per l'inserimento dei comandi
    private JButton sendButton;     // Pulsante per inviare i comandi
    private JLabel imageLabel;     // Etichetta per visualizzare l'immagine ASCII

    /**
     * Costruttore della classe. Chiama il costruttore della superclasse UI_Abstract
     * per inizializzare l'interfaccia grafica.
     */
    public UI_Flipper() {
        super();
    }

    /**
     * Implementazione del metodo astratto initComponents() della superclasse UI_Abstract.
     * Inizializza tutti i componenti dell'interfaccia utente, inclusi l'immagine ASCII,
     * il campo di input e il pulsante di invio.
     */
    @Override
    protected void initComponents() {
        setLayout(new BorderLayout(10, 10));  // Imposta il layout principale della finestra

        // 1. PANNELLO CENTRALE - IMMAGINE ASCII
        BufferedImage originalImage = UI_Config.getAsciiImage();  // Ottiene l'immagine ASCII dalla configurazione

        // Scala l'immagine mantenendo le proporzioni (larghezza massima 600px)
        int maxWidth = 600;  // Larghezza massima dell'immagine
        int scaledWidth = Math.min(originalImage.getWidth(), maxWidth);  // Calcola la larghezza scalata
        int scaledHeight = (int) ((double) originalImage.getHeight() / originalImage.getWidth() * scaledWidth);  // Calcola l'altezza proporzionale

        // Ridimensiona l'immagine in modo fluido
        Image scaledImage = originalImage.getScaledInstance(
            scaledWidth, 
            scaledHeight, 
            Image.SCALE_SMOOTH  // Algoritmo di ridimensionamento ad alta qualità
        );

        // Crea un'etichetta per visualizzare l'immagine scalata
        imageLabel = new JLabel(new ImageIcon(scaledImage));
        imageLabel.setHorizontalAlignment(JLabel.CENTER);  // Allinea l'immagine al centro

        // Aggiunge l'immagine a uno JScrollPane per supportare lo scrolling se necessario
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        scrollPane.setBorder(null);  // Rimuove il bordo predefinito
        scrollPane.getViewport().setBackground(UI_Config.BACKGROUND_COLOR);  // Imposta il colore di sfondo
        add(scrollPane, BorderLayout.CENTER);  // Aggiunge il pannello al centro della finestra

        // 2. PANNELLO INFERIORE - INPUT E PULSANTE
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));  // Crea un pannello per input e pulsante
        bottomPanel.setBackground(UI_Config.BACKGROUND_COLOR);  // Imposta il colore di sfondo
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));  // Aggiunge un padding interno

        // Configurazione del campo di input
        inputField = new JTextField();
        inputField.setFont(UI_Config.getNormalFont().deriveFont(14f));  // Imposta il font
        inputField.setForeground(Color.ORANGE);  // Colore del testo
        inputField.setBackground(UI_Config.BUTTON_BASE_COLOR);  // Colore di sfondo
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 1),  // Bordo esterno
            BorderFactory.createEmptyBorder(5, 5, 5, 5)  // Padding interno
        ));
        bottomPanel.add(inputField, BorderLayout.CENTER);  // Aggiunge il campo di input al pannello

        // Configurazione del pulsante di invio
        sendButton = new JButton("Invia");
        sendButton.setFont(UI_Config.getBoldFont().deriveFont(14f));  // Imposta il font in grassetto
        sendButton.setForeground(UI_Config.TEXT_COLOR);  // Colore del testo
        sendButton.setBackground(UI_Config.BUTTON_BASE_COLOR);  // Colore di sfondo
        sendButton.setFocusPainted(false);  // Disabilita l'effetto di focus
        sendButton.addActionListener(e -> onSendButtonClicked());  // Aggiunge l'azione al pulsante
        bottomPanel.add(sendButton, BorderLayout.EAST);  // Aggiunge il pulsante al pannello

        add(bottomPanel, BorderLayout.SOUTH);  // Aggiunge il pannello inferiore alla finestra

        // Imposta le dimensioni della finestra in base all'immagine scalata
        setSize(new Dimension(
            scaledWidth + 40,  // Larghezza immagine + padding laterale
            scaledHeight + 100 // Altezza immagine + spazio per il pannello inferiore
        ));
    }

    /**
     * Implementazione del metodo astratto getWindowTitle() della superclasse UI_Abstract.
     * Restituisce il titolo della finestra.
     *
     * @return Stringa contenente il titolo della finestra
     */
    @Override
    protected String getWindowTitle() {
        return "Flipper Zero";
    }

    /**
     * Metodo chiamato quando viene cliccato il pulsante "Invia".
     * Gestisce l'input dell'utente e mostra un messaggio di conferma o errore.
     */
    private void onSendButtonClicked() {
        String inputText = inputField.getText().trim();  // Ottiene il testo inserito dall'utente
        if (!inputText.isEmpty()) {
            // Mostra un messaggio di conferma con il testo inserito
            JOptionPane.showMessageDialog(this,
                "Hai inserito: " + inputText,
                "Input Ricevuto",
                JOptionPane.INFORMATION_MESSAGE);
            inputField.setText("");  // Resetta il campo di input
        } else {
            // Mostra un messaggio di errore se il campo è vuoto
            JOptionPane.showMessageDialog(this,
                "Il campo di testo è vuoto!",
                "Errore",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Metodo main per avviare l'applicazione.
     *
     * @param args Argomenti della riga di comando (non utilizzati)
     */
    public static void main(String[] args) {
        FlatLightLaf.setup();  // Configura il tema FlatLaf light per l'interfaccia
        EventQueue.invokeLater(() -> {
            UI_Flipper window = new UI_Flipper();  // Crea una nuova finestra
            window.setVisible(true);  // Rende la finestra visibile
        });
    }
}