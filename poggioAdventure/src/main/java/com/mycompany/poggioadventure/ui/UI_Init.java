package com.mycompany.poggioadventure.ui;

import di.uniba.map.b.adventure.MenuManager;
import di.uniba.map.b.adventure.Utils;
import di.uniba.map.b.adventure.impl.GUIErrorHandler;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.border.Border;

/**
 * Schermata principale dell'applicazione PoggioAdventure.
 * 
 * <p>Responsabilità principali:
 * <ul>
 *   <li>Visualizzazione del menu principale</li>
 *   <li>Gestione della navigazione tra diverse schermate</li>
 *   <li>Caricamento e visualizzazione delle risorse grafiche</li>
 *   <li>Implementazione dell'interfaccia MenuManager</li>
 * </ul>
 * 
 * <p>Pattern utilizzati:
 * <ul>
 *   <li>Template Method (ereditando da UI_Abstract)</li>
 *   <li>Strategy (tramite MenuManager)</li>
 * </ul>
 * 
 * @author Strix89
 */
public class UI_Init extends UI_Abstract implements MenuManager {
    
    // ============== COMPONENTI UI ==============
    private JButton newGameButton;
    private JButton loadGameButton;
    private JButton rankingButton;
    private JButton exitButton;
    private JLabel shieldCenter;

    // ============== COSTRUTTORE ==============
    
    /**
     * Inizializza la schermata principale configurando:
     * - Layout della finestra
     * - Componenti grafici
     * - Gestori eventi
     */
    public UI_Init() {
        super(); // Chiama il costruttore della superclasse UI_Abstract
    }

    // ============== INIZIALIZZAZIONE ==============
    
    /**
     * Implementazione del metodo astratto di UI_Abstract.
     * Configura l'interfaccia utente con:
     * - Frame principale
     * - Immagine logo
     * - Pulsanti di navigazione
     * - Gestori eventi
     */
    @Override
    protected void initComponents() {
        configureMainFrame();
        addShieldComponent();
        addMainContentComponents();
        setupEventListeners();
        pack(); // Adatta la finestra al contenuto
    }

    // ============== CONFIGURAZIONE FINESTRA ==============
    
    /**
     * Configura le proprietà base del JFrame:
     * - Comportamento chiusura
     * - Dimensioni responsive
     * - Sfondo e layout
     */
    private void configureMainFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(calculateWindowSize());
        getContentPane().setBackground(UI_Config.BACKGROUND_COLOR);
        getContentPane().setLayout(new BorderLayout());
    }

    /**
     * Calcola dimensioni responsive basate sullo schermo:
     * - Utilizza i rapporti definiti in UI_Config
     * - Adatta a diverse risoluzioni
     */
    private Dimension calculateWindowSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new Dimension(
            (int)(screenSize.width * UI_Config.WINDOW_WIDTH_RATIO),
            (int)(screenSize.height * UI_Config.WINDOW_HEIGHT_RATIO)
        );
    }

    // ============== COMPONENTI GRAFICI ==============
    
    /**
     * Aggiunge l'immagine del logo con:
     * - Ridimensionamento proporzionale
     * - Margini corretti
     * - Posizionamento in alto
     */
    private void addShieldComponent() {
        ImageIcon shieldIcon = loadShieldImage();
        shieldCenter = new JLabel(scaleImage(shieldIcon, UI_Config.SHIELD_SIZE_RATIO));

        JPanel shieldPanel = new JPanel(new BorderLayout());
        shieldPanel.setOpaque(false);
        shieldPanel.setBorder(BorderFactory.createEmptyBorder(
            (int)(getPreferredSize().height * UI_Config.TOP_MARGIN_RATIO / 100f),
            0, 0, 0
        ));
        shieldPanel.add(shieldCenter, BorderLayout.CENTER);
        add(shieldPanel, BorderLayout.NORTH);
    }

    /**
     * Carica l'immagine dello scudo con gestione errori:
     * - Fallback a messaggio di errore
     * - Chiusura pulita in caso di fallimento
     */
    private ImageIcon loadShieldImage() {
        BufferedImage image = UI_Config.getShieldImage();
        if(image == null) {
            new GUIErrorHandler().handleRecoverableError("Immagine dello scudo non caricata!");
            Utils.exitApplication(Utils.EXIT_CODE_RESOURCE_ERROR);
        }
        return new ImageIcon(image);
    }

    /**
     * Ridimensiona immagini mantenendo le proporzioni:
     * - Utilizza algoritmo SCALE_SMOOTH per qualità
     * - Basato su percentuali dello schermo
     */
    private ImageIcon scaleImage(ImageIcon icon, float ratio) {
        Dimension windowSize = getPreferredSize();
        Image scaled = icon.getImage().getScaledInstance(
            (int)(windowSize.width * ratio),
            (int)(windowSize.height * ratio),
            Image.SCALE_SMOOTH
        );
        return new ImageIcon(scaled);
    }

    // ============== CONTENUTO PRINCIPALE ==============
    
    /**
     * Crea e posiziona i componenti centrali:
     * - Titolo del gioco
     * - Pulsanti di navigazione
     * - Layout a griglia con vincoli
     */
    private void addMainContentComponents() {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = UI_Config.BUTTON_INSETS;

        contentPanel.add(createTitleLabel(), gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        newGameButton = createButton("NUOVA PARTITA");
        loadGameButton = createButton("CARICA PARTITA");
        rankingButton = createButton("CLASSIFICA");
        exitButton = createButton("ESCI");

        contentPanel.add(newGameButton, gbc);
        contentPanel.add(loadGameButton, gbc);
        contentPanel.add(rankingButton, gbc);

        gbc.insets = UI_Config.EXIT_BUTTON_INSETS;
        contentPanel.add(exitButton, gbc);

        add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * Crea il titolo con stile personalizzato:
     * - Font scalato responsivo
     * - Colore dal tema UI
     * - Stile corsivo
     */
    private JLabel createTitleLabel() {
        JLabel label = new JLabel("PoggioAdventure");
        label.setFont(scaleFont(UI_Config.getItalicFont(), UI_Config.TITLE_FONT_RATIO));
        label.setForeground(UI_Config.TEXT_COLOR);
        return label;
    }

    // ============== GESTIONE PULSANTI ==============
    
    /**
     * Crea pulsanti con stile coerente:
     * - Effetti hover
     * - Dimensioni responsive
     * - Stile visivo uniforme
     */
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(scaleFont(UI_Config.getNormalFont(), UI_Config.BUTTON_FONT_RATIO));
        button.setForeground(UI_Config.TEXT_COLOR);
        button.setBackground(UI_Config.BUTTON_BASE_COLOR);
        button.setFocusPainted(false);
        button.setBorder(createButtonBorder());
        button.setPreferredSize(new Dimension(
            (int)(getPreferredSize().width * UI_Config.BUTTON_WIDTH_RATIO),
            (int)(getPreferredSize().height * UI_Config.BUTTON_HEIGHT_RATIO)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(UI_Config.BUTTON_HOVER_COLOR);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(UI_Config.BUTTON_BASE_COLOR);
            }
        });
        
        return button;
    }

    /**
     * Scala i font in base alle dimensioni dello schermo
     * mantenendo leggibilità su diverse risoluzioni
     */
    private Font scaleFont(Font baseFont, float ratio) {
        return baseFont.deriveFont(getPreferredSize().height * ratio);
    }

    // ============== GESTIONE EVENTI ==============
    
    /**
     * Collega le azioni ai pulsanti:
     * - Navigazione tra schermate
     * - Gestione chiusura
     */
    private void setupEventListeners() {
        exitButton.addActionListener(e -> exit());
        rankingButton.addActionListener(e -> showRanking());
        newGameButton.addActionListener(e -> showNewGame());
        loadGameButton.addActionListener(e -> showLoadGame());
    }

    // ============== IMPLEMENTAZIONE MENU MANAGER ==============
    
    @Override
    public void showRanking() {
        JFrame ranking = new UI_Rank();
        ranking.setLocationRelativeTo(this);
        ranking.setVisible(true);
    }

    @Override
    public void showNewGame() {
        JFrame newGame = new UI_NewGame(this);
        newGame.setLocationRelativeTo(null);
        newGame.setVisible(true);
    }

    @Override
    public void showLoadGame() {
        JFrame loadGame = new UI_LoadGame();
        loadGame.setLocationRelativeTo(this);
        dispose();
        loadGame.setVisible(true);
    }
    
    @Override
    public void showMainMenu() {
        setVisible(true);
    }
    
    @Override
    public void exit() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Vuoi davvero uscire?",
            "Conferma Uscita",
            JOptionPane.YES_NO_OPTION);
        
        if(confirm == JOptionPane.YES_OPTION) {
            Utils.exitApplication();
        }
    }

    // ============== UTILITY ==============
    
    /**
     * Crea bordi personalizzati per pulsanti:
     * - Linea esterna con colore dal tema
     * - Padding interno per migliore leggibilità
     * @return tipo di Bordo
     */
    public Border createButtonBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        );
    }

    // ============== METODI OVERRIDE ==============
    
    @Override
    protected String getWindowTitle() {
        return "PoggioAdventure";
    }

    // ============== MAIN PER TEST ==============
    public static void main(String[] args) {
        try {
            EventQueue.invokeLater(() -> {
                UI_Init mainWindow = new UI_Init();
                mainWindow.setVisible(true);
            });
        } catch (Exception ex) {
            new GUIErrorHandler().handleRecoverableError(
                "Errore inizializzazione interfaccia: " + ex.getMessage());
            Utils.exitApplication(Utils.EXIT_CODE_CRITICAL);
        }
    }
}