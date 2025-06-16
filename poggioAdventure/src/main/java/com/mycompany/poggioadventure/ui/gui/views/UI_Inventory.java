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
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
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
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        // Dimensioni fisse più piccole per il pannello immagine
        imageObjects.setPreferredSize(new Dimension(280, 160));
        imageObjects.setMinimumSize(new Dimension(280, 160));
        imageObjects.setMaximumSize(new Dimension(280, 160));
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0.35; // Ridotto ulteriormente per dare più spazio alla descrizione
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
            BorderFactory.createEmptyBorder(10, 10, 10, 10) // Aumentato padding per migliore leggibilità
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
        gbc.weighty = 0.65; // Aumentato per dare più spazio alla descrizione
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
        Font font = UI_Config.getNormalFont().deriveFont(16f);
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
                
                // Ottieni le dimensioni reali del pannello (con margini del bordo)
                Dimension panelSize = imageObjects.getSize();
                if (panelSize.width <= 0 || panelSize.height <= 0) {
                    // Se il pannello non è ancora renderizzato, usa le dimensioni preferite
                    panelSize = new Dimension(280, 160);
                }
                
                // Calcola lo spazio effettivamente disponibile per l'immagine
                // Sottrai i margini del bordo composto (bordo + padding interno)
                int availableWidth = panelSize.width - 8;  // 2px bordo + 2px padding su ogni lato
                int availableHeight = panelSize.height - 8;
                
                // Calcola le dimensioni ottimali che si adattano perfettamente al contenitore
                Dimension fitSize = calculateFitToContainerSize(
                    bufferedImage.getWidth(), 
                    bufferedImage.getHeight(), 
                    availableWidth, 
                    availableHeight
                );
                
                Image scaledImage = bufferedImage.getScaledInstance(
                    fitSize.width, 
                    fitSize.height, 
                    Image.SCALE_SMOOTH
                );
                
                JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                imageLabel.setHorizontalAlignment(JLabel.CENTER);
                imageLabel.setVerticalAlignment(JLabel.CENTER);
                
                // Usa un pannello semplice per centrare l'immagine senza margini extra
                JPanel imageWrapper = new JPanel(new GridBagLayout());
                imageWrapper.setBackground(new Color(30, 30, 30));
                imageWrapper.add(imageLabel);
                
                imageObjects.add(imageWrapper, BorderLayout.CENTER);

            } catch (IOException | IllegalArgumentException ex) {
                JLabel errorLabel = new JLabel("Errore nel caricamento dell'immagine");
                errorLabel.setForeground(UI_Config.TEXT_COLOR);
                errorLabel.setHorizontalAlignment(JLabel.CENTER);
                errorLabel.setVerticalAlignment(JLabel.CENTER);
                imageObjects.add(errorLabel, BorderLayout.CENTER);
            }
        } else {
            JLabel noImageLabel = new JLabel("Nessuna immagine disponibile");
            noImageLabel.setForeground(UI_Config.TEXT_COLOR);
            noImageLabel.setHorizontalAlignment(JLabel.CENTER);
            noImageLabel.setVerticalAlignment(JLabel.CENTER);
            imageObjects.add(noImageLabel, BorderLayout.CENTER);
        }

        // Pulisce la descrizione dai tag colore prima di visualizzarla
        String cleanDescription = removeColorTags(obj.getDescription());
        descriptionArea.setText(cleanDescription);
        descriptionArea.setCaretPosition(0);
        revalidate();
        repaint();
    }
    
    /**
     * Calcola le dimensioni per adattare l'immagine al contenitore disponibile
     * mantenendo le proporzioni originali (simile a object-fit: contain in CSS).
     * 
     * @param originalWidth Larghezza originale dell'immagine
     * @param originalHeight Altezza originale dell'immagine
     * @param containerWidth Larghezza disponibile nel contenitore
     * @param containerHeight Altezza disponibile nel contenitore
     * @return Dimensioni ottimali per l'immagine che si adatta al contenitore
     */
    private Dimension calculateFitToContainerSize(int originalWidth, int originalHeight, 
                                                 int containerWidth, int containerHeight) {
        if (originalWidth <= 0 || originalHeight <= 0 || containerWidth <= 0 || containerHeight <= 0) {
            return new Dimension(Math.max(1, containerWidth), Math.max(1, containerHeight));
        }
        
        // Calcola i rapporti di scala per larghezza e altezza
        double widthScale = (double) containerWidth / originalWidth;
        double heightScale = (double) containerHeight / originalHeight;
        
        // Usa il rapporto più piccolo per garantire che l'immagine si adatti completamente
        // al contenitore mantenendo le proporzioni (comportamento "contain")
        double scale = Math.min(widthScale, heightScale);
        
        // Applica il fattore di scala calcolato
        int scaledWidth = (int) Math.round(originalWidth * scale);
        int scaledHeight = (int) Math.round(originalHeight * scale);
        
        // Assicurati che le dimensioni siano almeno 1x1 pixel
        scaledWidth = Math.max(1, scaledWidth);
        scaledHeight = Math.max(1, scaledHeight);
        
        return new Dimension(scaledWidth, scaledHeight);
    }

    /**
     * Rimuove i tag colore dal testo nel formato [COLORE]testo[/].
     * @param text Testo con potenziali tag colore
     * @return Testo pulito senza tag colore
     */
    private String removeColorTags(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // Rimuove i tag colore nel formato [COLORE]testo[/]
        return text.replaceAll("\\[([A-Za-z_]+)\\](.*?)\\[/\\]", "$2")
                  .replaceAll("\\[([A-Za-z_]+)\\]", "")  // Rimuove tag apertura senza chiusura
                  .replaceAll("\\[/\\]", "");            // Rimuove tag chiusura senza apertura
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