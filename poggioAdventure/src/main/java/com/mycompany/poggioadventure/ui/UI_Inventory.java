package com.mycompany.poggioadventure.ui;

import com.formdev.flatlaf.FlatLightLaf;
import di.uniba.map.b.adventure.type.AdvObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Classe che rappresenta l'interfaccia utente per l'inventario del gioco PoggioAdventure.
 * Mostra una lista di oggetti, un'immagine e una descrizione dettagliata dell'oggetto selezionato.
 * Estende la classe astratta UI_Abstract per ereditare la struttura di base dell'interfaccia grafica.
 */
public class UI_Inventory extends UI_Abstract {
    // Componenti dell'interfaccia utente
    private JScrollPane objectsScroller;  // Pannello scrollabile per la lista degli oggetti
    private JTextArea descriptionArea;   // Area di testo per la descrizione dell'oggetto
    private JButton escButton;           // Pulsante per chiudere la finestra
    private JPanel imageObjects;         // Pannello per visualizzare l'immagine dell'oggetto

    /**
     * Costruttore della classe. Chiama il costruttore della superclasse UI_Abstract
     * per inizializzare l'interfaccia grafica.
     */
    public UI_Inventory() {
        super();
    }

    /**
     * Implementazione del metodo astratto initComponents() della superclasse UI_Abstract.
     * Inizializza tutti i componenti dell'interfaccia utente, inclusi i pannelli, i pulsanti
     * e le aree di testo.
     */
    @Override
    protected void initComponents() {
        // Configurazione generale della finestra
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Chiude l'applicazione alla chiusura della finestra
        setSize(650, 480);  // Dimensioni fisse della finestra
        getContentPane().setBackground(UI_Config.BACKGROUND_COLOR);  // Colore di sfondo
        setLayout(new BorderLayout(15, 15));  // Layout principale con spaziatura
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));  // Aggiunge un padding interno

        // 1. PANNELLO SINISTRA - LISTA OGGETTI
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(new Color(70, 70, 70));  // Colore di sfondo del pannello
        listPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));  // Bordo del pannello

        // Titolo della lista degli oggetti
        JLabel listTitle = new JLabel(getWindowTitle().toUpperCase());
        listTitle.setFont(UI_Config.getBoldFont().deriveFont(18f));  // Font in grassetto
        listTitle.setForeground(UI_Config.TEXT_COLOR);  // Colore del testo
        listTitle.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));  // Padding interno
        listPanel.add(listTitle, BorderLayout.NORTH);  // Aggiunge il titolo al pannello

        // Pannello scrollabile per la lista degli oggetti
        objectsScroller = new JScrollPane();
        objectsScroller.setBorder(null);  // Rimuove il bordo predefinito
        objectsScroller.getViewport().setBackground(new Color(90, 90, 90));  // Colore di sfondo
        listPanel.add(objectsScroller, BorderLayout.CENTER);  // Aggiunge il pannello scrollabile
        add(listPanel, BorderLayout.WEST);  // Aggiunge il pannello alla finestra

        // 2. PANNELLO DESTRA - IMMAGINE E DESCRIZIONE
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(UI_Config.BACKGROUND_COLOR);  // Colore di sfondo

        GridBagConstraints gbc = new GridBagConstraints();  // Configura i vincoli del layout

        // 2a. PANNELLO IMMAGINE
        imageObjects = new JPanel(new BorderLayout());
        imageObjects.setBackground(new Color(30, 30, 30));  // Colore di sfondo
        imageObjects.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 2),  // Bordo esterno
            BorderFactory.createEmptyBorder(5, 5, 5, 5)  // Padding interno
        ));
        imageObjects.setPreferredSize(new Dimension(300, 250));  // Dimensione preferita
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0.6;
        gbc.fill = GridBagConstraints.BOTH;
        rightPanel.add(imageObjects, gbc);  // Aggiunge il pannello immagine

        // 2b. PANNELLO DESCRIZIONE
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.setBackground(new Color(30, 30, 30));  // Colore di sfondo
        descPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 2),  // Bordo esterno
            BorderFactory.createEmptyBorder(5, 5, 5, 5)  // Padding interno
        ));

        // Area di testo per la descrizione
        descriptionArea = new JTextArea();
        descriptionArea.setBackground(new Color(60, 60, 60));  // Colore di sfondo
        descriptionArea.setForeground(UI_Config.TEXT_COLOR);  // Colore del testo
        descriptionArea.setFont(UI_Config.getNormalFont().deriveFont(14f));  // Font
        descriptionArea.setEditable(false);  // Impedisce la modifica del testo
        descriptionArea.setLineWrap(true);  // Abilita il ritorno a capo automatico
        descriptionArea.setWrapStyleWord(true);  // Mantiene le parole intere

        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(null);  // Rimuove il bordo predefinito
        descPanel.add(descScroll, BorderLayout.CENTER);  // Aggiunge l'area di testo al pannello
        gbc.gridy = 1;
        gbc.weighty = 0.4;
        rightPanel.add(descPanel, gbc);  // Aggiunge il pannello descrizione

        // 2c. PULSANTE ESCI
        escButton = new JButton("ESCI");
        escButton.setFont(UI_Config.getBoldFont().deriveFont(14f));  // Font in grassetto
        escButton.setBackground(new Color(100, 100, 100));  // Colore di sfondo
        escButton.setForeground(UI_Config.TEXT_COLOR);  // Colore del testo
        escButton.setFocusPainted(false);  // Disabilita l'effetto di focus
        escButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));  // Padding interno
        escButton.addActionListener(e -> dispose());  // Chiude la finestra al click
        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 0, 0);  // Spaziatura superiore
        rightPanel.add(escButton, gbc);  // Aggiunge il pulsante

        add(rightPanel, BorderLayout.CENTER);  // Aggiunge il pannello destro alla finestra
    }

    /**
     * Popola la lista degli oggetti nell'area scrollabile.
     * Ogni oggetto è rappresentato da una JLabel cliccabile.
     *
     * @param objects Lista di AdvObject da visualizzare nell'inventario.
     */
    public void addObjectsToScroller(java.util.List<AdvObject> objects) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));  // Layout verticale
        panel.setBackground(new Color(90, 90, 90));  // Colore di sfondo

        // Calcola la larghezza massima del testo
        Font font = UI_Config.getNormalFont().deriveFont(12f);
        FontMetrics fontMetrics = getFontMetrics(font);
        int maxTextWidth = 0;

        // Aggiunge ogni oggetto come JLabel cliccabile
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
                    showObjectDetails(obj);  // Mostra i dettagli dell'oggetto al click
                }
            });
            panel.add(label);
        }

        // Imposta la larghezza del pannello in base alla larghezza massima del testo
        int padding = 30;  // Padding per evitare che il testo sia troppo vicino ai bordi
        int panelWidth = maxTextWidth + padding;
        panel.setPreferredSize(new Dimension(panelWidth, panel.getPreferredSize().height));

        objectsScroller.setViewportView(panel);  // Imposta il pannello come vista scrollabile
        objectsScroller.setPreferredSize(new Dimension(panelWidth, objectsScroller.getPreferredSize().height));
    }

    /**
     * Mostra i dettagli di un oggetto selezionato (immagine e descrizione).
     *
     * @param obj L'oggetto di cui visualizzare i dettagli.
     */
    private void showObjectDetails(AdvObject obj) {
        imageObjects.removeAll();  // Rimuove l'immagine precedente
        imageObjects.setLayout(new BorderLayout());

        // Carica l'immagine dell'oggetto se disponibile
        if (obj.getImagePath() != null && obj.getImagePath().exists()) {
            ImageIcon icon = new ImageIcon(obj.getImagePath().getPath());
            Image image = icon.getImage().getScaledInstance(280, 200, Image.SCALE_SMOOTH);  // Ridimensiona l'immagine
            JLabel imageLabel = new JLabel(new ImageIcon(image));
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            imageObjects.add(imageLabel, BorderLayout.CENTER);
        } else {
            // Mostra un messaggio se l'immagine non è disponibile
            JLabel noImageLabel = new JLabel("Nessuna immagine disponibile");
            noImageLabel.setForeground(UI_Config.TEXT_COLOR);
            noImageLabel.setHorizontalAlignment(JLabel.CENTER);
            imageObjects.add(noImageLabel, BorderLayout.CENTER);
        }

        descriptionArea.setText(obj.getDescription());  // Imposta la descrizione dell'oggetto
        descriptionArea.setCaretPosition(0);  // Posiziona il cursore all'inizio del testo
        revalidate();  // Aggiorna il layout
        repaint();  // Ridisegna la finestra
    }

    /**
     * Metodo main di esempio per testare l'interfaccia.
     * Da rimuovere in produzione o utilizzare solo per scopi dimostrativi.
     */
    public static void main(String[] args) {
        FlatLightLaf.setup();  // Configura il tema FlatLaf light
        java.awt.EventQueue.invokeLater(() -> {
            UI_Inventory inventoryUI = new UI_Inventory();
            inventoryUI.setVisible(true);

            // Esempio di dati (modifica con i tuoi percorsi reali)
            java.util.List<AdvObject> objects = new ArrayList<>();
            for (int i = 1; i <= 26; i++) {
                objects.add(new AdvObject(
                    i,
                    "Oggetto fsefesfsefe" + i,
                    "./resources/img/none.png",
                    "Descrizione dettagliata per l'oggetto " + i + ".\n\n" +
                    "Materiale: Speciale\nPeso: " + (i % 10 + 1) + "kg\nRarità: " + (i % 5 + 1) + "/5"
                ));
            }
            inventoryUI.addObjectsToScroller(objects);  // Popola la lista degli oggetti
        });
    }

    /**
     * Implementazione del metodo astratto getWindowTitle() della superclasse UI_Abstract.
     * Restituisce il titolo della finestra.
     *
     * @return Stringa contenente il titolo della finestra
     */
    @Override
    protected String getWindowTitle() {
        return "PoggioAdventure - Inventario";
    }
}