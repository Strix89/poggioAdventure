package com.mycompany.poggioadventure.ui;

import com.formdev.flatlaf.FlatLightLaf;
import di.uniba.map.b.adventure.type.AdvObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class UI_Inventory extends JFrame {
    // Componenti UI
    private JScrollPane objectsScroller;  // Pannello scrollabile per la lista oggetti
    private JTextArea descriptionArea;    // Area di testo per la descrizione
    private JButton escButton;            // Pulsante per uscire dall'applicazione
    private JPanel imageObjects;          // Pannello per visualizzare l'immagine
    
    // Configurazioni
    private String nameWindow = "INVENTARIO";  // Nome default della finestra

    /**
     * Costruttore principale. Inizializza i componenti dell'interfaccia.
     */
    public UI_Inventory() {
        initComponents(); // Inizializzazione degli elementi grafici
    }
    
    /**
     * Costruttore alternativo che permette di impostare un nome personalizzato per la finestra.
     * Attenzione: Non chiama initComponents(), lasciando la finestra non inizializzata.
     * @param name Nome da assegnare alla finestra.
     */
    public UI_Inventory(String name) {
        nameWindow = name; // Potrebbe richiedere una chiamata a initComponents()
    }

    /**
     * Inizializza tutti i componenti grafici e configura il layout.
     * Metodo privato chiamato dal costruttore principale.
     */
    private void initComponents() {
        // Configurazione generale della finestra
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(nameWindow);
        setSize(650, 480); // Dimensioni fisse (come nella versione precedente)
        setResizable(false);
        getContentPane().setBackground(UI_Config.BACKGROUND_COLOR); // Sfondo scuro
        // Layout principale con margini
        setLayout(new BorderLayout(15, 15));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. PANNELLO SINISTRA - LISTA OGGETTI
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(new Color(70, 70, 70)); // Colore invariato
        listPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2)); // Bordo invariato
        JLabel listTitle = new JLabel(nameWindow);
        listTitle.setFont(UI_Config.getBoldFont().deriveFont(18f)); // Font da UI_Config
        listTitle.setForeground(UI_Config.TEXT_COLOR); // Colore del testo da UI_Config
        listTitle.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Spaziatura invariata
        listPanel.add(listTitle, BorderLayout.NORTH);
        objectsScroller = new JScrollPane();
        objectsScroller.setBorder(null);
        objectsScroller.getViewport().setBackground(new Color(90, 90, 90)); // Colore invariato
        listPanel.add(objectsScroller, BorderLayout.CENTER);
        add(listPanel, BorderLayout.WEST);

        // 2. PANNELLO DESTRA - IMMAGINE E DESCRIZIONE
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(UI_Config.BACKGROUND_COLOR); // Sfondo da UI_Config
        GridBagConstraints gbc = new GridBagConstraints();
        // 2a. PANNELLO IMMAGINE
        imageObjects = new JPanel(new BorderLayout());
        imageObjects.setBackground(new Color(30, 30, 30)); // Colore invariato
        imageObjects.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 2), // Bordo invariato
            BorderFactory.createEmptyBorder(5, 5, 5, 5) // Spaziatura invariata
        ));
        imageObjects.setPreferredSize(new Dimension(300, 250)); // Dimensione invariata
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0.6;
        gbc.fill = GridBagConstraints.BOTH;
        rightPanel.add(imageObjects, gbc);
        // 2b. PANNELLO DESCRIZIONE
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.setBackground(new Color(30, 30, 30)); // Colore invariato
        descPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 2), // Bordo invariato
            BorderFactory.createEmptyBorder(5, 5, 5, 5) // Spaziatura invariata
        ));
        descriptionArea = new JTextArea();
        descriptionArea.setBackground(new Color(60, 60, 60)); // Colore invariato
        descriptionArea.setForeground(UI_Config.TEXT_COLOR); // Colore del testo da UI_Config
        descriptionArea.setFont(UI_Config.getNormalFont().deriveFont(14f)); // Font da UI_Config
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
        escButton.setFont(UI_Config.getBoldFont().deriveFont(14f)); // Font da UI_Config
        escButton.setBackground(new Color(100, 100, 100)); // Colore invariato
        escButton.setForeground(UI_Config.TEXT_COLOR); // Colore del testo da UI_Config
        escButton.setFocusPainted(false);
        escButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15)); // Spaziatura invariata
        escButton.addActionListener(e -> System.exit(0));
        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 0, 0); // Spaziatura invariata
        rightPanel.add(escButton, gbc);
        add(rightPanel, BorderLayout.CENTER);
    }

    /**
     * Popola la lista degli oggetti nell'area scrollabile.
     * Ogni oggetto è rappresentato da una JLabel cliccabile.
     * @param objects Lista di AdvObject da visualizzare nell'inventario.
     */
    public void addObjectsToScroller(java.util.List<AdvObject> objects) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(90, 90, 90)); // Colore invariato

        // Calcola la larghezza massima del testo
        Font font = UI_Config.getNormalFont().deriveFont(12f);
        FontMetrics fontMetrics = getFontMetrics(font);
        int maxTextWidth = 0;

        for (AdvObject obj : objects) {
            String text = " • " + obj.getName();
            int textWidth = fontMetrics.stringWidth(text);
            if (textWidth > maxTextWidth) {
                maxTextWidth = textWidth;
            }

            JLabel label = new JLabel(text);
            label.setFont(font);
            label.setForeground(UI_Config.TEXT_COLOR);
            label.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showObjectDetails(obj);
                }
            });
            panel.add(label);
        }

        // Imposta la larghezza del pannello in base alla larghezza massima del testo
        int padding = 30; // Padding per evitare che il testo sia troppo vicino ai bordi
        int panelWidth = maxTextWidth + padding;
        panel.setPreferredSize(new Dimension(panelWidth, panel.getPreferredSize().height));

        objectsScroller.setViewportView(panel);
        objectsScroller.setPreferredSize(new Dimension(panelWidth, objectsScroller.getPreferredSize().height));
    }

    /**
     * Mostra i dettagli di un oggetto selezionato (immagine e descrizione).
     * @param obj L'oggetto di cui visualizzare i dettagli.
     */
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
            noImageLabel.setForeground(UI_Config.TEXT_COLOR); // Colore del testo da UI_Config
            noImageLabel.setHorizontalAlignment(JLabel.CENTER);
            imageObjects.add(noImageLabel, BorderLayout.CENTER);
        }
        descriptionArea.setText(obj.getDescription());
        descriptionArea.setCaretPosition(0);
        revalidate();
        repaint();
    }

    /**
     * Metodo main di esempio per testare l'interfaccia.
     * Da rimuovere in produzione o utilizzare solo per scopi dimostrativi.
     */
    public static void main(String[] args) {
        FlatLightLaf.setup();
        java.awt.EventQueue.invokeLater(() -> {
            UI_Inventory inventoryUI = new UI_Inventory();
            inventoryUI.setVisible(true);
            // Esempio di dati (modifica con i tuoi percorsi reali)
            java.util.List<AdvObject> objects = new ArrayList<>();
            for(int i = 1; i <= 26; i++) {
                objects.add(new AdvObject(
                    i,
                    "Oggetto fsefesfsefe" + i,
                    "./resources/img/none.png",
                    "Descrizione dettagliata per l'oggetto " + i + ".\n\n" +
                    "Materiale: Speciale\nPeso: " + (i % 10 + 1) + "kg\nRarità: " + (i % 5 + 1) + "/5"
                ));
            }
            inventoryUI.addObjectsToScroller(objects);
        });
    }
}