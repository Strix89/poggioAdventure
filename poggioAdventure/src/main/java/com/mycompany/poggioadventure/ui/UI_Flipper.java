package com.mycompany.poggioadventure.ui;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class UI_Flipper extends JFrame {

    private JTextField inputField;
    private JButton sendButton;
    private JLabel imageLabel;

    public UI_Flipper() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Flipper Zero");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false); // Finestra non ridimensionabile
        getContentPane().setBackground(UI_Config.BACKGROUND_COLOR);
        setLayout(new BorderLayout(10, 10));

        // 1. PANNELLO CENTRALE - IMMAGINE
        BufferedImage originalImage = UI_Config.getAsciiImage();
        
        // Scala l'immagine mantenendo le proporzioni (max width 600px)
        int maxWidth = 600;
        int scaledWidth = Math.min(originalImage.getWidth(), maxWidth);
        int scaledHeight = (int) ((double) originalImage.getHeight() / originalImage.getWidth() * scaledWidth);
        
        Image scaledImage = originalImage.getScaledInstance(
            scaledWidth, 
            scaledHeight, 
            Image.SCALE_SMOOTH
        );

        imageLabel = new JLabel(new ImageIcon(scaledImage));
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UI_Config.BACKGROUND_COLOR);
        add(scrollPane, BorderLayout.CENTER);

        // 2. PANNELLO INFERIORE - INPUT E PULSANTE
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(UI_Config.BACKGROUND_COLOR);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        inputField = new JTextField();
        inputField.setFont(UI_Config.getNormalFont().deriveFont(14f));
        inputField.setForeground(Color.ORANGE);
        inputField.setBackground(UI_Config.BUTTON_BASE_COLOR);
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        bottomPanel.add(inputField, BorderLayout.CENTER);

        sendButton = new JButton("Invia");
        sendButton.setFont(UI_Config.getBoldFont().deriveFont(14f));
        sendButton.setForeground(UI_Config.TEXT_COLOR);
        sendButton.setBackground(UI_Config.BUTTON_BASE_COLOR);
        sendButton.setFocusPainted(false);
        sendButton.addActionListener(e -> onSendButtonClicked());
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        // Adatta la finestra all'immagine
        setSize(new Dimension(
            scaledWidth + 40, // Larghezza immagine + padding
            scaledHeight + 100 // Altezza immagine + spazio per input
        ));
        setLocationRelativeTo(null); // Centra la finestra
    }

    private void onSendButtonClicked() {
        String inputText = inputField.getText();
        if (!inputText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Hai inserito: " + inputText,
                "Input Ricevuto",
                JOptionPane.INFORMATION_MESSAGE);
            inputField.setText("");
        } else {
            JOptionPane.showMessageDialog(this,
                "Il campo di testo è vuoto!",
                "Errore",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        EventQueue.invokeLater(() -> {
            UI_Flipper window = new UI_Flipper();
            window.setVisible(true);
        });
    }
}