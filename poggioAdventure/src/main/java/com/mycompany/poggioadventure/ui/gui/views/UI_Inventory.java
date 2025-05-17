package com.mycompany.poggioadventure.ui.gui.views;

import com.formdev.flatlaf.FlatLightLaf;
import com.mycompany.poggioadventure.ui.UI_Abstract;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.persistence.ResourceLoader;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Interfaccia grafica per la gestione dell'inventario di gioco.
 * 
 * <p>Responsabilità principali:
 * <ul>
 *   <li>Visualizzazione lista oggetti posseduti</li>
 *   <li>Mostra dettagli oggetti selezionati</li>
 *   <li>Gestione interazione con gli oggetti</li>
 * </ul>
 * 
 * <p>Caratteristiche:
 * <ul>
 *   <li>Layout diviso in due sezioni (lista/dettaglio)</li>
 *   <li>Scroll automatico per liste lunghe</li>
 *   <li>Visualizzazione immagini e descrizioni</li>
 *   <li>Design responsive con dimensioni calcolate</li>
 * </ul>
 *
 * @author Strix89
 */
public class UI_Inventory extends UI_Abstract {
    
    // ============== COMPONENTI UI ==============
    private JScrollPane objectsScroller;  // Area scrollabile per la lista oggetti
    private JTextArea descriptionArea;    // Area testo per la descrizione
    private JButton escButton;            // Pulsante chiusura finestra
    private JPanel imageObjects;          // Pannello visualizzazione immagine

    // ============== COSTRUTTORE ==============
    
    /**
     * Inizializza la finestra dell'inventario configurando:
     * - Proprietà base della finestra
     * - Layout principale
     * - Componenti grafici
     */
    public UI_Inventory() {
        super();
    }

    // ============== INIZIALIZZAZIONE ==============
    
    /**
     * Configura l'interfaccia utente come richiesto da UI_Abstract.
     * Crea e posiziona tutti i componenti grafici.
     */
    @Override
    protected void initComponents() {
        configureMainWindow();
        createLeftPanel();
        createRightPanel();
    }

    // ============== CONFIGURAZIONE FINESTRA ==============
    
    /**
     * Imposta le proprietà base della finestra:
     * - Dimensioni fisse
     * - Comportamento chiusura
     * - Sfondo e layout
     */
    private void configureMainWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 480);
        getContentPane().setBackground(UI_Config.BACKGROUND_COLOR);
        setLayout(new BorderLayout(15, 15));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    }

    // ============== PANNELLO SINISTRO (LISTA) ==============
    
    /**
     * Crea il pannello sinistro contenente:
     * - Titolo sezione
     * - Area scrollabile per la lista oggetti
     */
    private void createLeftPanel() {
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(new Color(70, 70, 70));
        listPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

        // Titolo sezione
        JLabel listTitle = new JLabel(getWindowTitle().toUpperCase());
        listTitle.setFont(UI_Config.getBoldFont().deriveFont(18f));
        listTitle.setForeground(UI_Config.TEXT_COLOR);
        listTitle.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        listPanel.add(listTitle, BorderLayout.NORTH);

        // Area scrollabile
        objectsScroller = new JScrollPane();
        objectsScroller.setBorder(null);
        objectsScroller.getViewport().setBackground(new Color(90, 90, 90));
        listPanel.add(objectsScroller, BorderLayout.CENTER);
        
        add(listPanel, BorderLayout.WEST);
    }

    // ============== PANNELLO DESTRO (DETTAGLI) ==============
    
    /**
     * Crea il pannello destro contenente:
     * - Visualizzazione immagine oggetto
     * - Descrizione testuale
     * - Pulsante chiusura
     */
    private void createRightPanel() {
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(UI_Config.BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();

        createImagePanel(gbc, rightPanel);
        createDescriptionPanel(gbc, rightPanel);
        createExitButton(gbc, rightPanel);

        add(rightPanel, BorderLayout.CENTER);
    }

    /**
     * Crea il pannello per la visualizzazione dell'immagine
     */
    private void createImagePanel(GridBagConstraints gbc, JPanel parent) {
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
        parent.add(imageObjects, gbc);
    }

    /**
     * Crea il pannello per la descrizione testuale
     */
    private void createDescriptionPanel(GridBagConstraints gbc, JPanel parent) {
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.setBackground(new Color(30, 30, 30));
        descPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        descriptionArea = new JTextArea();
        descriptionArea.setBackground(new Color(60, 60, 60));
        descriptionArea.setForeground(UI_Config.TEXT_COLOR);
        descriptionArea.setFont(UI_Config.getNormalFont().deriveFont(14f));
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(null);
        descPanel.add(descScroll, BorderLayout.CENTER);
        
        gbc.gridy = 1;
        gbc.weighty = 0.4;
        parent.add(descPanel, gbc);
    }

    /**
     * Crea e configura il pulsante di chiusura
     */
    private void createExitButton(GridBagConstraints gbc, JPanel parent) {
        escButton = new JButton("ESCI");
        escButton.setFont(UI_Config.getBoldFont().deriveFont(14f));
        escButton.setBackground(new Color(100, 100, 100));
        escButton.setForeground(UI_Config.TEXT_COLOR);
        escButton.setFocusPainted(false);
        escButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        escButton.addActionListener(e -> dispose());
        
        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 0, 0);
        parent.add(escButton, gbc);
    }

    // ============== GESTIONE OGGETTI ==============
    
    /**
     * Popola la lista degli oggetti nell'inventario.
     * @param objects Lista degli oggetti da visualizzare
     */
    public void addObjectsToScroller(java.util.List<AdvObject> objects) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(90, 90, 90));

        // Calcola larghezza massima testo
        Font font = UI_Config.getNormalFont().deriveFont(12f);
        FontMetrics fontMetrics = getFontMetrics(font);
        int maxTextWidth = 0;

        // Crea elementi cliccabili per ogni oggetto
        for (AdvObject obj : objects) {
            String text = " • " + obj.getName();
            maxTextWidth = Math.max(maxTextWidth, fontMetrics.stringWidth(text));

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

        // Imposta dimensioni ottimali
        int panelWidth = maxTextWidth + 30;
        panel.setPreferredSize(new Dimension(panelWidth, panel.getPreferredSize().height));
        objectsScroller.setViewportView(panel);
        objectsScroller.setPreferredSize(new Dimension(panelWidth, objectsScroller.getPreferredSize().height));
    }

    private void showObjectDetails(AdvObject obj) {
        imageObjects.removeAll();
        imageObjects.setLayout(new BorderLayout());

        if (obj.getImagePath() != null) {
            try {
                BufferedImage bufferedImage = ResourceLoader.loadImage(obj.getImagePath());
                Image scaledImage = bufferedImage.getScaledInstance(280, 200, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                imageLabel.setHorizontalAlignment(JLabel.CENTER);
                imageObjects.add(imageLabel, BorderLayout.CENTER);

            } catch (IOException | IllegalArgumentException ex) {
                JLabel errorLabel = new JLabel("Errore nel caricamento dell'immagine");
                errorLabel.setForeground(UI_Config.TEXT_COLOR);
                errorLabel.setHorizontalAlignment(JLabel.CENTER);
                imageObjects.add(errorLabel, BorderLayout.CENTER);
            }
        } else {
            JLabel noImageLabel = new JLabel("Nessuna immagine disponibile");
            noImageLabel.setForeground(UI_Config.TEXT_COLOR);
            noImageLabel.setHorizontalAlignment(JLabel.CENTER);
            imageObjects.add(noImageLabel, BorderLayout.CENTER);
        }

        descriptionArea.setText(obj.getDescription());
        descriptionArea.setCaretPosition(0);
        revalidate();
        repaint();
    }

    // ============== METODI OVERRIDE ==============
    
    @Override
    protected String getWindowTitle() {
        return "PoggioAdventure - Inventario";
    }

    // ============== MAIN PER TEST ==============
    public static void main(String[] args) {
        FlatLightLaf.setup();
        java.awt.EventQueue.invokeLater(() -> {
            UI_Inventory inventoryUI = new UI_Inventory();
            inventoryUI.setVisible(true);

            // Dati di esempio
            java.util.List<AdvObject> objects = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                objects.add(new AdvObject(
                    i,
                    "Oggetto " + i,
                    ResourceLoader.IMG_PATH.resolve("non2e.png").toString(),
                    "Descrizione dettagliata per l'oggetto " + i
                ));
            }
            inventoryUI.addObjectsToScroller(objects);
        });
    }
}