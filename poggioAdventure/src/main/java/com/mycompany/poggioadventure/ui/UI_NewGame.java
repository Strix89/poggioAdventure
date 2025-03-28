package com.mycompany.poggioadventure.ui;

import di.uniba.map.b.adventure.Utils;
import di.uniba.map.b.adventure.impl.GUIErrorHandler;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Interfaccia per la creazione di una nuova partita in PoggioAdventure.
 * 
 * <p>Responsabilità principali:
 * <ul>
 *   <li>Raccogliere il nome del giocatore</li>
 *   <li>Validare l'input dell'utente</li>
 *   <li>Avviare una nuova partita con i parametri specificati</li>
 *   <li>Gestire la transizione alla schermata di gioco</li>
 * </ul>
 * 
 * <p>Pattern utilizzati:
 * <ul>
 *   <li>Template Method (ereditando da UI_Abstract)</li>
 *   <li>Observer (per gli eventi dei pulsanti)</li>
 * </ul>
 *
 * @author Strix89
 */
public class UI_NewGame extends UI_Abstract {

    // ============== COMPONENTI UI ==============
    private JTextField nameField;    // Campo per l'inserimento nome
    private JButton startButton;     // Pulsante di avvio partita
    private JLabel titleLabel;       // Titolo della finestra
    private JFrame parentFrame;      // Finestra parent (opzionale)

    // ============== COSTRUTTORI ==============
    
    /**
     * Costruttore con finestra parent.
     * @param parent Finestra da chiudere al lancio del gioco
     */
    public UI_NewGame(JFrame parent) {
        super();
        this.parentFrame = parent;
    }
    
    /**
     * Costruttore senza parent (per testing).
     */
    public UI_NewGame() {
        super();
    }

    // ============== INIZIALIZZAZIONE ==============
    
    /**
     * Configura l'interfaccia utente come richiesto da UI_Abstract.
     * Crea e posiziona tutti i componenti grafici.
     */
    @Override
    protected void initComponents() {
        configureFrame();
        createComponents();
        setupLayout();
        setupEventListeners();
        pack();
    }

    // ============== CONFIGURAZIONE FINESTRA ==============
    
    /**
     * Imposta le proprietà base della finestra:
     * - Dimensioni responsive
     * - Comportamento chiusura
     * - Sfondo e layout
     */
    private void configureFrame() {
        setPreferredSize(calculateWindowSize());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UI_Config.BACKGROUND_COLOR);
        getContentPane().setLayout(new BorderLayout());
    }

    /**
     * Calcola dimensioni in base allo schermo:
     * - 80% della larghezza standard
     * - 60% dell'altezza standard
     */
    private Dimension calculateWindowSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new Dimension(
            (int)(screenSize.width * UI_Config.WINDOW_WIDTH_RATIO * 0.8),
            (int)(screenSize.height * UI_Config.WINDOW_HEIGHT_RATIO * 0.6)
        );
    }

    // ============== CREAZIONE COMPONENTI ==============
    
    /**
     * Crea e configura i componenti principali:
     * - Titolo
     * - Campo nome giocatore
     * - Pulsante start
     */
    private void createComponents() {
        // Titolo
        titleLabel = new JLabel("INSERISCI IL TUO NOME");
        titleLabel.setFont(UI_Config.getBoldFont().deriveFont(
            getPreferredSize().height * UI_Config.TITLE_FONT_RATIO * 0.7f));
        titleLabel.setForeground(UI_Config.TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Campo nome
        nameField = new JTextField();
        nameField.setFont(UI_Config.getNormalFont().deriveFont(
            getPreferredSize().height * UI_Config.BUTTON_FONT_RATIO));
        nameField.setForeground(UI_Config.TEXT_COLOR);
        nameField.setBackground(UI_Config.BUTTON_BASE_COLOR);
        nameField.setCaretColor(UI_Config.TEXT_COLOR);
        nameField.setBorder(createTextFieldBorder());
        nameField.setHorizontalAlignment(SwingConstants.CENTER);

        // Pulsante Start
        startButton = new JButton("START");
        startButton.setFont(UI_Config.getBoldFont().deriveFont(
            getPreferredSize().height * UI_Config.BUTTON_FONT_RATIO));
        startButton.setForeground(UI_Config.TEXT_COLOR);
        startButton.setBackground(UI_Config.BUTTON_BASE_COLOR);
        startButton.setBorder(createButtonBorder());
        startButton.setFocusPainted(false);
        startButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addButtonHoverEffect(startButton);
    }

    // ============== LAYOUT ==============
    
    /**
     * Configura il layout con:
     * - Titolo in alto
     * - Campo nome al centro
     * - Pulsante start in basso
     */
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Titolo
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);

        // Campo nome
        gbc.gridy = 1;
        gbc.ipady = 20;
        nameField.setPreferredSize(new Dimension(
            (int)(getPreferredSize().width * 0.7),
            (int)(getPreferredSize().height * 0.1)
        ));
        nameField.setFont(UI_Config.getBoldFont().deriveFont(22f));
        mainPanel.add(nameField, gbc);

        // Pulsante Start
        gbc.gridy = 2;
        gbc.ipady = 0;
        gbc.fill = GridBagConstraints.NONE;
        startButton.setPreferredSize(new Dimension(
            (int)(getPreferredSize().width * (UI_Config.BUTTON_WIDTH_RATIO + 0.1)),
            (int)(getPreferredSize().height * (UI_Config.BUTTON_HEIGHT_RATIO + 0.05))
        ));
        startButton.setFont(UI_Config.getBoldFont().deriveFont(22f));
        mainPanel.add(startButton, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    // ============== STILE COMPONENTI ==============
    
    /**
     * Crea bordo per il campo testo.
     */
    private Border createTextFieldBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        );
    }

    /**
     * Crea bordo per i pulsanti.
     */
    private Border createButtonBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(5, 25, 5, 25)
        );
    }

    /**
     * Aggiunge effetto hover ai pulsanti.
     */
    private void addButtonHoverEffect(JButton button) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(UI_Config.BUTTON_HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(UI_Config.BUTTON_BASE_COLOR);
            }
        });
    }

    // ============== GESTIONE EVENTI ==============
    
    /**
     * Configura gli eventi per:
     * - Avvio partita (pulsante o invio)
     */
    private void setupEventListeners() {
        startButton.addActionListener(e -> handleStartGame());
        nameField.addActionListener(e -> handleStartGame());
    }

    // ============== LOGICA APPLICATIVA ==============
    
    /**
     * Gestisce l'avvio di una nuova partita:
     * - Valida l'input
     * - Chiude le finestre parent
     * - Avvia la schermata di gioco
     */
    private void handleStartGame() {
        String playerName = nameField.getText().trim();
        if (playerName.isEmpty()) {
            new GUIErrorHandler().handleRecoverableError("Inserisci un nome valido!");
            return;
        }
        
        if(parentFrame != null) parentFrame.dispose();
        dispose();
        
        EventQueue.invokeLater(() -> {
            try {
                new UI_Game(playerName).setVisible(true);
            } catch (Exception e) {
                new GUIErrorHandler().handleFatalError("Errore avvio partita: ", e);
            }
        });
    }

    // ============== METODI OVERRIDE ==============
    
    @Override
    protected String getWindowTitle() {
        return "Nuova Partita - PoggioAdventure";
    }

    // ============== MAIN PER TEST ==============
    public static void main(String[] args) {
        try {
            EventQueue.invokeLater(() -> {
                new UI_NewGame().setVisible(true);
            });
        } catch (Exception ex) {
            new GUIErrorHandler().handleFatalError("Errore inizializzazione:", ex);
            Utils.exitApplication(Utils.EXIT_CODE_CRITICAL);
        }
    }
}