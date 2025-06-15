package com.mycompany.poggioadventure.ui.gui.views;

import com.mycompany.poggioadventure.core.utils.ApiClientResult;
import com.mycompany.poggioadventure.core.utils.PoggioClientJersey;
import com.mycompany.poggioadventure.core.utils.Utils;

import com.mycompany.poggioadventure.ui.UI_Abstract;
import com.mycompany.poggioadventure.ui.ErrorHandler;
import com.mycompany.poggioadventure.ui.gui.GUIErrorHandler;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Schermata per la creazione di una nuova partita di PoggioAdventure.
 * 
 * Questa finestra permette all'utente di inserire il proprio nome per iniziare
 * una nuova sessione di gioco. Implementa validazioni sul nome utente e verifica
 * tramite API se l'utente esiste già nel sistema.
 * 
 * La UI è progettata con un focus sulla semplicità e un'esperienza utente fluida,
 * fornendo feedback visivo immediato (effetti hover, messaggi di errore) e
 * gestendo correttamente la navigazione tra le schermate.
 */
public class UI_NewGame extends UI_Abstract {

    /** Campo per l'inserimento del nome giocatore */
    private JTextField nameField;
    
    /** Pulsante per avviare la partita */
    private JButton startButton;
    
    /** Etichetta con il titolo della schermata */
    private JLabel titleLabel;
    
    /** Riferimento alla finestra parent (se presente) */
    private JFrame parentFrame;

    /**
     * Crea una nuova finestra di creazione partita con finestra parent.
     * 
     * La finestra parent verrà disabilitata mentre questa è attiva e
     * riabilitata quando questa viene chiusa (a meno che non si avvii una partita).
     *
     * @param parent Finestra che ha aperto questa (può essere null)
     */
    public UI_NewGame(JFrame parent) {
        super();
        this.parentFrame = parent;
        if (parent != null) {
            parent.setEnabled(false);
        }
    }

    /**
     * Crea una nuova finestra di creazione partita senza finestra parent.
     * 
     * Utile per test o avvio diretto di questa schermata.
     */
    public UI_NewGame() {
        super();
    }

    /**
     * Inizializza tutti i componenti dell'interfaccia e il loro layout.
     * Implementa il template method definito in UI_Abstract.
     */
    @Override
    protected void initComponents() {
        configureFrame();
        createComponents();
        setupLayout();
        setupEventListeners();
        pack();
    }

    /**
     * Configura le proprietà di base della finestra.
     */
    private void configureFrame() {
        setPreferredSize(calculateWindowSize());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UI_Config.BACKGROUND_COLOR);
        getContentPane().setLayout(new BorderLayout());

        // Gestisce la riabilitazione della finestra parent quando questa viene chiusa
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (parentFrame != null && parentFrame.isDisplayable()) {
                    parentFrame.setEnabled(true);
                }
            }
        });
    }

    /**
     * Calcola le dimensioni appropriate per questa finestra.
     * 
     * @return Dimensioni ottimali basate sulla risoluzione dello schermo
     */
    private Dimension calculateWindowSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new Dimension(
            (int)(screenSize.width * UI_Config.WINDOW_WIDTH_RATIO * 0.8),
            (int)(screenSize.height * UI_Config.WINDOW_HEIGHT_RATIO * 0.6)
        );
    }

    /**
     * Crea e configura i componenti dell'interfaccia.
     */
    private void createComponents() {
        // Configurazione titolo
        titleLabel = new JLabel("INSERISCI IL TUO NOME");
        titleLabel.setFont(UI_Config.getBoldFont().deriveFont(
            getPreferredSize().height * UI_Config.TITLE_FONT_RATIO * 0.7f));
        titleLabel.setForeground(UI_Config.TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Configurazione campo nome
        nameField = new JTextField();
        nameField.setFont(UI_Config.getNormalFont().deriveFont(
            getPreferredSize().height * UI_Config.BUTTON_FONT_RATIO));
        nameField.setForeground(UI_Config.TEXT_COLOR);
        nameField.setBackground(UI_Config.BUTTON_BASE_COLOR);
        nameField.setCaretColor(UI_Config.TEXT_COLOR);
        nameField.setBorder(createTextFieldBorder());
        nameField.setHorizontalAlignment(SwingConstants.CENTER);

        // Configurazione pulsante start
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

    /**
     * Organizza i componenti secondo il layout desiderato.
     */
    private void setupLayout() {
        // Pannello principale con GridBagLayout per posizionamento flessibile
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Configurazione dei vincoli per il layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Posizionamento titolo
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);

        // Posizionamento campo nome
        gbc.gridy = 1;
        gbc.ipady = 20;
        nameField.setPreferredSize(new Dimension(
            (int)(getPreferredSize().width * 0.7),
            (int)(getPreferredSize().height * 0.1)
        ));
        nameField.setFont(UI_Config.getBoldFont().deriveFont(22f));
        mainPanel.add(nameField, gbc);

        // Posizionamento pulsante start
        gbc.gridy = 2;
        gbc.ipady = 0;
        gbc.fill = GridBagConstraints.NONE;
        startButton.setPreferredSize(new Dimension(
            (int)(getPreferredSize().width * (UI_Config.BUTTON_WIDTH_RATIO + 0.1)),
            (int)(getPreferredSize().height * (UI_Config.BUTTON_HEIGHT_RATIO + 0.05))
        ));
        startButton.setFont(UI_Config.getBoldFont().deriveFont(22f));
        mainPanel.add(startButton, gbc);

        // Aggiunta del pannello al frame
        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Crea un bordo personalizzato per il campo di testo.
     * 
     * @return Bordo composto con linea esterna e padding interno
     */
    private Border createTextFieldBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        );
    }

    /**
     * Crea un bordo personalizzato per i pulsanti.
     * 
     * @return Bordo composto con linea esterna e padding interno
     */
    private Border createButtonBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(5, 25, 5, 25)
        );
    }

    /**
     * Aggiunge effetto di cambio colore al passaggio del mouse su un pulsante.
     * 
     * @param button Pulsante a cui aggiungere l'effetto hover
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

    /**
     * Configura i listener per gli eventi dell'interfaccia.
     */
    private void setupEventListeners() {
        // Azione sul click del pulsante
        startButton.addActionListener(e -> handleStartGame());
        
        // Azione su invio nel campo nome
        nameField.addActionListener(e -> handleStartGame());
    }

    /**
     * Gestisce la logica di avvio della partita.
     * 
     * Il flusso include:
     * 1. Validazione del nome utente
     * 2. Verifica con il server se l'utente esiste già
     * 3. Avvio della partita o visualizzazione di messaggi di errore
     */
    private void handleStartGame() {
        String playerName = nameField.getText().trim();
        ErrorHandler errorHan = new GUIErrorHandler();

        // Validazione input
        if (playerName.isEmpty()) {
            errorHan.handleRecoverableError("Il nome del giocatore non può essere vuoto!");
            return;
        }

        // Chiusura finestre
        if(parentFrame != null) parentFrame.dispose();
        dispose();

        // Verifica esistenza utente
        PoggioClientJersey gameClient = null;
        try {
            gameClient = new PoggioClientJersey();
            ApiClientResult result = gameClient.checkUserExists(playerName);
            gameClient.close();

            // Gestione risultato
            switch(result){
                case USER_NOT_FOUND -> 
                    // Utente non trovato - procedi con la creazione
                    EventQueue.invokeLater(() -> {
                        try {
                            new UI_Game(playerName).setVisible(true);
                        } catch (Exception e) {
                            new GUIErrorHandler().handleFatalError("Errore critico durante l'avvio della partita: ", e);
                            Utils.exitApplication(Utils.EXIT_CODE_CRITICAL);
                        }
                    });
                case SUCCESS_OK -> {
                    // Utente già esistente
                    errorHan.handleRecoverableError("Errore: L'utente '" + playerName + "' esiste già. Carica la partita o scegli un nome diverso.");
                    new UI_Init().setVisible(true);
                }
                case CONNECTION_ERROR -> 
                    errorHan.handleRecoverableError("Errore di connessione: Impossibile comunicare con il server per verificare l'utente.");
                default -> 
                    errorHan.handleRecoverableError("Errore sconosciuto durante la verifica dell'utente (" + result + ").");
            }
        } catch (Exception e) {
            errorHan.handleRecoverableError("Errore imprevisto durante la comunicazione con il server: " + e.getMessage());
            if (gameClient != null) {
                try { gameClient.close(); } catch (Exception ce) { /* ignora errore chiusura */ }
            }
        }
    }

    /**
     * Restituisce il titolo della finestra.
     * 
     * @return Titolo da visualizzare nella barra del titolo
     */
    @Override
    protected String getWindowTitle() {
        return "Nuova Partita - PoggioAdventure";
    }

    /**
     * Entry point per test della finestra in modalità standalone.
     * 
     * @param args Parametri da linea di comando (non utilizzati)
     */
    public static void main(String[] args) {
        try {
            EventQueue.invokeLater(() -> {
                new UI_NewGame().setVisible(true);
            });
        } catch (Exception ex) {
            new GUIErrorHandler().handleFatalError("Errore durante l'inizializzazione della UI:", ex);
            Utils.exitApplication(Utils.EXIT_CODE_CRITICAL);
        }
    }
}