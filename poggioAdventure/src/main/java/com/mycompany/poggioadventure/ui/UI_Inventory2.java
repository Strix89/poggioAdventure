package com.mycompany.poggioadventure.ui;

import com.formdev.flatlaf.FlatLightLaf;
import di.uniba.map.b.adventure.type.AdvObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class UI_Inventory2 extends JFrame {
    private JScrollPane objectsScroller;
    private JTextArea descriptionArea;
    private JButton escButton;
    private JPanel imageObjects;

    public UI_Inventory2() {
        initComponents();
    }

    private void initComponents() {
        // Configurazione generale della finestra
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("INVENTARIO");
        setSize(650, 480);
        setResizable(false);
        getContentPane().setBackground(new Color(45, 45, 45)); // Sfondo scuro
        
        // Layout principale con margini
        setLayout(new BorderLayout(15, 15));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. PANNELLO SINISTRA - LISTA OGGETTI
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(new Color(70, 70, 70));
        listPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        
        JLabel listTitle = new JLabel("INVENTARIO");
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        listTitle.setForeground(Color.WHITE);
        listTitle.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        listPanel.add(listTitle, BorderLayout.NORTH);
        
        objectsScroller = new JScrollPane();
        objectsScroller.setBorder(null);
        objectsScroller.getViewport().setBackground(new Color(90, 90, 90));
        listPanel.add(objectsScroller, BorderLayout.CENTER);
        
        add(listPanel, BorderLayout.WEST);

        // 2. PANNELLO DESTRA - IMMAGINE E DESCRIZIONE
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(45, 45, 45));
        GridBagConstraints gbc = new GridBagConstraints();
        
        // 2a. PANNELLO IMMAGINE
        imageObjects = new JPanel(new BorderLayout());
        imageObjects.setBackground(new Color(30, 30, 30));
        imageObjects.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        imageObjects.setPreferredSize(new Dimension(300, 250));
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0.6;
        gbc.fill = GridBagConstraints.BOTH;
        rightPanel.add(imageObjects, gbc);

        // 2b. PANNELLO DESCRIZIONE
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.setBackground(new Color(30, 30, 30));
        descPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        descriptionArea = new JTextArea();
        descriptionArea.setBackground(new Color(60, 60, 60));
        descriptionArea.setForeground(Color.WHITE);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(null);
        descPanel.add(descScroll, BorderLayout.CENTER);
        
        gbc.gridy = 1;
        gbc.weighty = 0.4;
        rightPanel.add(descPanel, gbc);

        // 2c. PULSANTE ESCI
        escButton = new JButton("ESCI");
        escButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        escButton.setBackground(new Color(100, 100, 100));
        escButton.setForeground(Color.WHITE);
        escButton.setFocusPainted(false);
        escButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        escButton.addActionListener(e -> System.exit(0));
        
        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 0, 0);
        rightPanel.add(escButton, gbc);

        add(rightPanel, BorderLayout.CENTER);
    }

    public void addObjectsToScroller(java.util.List<AdvObject> objects) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(90, 90, 90));

        for (AdvObject obj : objects) {
            JLabel label = new JLabel(" • " + obj.getName());
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setForeground(Color.WHITE);
            label.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showObjectDetails(obj);
                }
            });
            panel.add(label);
        }
        objectsScroller.setViewportView(panel);
    }

    private void showObjectDetails(AdvObject obj) {
        imageObjects.removeAll();
        imageObjects.setLayout(new BorderLayout());

        if (obj.getImagePath() != null && obj.getImagePath().exists()) {
            ImageIcon icon = new ImageIcon(obj.getImagePath().getPath());
            Image image = icon.getImage().getScaledInstance(280, 200, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(image));
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            imageObjects.add(imageLabel, BorderLayout.CENTER);
        } else {
            JLabel noImageLabel = new JLabel("Nessuna immagine disponibile");
            noImageLabel.setForeground(Color.WHITE);
            noImageLabel.setHorizontalAlignment(JLabel.CENTER);
            imageObjects.add(noImageLabel, BorderLayout.CENTER);
        }

        descriptionArea.setText(obj.getDescription());
        descriptionArea.setCaretPosition(0);
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        java.awt.EventQueue.invokeLater(() -> {
            UI_Inventory2 inventoryUI = new UI_Inventory2();
            inventoryUI.setVisible(true);

            // Esempio di dati (modifica con i tuoi percorsi reali)
            java.util.List<AdvObject> objects = new ArrayList<>();
            for(int i = 1; i <= 26; i++) {
                objects.add(new AdvObject(
                    i,
                    "Oggetto " + i,
                    "./resources/img/none.png",
                    "Descrizione dettagliata per l'oggetto " + i + ".\n\n" +
                    "Materiale: Speciale\nPeso: " + (i % 10 + 1) + "kg\nRarità: " + (i % 5 + 1) + "/5"
                ));
            }
            inventoryUI.addObjectsToScroller(objects);
        });
    }
}