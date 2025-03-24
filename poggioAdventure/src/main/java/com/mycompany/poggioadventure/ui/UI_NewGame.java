package com.mycompany.poggioadventure.ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Classe che rappresenta l'interfaccia utente per iniziare una nuova partita.
 * Permette all'utente di inserire il proprio nome e avviare il gioco.
 * Estende la classe astratta UI_Abstract per ereditare la struttura di base dell'interfaccia grafica.
 */
public class UI_NewGame extends UI_Abstract {

    // Componenti dell'interfaccia utente
    private JTextField nameField;  // Campo di testo per inserire il nome del giocatore
    private JButton startButton;   // Pulsante per avviare la nuova partita
    private JLabel titleLabel;     // Etichetta per il titolo della finestra
    private JFrame parentFrame = null; 

    /**
     * Costruttore della classe.Chiama il costruttore della superclasse UI_Abstract
     * per inizializzare l'interfaccia grafica.
     * @param parent
     */
    public UI_NewGame(JFrame parent) {
        super();
        this.parentFrame = parent;
    }
    
    public UI_NewGame() {
        super();
    }

    /**
     * Implementazione del metodo astratto initComponents() della superclasse UI_Abstract.
     * Inizializza tutti i componenti dell'interfaccia utente, inclusi il campo di testo,
     * il pulsante di avvio e il titolo.
     */
    @Override
    protected void initComponents() {
        applyDialogStyles();
        configureFrame();          // Configura la finestra principale
        createComponents();        // Crea i componenti dell'interfaccia
        setupLayout();             // Configura il layout della finestra
        setupEventListeners();     // Imposta gli eventi dei pulsanti
        pack();                    // Ridimensiona la finestra per adattarsi ai componenti
    }

    /**
     * Configura la finestra principale con le impostazioni di base:
     * - Dimensioni preferite
     * - Comportamento alla chiusura
     * - Colore di sfondo
     * - Layout principale
     */
    private void configureFrame() {
        setPreferredSize(calculateWindowSize());  // Imposta le dimensioni della finestra
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // Chiude solo questa finestra
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
            (int)(screenSize.width * UI_Config.WINDOW_WIDTH_RATIO * 0.8),  // Larghezza ridotta dell'80%
            (int)(screenSize.height * UI_Config.WINDOW_HEIGHT_RATIO * 0.6)  // Altezza ridotta del 60%
        );
    }

    /**
     * Crea i componenti dell'interfaccia utente, inclusi il titolo, il campo di testo
     * e il pulsante di avvio.
     */
    private void createComponents() {
        // Titolo
        titleLabel = new JLabel("INSERISCI IL TUO NOME");
        titleLabel.setFont(UI_Config.getBoldFont().deriveFont(
            getPreferredSize().height * UI_Config.TITLE_FONT_RATIO * 0.7f));  // Scala il font
        titleLabel.setForeground(UI_Config.TEXT_COLOR);  // Colore del testo
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);  // Allinea il testo al centro

        // Campo di testo
        nameField = new JTextField();
        nameField.setFont(UI_Config.getNormalFont().deriveFont(
            getPreferredSize().height * UI_Config.BUTTON_FONT_RATIO));  // Scala il font
        nameField.setForeground(UI_Config.TEXT_COLOR);  // Colore del testo
        nameField.setBackground(UI_Config.BUTTON_BASE_COLOR);  // Colore di sfondo
        nameField.setCaretColor(UI_Config.TEXT_COLOR);  // Colore del cursore
        nameField.setBorder(createTextFieldBorder());  // Bordo personalizzato
        nameField.setHorizontalAlignment(SwingConstants.CENTER);  // Allinea il testo al centro

        // Pulsante Start
        startButton = new JButton("START");
        startButton.setFont(UI_Config.getBoldFont().deriveFont(
            getPreferredSize().height * UI_Config.BUTTON_FONT_RATIO));  // Scala il font
        startButton.setForeground(UI_Config.TEXT_COLOR);  // Colore del testo
        startButton.setBackground(UI_Config.BUTTON_BASE_COLOR);  // Colore di sfondo
        startButton.setBorder(createButtonBorder());  // Bordo personalizzato
        startButton.setFocusPainted(false);  // Disabilita l'effetto di focus
        startButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));  // Cambia il cursore al passaggio del mouse
        addButtonHoverEffect(startButton);  // Aggiunge l'effetto hover
    }

    /**
     * Configura il layout della finestra, posizionando i componenti nei pannelli appropriati.
     */
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new GridBagLayout());  // Pannello principale
        mainPanel.setOpaque(false);  // Rende il pannello trasparente
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));  // Aggiunge un padding interno

        GridBagConstraints gbc = new GridBagConstraints();  // Configura i vincoli del layout
        gbc.insets = new Insets(15, 15, 15, 15);  // Imposta i margini tra i componenti
        gbc.weightx = 1;  // Distribuisce lo spazio orizzontalmente
        gbc.fill = GridBagConstraints.HORIZONTAL;  // Riempi orizzontalmente i componenti

        // Titolo
        gbc.gridy = 0;  // Posizione nella griglia (riga 0)
        gbc.anchor = GridBagConstraints.CENTER;  // Allinea al centro
        mainPanel.add(titleLabel, gbc);  // Aggiunge il titolo al pannello

        // Campo di testo
        gbc.gridy = 1;  // Posizione nella griglia (riga 1)
        gbc.ipady = 20;  // Aumenta l'altezza interna del componente
        nameField.setPreferredSize(new Dimension(
            (int)(getPreferredSize().width * 0.7),  // Larghezza del campo di testo
            (int)(getPreferredSize().height * 0.1)  // Altezza del campo di testo
        ));
        nameField.setFont(UI_Config.getBoldFont().deriveFont(18f));
        mainPanel.add(nameField, gbc);  // Aggiunge il campo di testo al pannello

        // Pulsante Start
        gbc.gridy = 2;  // Posizione nella griglia (riga 2)
        gbc.ipady = 0;  // Ripristina l'altezza interna
        gbc.fill = GridBagConstraints.NONE;  // Non riempire lo spazio
        startButton.setPreferredSize(new Dimension(
            (int)(getPreferredSize().width * (UI_Config.BUTTON_WIDTH_RATIO + 0.1)),  // Larghezza del pulsante
            (int)(getPreferredSize().height * (UI_Config.BUTTON_HEIGHT_RATIO + 0.05))  // Altezza del pulsante
        ));
        startButton.setFont(UI_Config.getBoldFont().deriveFont(22f));
        mainPanel.add(startButton, gbc);  // Aggiunge il pulsante al pannello

        add(mainPanel, BorderLayout.CENTER);  // Aggiunge il pannello principale alla finestra
    }

    /**
     * Crea un bordo personalizzato per il campo di testo.
     *
     * @return Border Bordo configurato
     */
    private Border createTextFieldBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 2),  // Bordo esterno
            BorderFactory.createEmptyBorder(10, 15, 10, 15)  // Padding interno
        );
    }

    /**
     * Crea un bordo personalizzato per i pulsanti.
     *
     * @return Border Bordo configurato
     */
    private Border createButtonBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 2),  // Bordo esterno
            BorderFactory.createEmptyBorder(5, 25, 5, 25)  // Padding interno
        );
    }

    /**
     * Aggiunge un effetto hover ai pulsanti, cambiando il colore di sfondo
     * quando il mouse passa sopra di essi.
     *
     * @param button Pulsante a cui aggiungere l'effetto
     */
    private void addButtonHoverEffect(JButton button) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(UI_Config.BUTTON_HOVER_COLOR);  // Cambia colore al passaggio del mouse
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(UI_Config.BUTTON_BASE_COLOR);  // Ripristina il colore originale
            }
        });
    }

    /**
     * Configura gli eventi dei pulsanti:
     * - "START" avvia il gioco se il nome è valido.
     */
    private void setupEventListeners() {
        startButton.addActionListener(e -> handleStartGame());  // Gestisce l'avvio del gioco
        nameField.addActionListener(e -> handleStartGame());
    }

    /**
     * Gestisce l'avvio del gioco, verificando che il nome del giocatore sia valido.
     */
    private void handleStartGame() {
        String playerName = nameField.getText().trim();  // Ottiene il nome inserito dall'utente
        if (playerName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Inserisci un nome valido!",
                "Errore",
                JOptionPane.WARNING_MESSAGE);  // Mostra un messaggio di errore
            return;
        }
        if(parentFrame != null) parentFrame.dispose();
        dispose(); // Chiude UI_NewGame
        EventQueue.invokeLater(() -> {
            UI_Game game = new UI_Game(playerName);
            game.setVisible(true);
        });
    }

    /**
     * Metodo main di esempio per testare l'interfaccia.
     * Da rimuovere in produzione o utilizzare solo per scopi dimostrativi.
     */
    public static void main(String[] args) {
        try {
            EventQueue.invokeLater(() -> {
                UI_NewGame mainWindow = new UI_NewGame();  // Crea la finestra principale
                mainWindow.setVisible(true);  // Rende la finestra visibile
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                "Errore critico nell'inizializzazione dell'interfaccia: " + ex.getMessage(),
                "Errore", JOptionPane.ERROR_MESSAGE);  // Mostra un messaggio di errore
            System.exit(1);  // Termina l'applicazione
        }
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

    /**
     * Implementazione del metodo astratto getWindowTitle() della superclasse UI_Abstract.
     * Restituisce il titolo della finestra.
     *
     * @return Stringa contenente il titolo della finestra
     */
    @Override
    protected String getWindowTitle() {
        return "Nuova Partita - PoggioAdventure";
    }
}