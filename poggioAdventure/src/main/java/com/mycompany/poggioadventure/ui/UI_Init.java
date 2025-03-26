package com.mycompany.poggioadventure.ui;

import di.uniba.map.b.adventure.MenuManager;
import di.uniba.map.b.adventure.Utils;
import di.uniba.map.b.adventure.impl.GUIErrorHandler;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.border.Border;

/**
 * Classe principale per l'interfaccia utente del gioco PoggioAdventure.
 * Gestisce la schermata iniziale del gioco, con pulsanti per iniziare una nuova partita,
 * caricare una partita esistente, visualizzare la classifica e uscire dal gioco.
 * Estende la classe astratta UI_Abstract per ereditare la struttura di base dell'interfaccia grafica.
 */
public class UI_Init extends UI_Abstract implements MenuManager{
    
    // Componenti dell'interfaccia utente
    private JButton newGameButton;  // Pulsante per iniziare una nuova partita
    private JButton loadGameButton; // Pulsante per caricare una partita esistente
    private JButton rankingButton;  // Pulsante per visualizzare la classifica
    private JButton exitButton;     // Pulsante per uscire dal gioco
    private JLabel shieldCenter;    // Etichetta per visualizzare l'immagine dello scudo

    /**
     * Costruttore della classe. Chiama il costruttore della superclasse UI_Abstract
     * per inizializzare l'interfaccia grafica.
     */
    public UI_Init() {
        super();
    }

    /**
     * Implementazione del metodo astratto initComponents() della superclasse UI_Abstract.
     * Inizializza tutti i componenti dell'interfaccia utente, inclusi i pulsanti,
     * l'immagine dello scudo e il titolo del gioco.
     */
    @Override
    protected void initComponents() {
        configureMainFrame();          // Configura la finestra principale
        addShieldComponent();          // Aggiunge l'immagine dello scudo
        addMainContentComponents();    // Aggiunge i pulsanti e il titolo
        setupEventListeners();         // Configura gli eventi dei pulsanti
        pack();                        // Ridimensiona la finestra per adattarsi ai componenti
    }

    /**
     * Configura la finestra principale con le impostazioni di base:
     * - Comportamento alla chiusura
     * - Dimensioni preferite
     * - Colore di sfondo
     * - Layout principale
     */
    private void configureMainFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Chiude l'applicazione alla chiusura della finestra
        setPreferredSize(calculateWindowSize());         // Imposta le dimensioni della finestra
        getContentPane().setBackground(UI_Config.BACKGROUND_COLOR);  // Colore di sfondo
        getContentPane().setLayout(new BorderLayout());  // Layout principale
    }

    /**
     * Calcola le dimensioni della finestra in base alle dimensioni dello schermo
     * e ai rapporti definiti in UI_Config.
     *
     * @return Dimension Oggetto Dimension che rappresenta le dimensioni della finestra
     */
    private Dimension calculateWindowSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();  // Ottiene le dimensioni dello schermo
        return new Dimension(
            (int)(screenSize.width * UI_Config.WINDOW_WIDTH_RATIO),  // Larghezza in base al rapporto
            (int)(screenSize.height * UI_Config.WINDOW_HEIGHT_RATIO)  // Altezza in base al rapporto
        );
    }

    /**
     * Aggiunge l'immagine dello scudo nella parte superiore della finestra.
     * L'immagine viene scalata in base alle dimensioni della finestra.
     */
    private void addShieldComponent() {
        ImageIcon shieldIcon = loadShieldImage();  // Carica l'immagine dello scudo
        shieldCenter = new JLabel(scaleImage(shieldIcon, UI_Config.SHIELD_SIZE_RATIO));  // Scala l'immagine

        JPanel shieldPanel = new JPanel(new BorderLayout());  // Crea un pannello per l'immagine
        shieldPanel.setOpaque(false);  // Rende il pannello trasparente
        shieldPanel.setBorder(BorderFactory.createEmptyBorder(
            (int)(getPreferredSize().height * UI_Config.TOP_MARGIN_RATIO / 100f),  // Margine superiore
            0, 0, 0
        ));
        shieldPanel.add(shieldCenter, BorderLayout.CENTER);  // Aggiunge l'immagine al pannello
        add(shieldPanel, BorderLayout.NORTH);  // Aggiunge il pannello alla finestra
    }

    /**
     * Carica l'immagine dello scudo dalla configurazione.
     * Se l'immagine non viene caricata, mostra un messaggio di errore e termina l'applicazione.
     *
     * @return ImageIcon Icona dell'immagine dello scudo
     */
    private ImageIcon loadShieldImage() {
        BufferedImage image = UI_Config.getShieldImage();  // Ottiene l'immagine dalla configurazione
        if(image == null) {
            new GUIErrorHandler().handleRecoverableError("Errore critico: immagine dello scudo non caricata!");  // Messaggio di errore
            Utils.exitApplication(Utils.EXIT_CODE_RESOURCE_ERROR);  // Termina l'applicazione
        }
        return new ImageIcon(image);  // Restituisce l'icona dell'immagine
    }

    /**
     * Scala un'immagine in base alle dimensioni della finestra e a un rapporto specificato.
     *
     * @param icon Icona da scalare
     * @param ratio Rapporto di ridimensionamento
     * @return ImageIcon Icona scalata
     */
    private ImageIcon scaleImage(ImageIcon icon, float ratio) {
        Dimension windowSize = getPreferredSize();  // Ottiene le dimensioni della finestra
        Image scaled = icon.getImage().getScaledInstance(
            (int)(windowSize.width * ratio),  // Larghezza scalata
            (int)(windowSize.height * ratio), // Altezza scalata
            Image.SCALE_SMOOTH  // Algoritmo di ridimensionamento ad alta qualità
        );
        return new ImageIcon(scaled);  // Restituisce l'icona scalata
    }

    /**
     * Aggiunge i componenti principali della finestra, inclusi il titolo e i pulsanti.
     */
    private void addMainContentComponents() {
        JPanel contentPanel = new JPanel(new GridBagLayout());  // Crea un pannello per il contenuto
        contentPanel.setOpaque(false);  // Rende il pannello trasparente

        GridBagConstraints gbc = new GridBagConstraints();  // Configura i vincoli del layout
        gbc.gridwidth = GridBagConstraints.REMAINDER;  // Ogni componente occupa una riga intera
        gbc.anchor = GridBagConstraints.CENTER;  // Allinea i componenti al centro
        gbc.insets = UI_Config.BUTTON_INSETS;  // Imposta i margini tra i componenti

        contentPanel.add(createTitleLabel(), gbc);  // Aggiunge il titolo al pannello

        gbc.fill = GridBagConstraints.HORIZONTAL;  // Riempi orizzontalmente i componenti
        newGameButton = createButton("NUOVA PARTITA");  // Crea il pulsante "Nuova Partita"
        loadGameButton = createButton("CARICA PARTITA");  // Crea il pulsante "Carica Partita"
        rankingButton = createButton("CLASSIFICA");  // Crea il pulsante "Classifica"
        exitButton = createButton("ESCI");  // Crea il pulsante "Esci"

        contentPanel.add(newGameButton, gbc);  // Aggiunge i pulsanti al pannello
        contentPanel.add(loadGameButton, gbc);
        contentPanel.add(rankingButton, gbc);

        gbc.insets = UI_Config.EXIT_BUTTON_INSETS;  // Imposta margini specifici per il pulsante "Esci"
        contentPanel.add(exitButton, gbc);

        add(contentPanel, BorderLayout.CENTER);  // Aggiunge il pannello alla finestra
    }

    /**
     * Crea e configura l'etichetta del titolo del gioco.
     *
     * @return JLabel Etichetta del titolo
     */
    private JLabel createTitleLabel() {
        JLabel label = new JLabel("PoggioAdventure");  // Crea l'etichetta con il titolo
        label.setFont(scaleFont(UI_Config.getItalicFont(), UI_Config.TITLE_FONT_RATIO));  // Scala il font
        label.setForeground(UI_Config.TEXT_COLOR);  // Imposta il colore del testo
        return label;
    }

    /**
     * Crea e configura un pulsante con stile personalizzato.
     *
     * @param text Testo del pulsante
     * @return JButton Pulsante configurato
     */
    private JButton createButton(String text) {
        JButton button = new JButton(text);  // Crea il pulsante
        button.setFont(scaleFont(UI_Config.getNormalFont(), UI_Config.BUTTON_FONT_RATIO));  // Scala il font
        button.setForeground(UI_Config.TEXT_COLOR);  // Imposta il colore del testo
        button.setBackground(UI_Config.BUTTON_BASE_COLOR);  // Imposta il colore di sfondo
        button.setFocusPainted(false);  // Disabilita l'effetto di focus
        button.setBorder(createButtonBorder());  // Imposta il bordo personalizzato
        button.setPreferredSize(new Dimension(
            (int)(getPreferredSize().width * UI_Config.BUTTON_WIDTH_RATIO),  // Larghezza del pulsante
            (int)(getPreferredSize().height * UI_Config.BUTTON_HEIGHT_RATIO)  // Altezza del pulsante
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));  // Cambia il cursore al passaggio del mouse
        
        // Effetto hover: cambia colore quando il mouse passa sopra il pulsante
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
     * Scala un font in base alle dimensioni della finestra e a un rapporto specificato.
     *
     * @param baseFont Font di base
     * @param ratio Rapporto di ridimensionamento
     * @return Font Font scalato
     */
    private Font scaleFont(Font baseFont, float ratio) {
        return baseFont.deriveFont(getPreferredSize().height * ratio);  // Scala il font
    }

    /**
     * Configura gli eventi dei pulsanti:
     * - "Esci" chiude l'applicazione.
     * - "Classifica" apre la finestra della classifica.
     * - "Nuova Partita" apre la finestra per una nuova partita.
     * - "Carica Partita" apre la finestra per caricare una partita esistente.
     */
    private void setupEventListeners() {
        exitButton.addActionListener(e -> System.exit(0));  // Chiude l'applicazione
        rankingButton.addActionListener(e -> showRanking());  // Mostra la classifica
        newGameButton.addActionListener(e -> showNewGame());  // Avvia una nuova partita
        loadGameButton.addActionListener(e -> showLoadGame());  // Carica una partita esistente
    }

    /**
     * Mostra la finestra della classifica.
     */
    @Override
    public void showRanking() {
        JFrame ranking = new UI_Rank();  // Crea la finestra della classifica
        ranking.setLocationRelativeTo(this);  // Centra la finestra rispetto a questa
        ranking.setVisible(true);  // Rende la finestra visibile
    }

    /**
     * Mostra la finestra per iniziare una nuova partita.
     */
    @Override
    public void showNewGame() {
        JFrame newGame = new UI_NewGame(this);  // Crea la finestra per una nuova partita
        newGame.setLocationRelativeTo(null);  // Centra la finestra rispetto a questa
        newGame.setVisible(true);  // Rende la finestra visibile
    }

    /**
     * Mostra la finestra per caricare una partita esistente.
     */
    @Override
    public void showLoadGame() {
        JFrame loadGame = new UI_LoadGame();  // Crea la finestra per caricare una partita
        loadGame.setLocationRelativeTo(this);  // Centra la finestra rispetto a questa
        dispose();
        loadGame.setVisible(true);  // Rende la finestra visibile
    }
    
    @Override
    public void showMainMenu() {
        // Già gestito dalla GUI, non necessario per CLI
        setVisible(true);
    }
    
    /**
     * Crea un bordo personalizzato per i pulsanti.
     *
     * @return Border Bordo configurato
     */
    public Border createButtonBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 2),  // Bordo esterno
            BorderFactory.createEmptyBorder(5, 15, 5, 15)  // Padding interno
        );
    }

    /**
     * Metodo main per avviare l'applicazione.
     *
     * @param args Argomenti della riga di comando (non utilizzati)
     */
    public static void main(String[] args) {
        try {
            EventQueue.invokeLater(() -> {
                UI_Init mainWindow = new UI_Init();  // Crea la finestra principale
                mainWindow.setVisible(true);  // Rende la finestra visibile
            });
        } catch (Exception ex) {
            new GUIErrorHandler().handleRecoverableError(
                "Errore critico nell'inizializzazione dell'interfaccia: " + ex.getMessage());  // Mostra un messaggio di errore
            Utils.exitApplication(Utils.EXIT_CODE_CRITICAL);  // Termina l'applicazione
        }
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

    /**
     * Implementazione del metodo astratto getWindowTitle() della superclasse UI_Abstract.
     * Restituisce il titolo della finestra.
     *
     * @return Stringa contenente il titolo della finestra
     */
    @Override
    protected String getWindowTitle() {
        return "PoggioAdventure";
    }
}