package com.mycompany.poggioadventure.ui;
import di.uniba.map.b.adventure.ColorText;
import di.uniba.map.b.adventure.Engine;
import di.uniba.map.b.adventure.EngineFactory;
import di.uniba.map.b.adventure.ErrorHandler;
import di.uniba.map.b.adventure.InputHandler;
import di.uniba.map.b.adventure.SaveGame;
import di.uniba.map.b.adventure.impl.GUIErrorHandler;
import di.uniba.map.b.adventure.impl.GUIOutputHandler;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import di.uniba.map.b.adventure.OutputHandler;
import di.uniba.map.b.adventure.impl.GUIInputHandler;

/**
 * Classe che rappresenta la finestra principale del gioco. Gestisce l'interfaccia utente,
 * inclusa la visualizzazione del log di gioco, l'input dei comandi, l'immagine della stanza corrente,
 * i timer di gioco e le funzioni di salvataggio/uscita. Estende la classe astratta UI_Abstract
 * per ereditare la struttura di base dell'interfaccia grafica.
 */
public class UI_Game extends UI_Abstract {
    // Dimensioni fisse della finestra (larghezza x altezza)
    private static final int WINDOW_WIDTH = 1100;  // Larghezza della finestra in pixel
    private static final int WINDOW_HEIGHT = 550;  // Altezza della finestra in pixel

    // Componenti principali dell'interfaccia utente
    private JTextPane gameOutputArea;      // Area di testo per visualizzare i messaggi di gioco
    private JScrollPane outputScrollPane;  // Scroll pane per rendere scrollabile l'area di testo
    private JTextField commandInput;       // Campo di testo per l'inserimento dei comandi
    private JButton sendButton;           // Pulsante per inviare i comandi
    private JPanel imagePanel;            // Pannello per visualizzare l'immagine della stanza corrente
    private JLabel timeLabel;             // Etichetta per visualizzare il tempo di gioco trascorso
    private JLabel countdownLabel;        // Etichetta per visualizzare il countdown del livello
    private JPanel outputImagePane;       // Pannello contenitore per l'immagine della stanza
    private JLabel playerNameLabel;

    // Timer e gestione del tempo
    private Timer gameTimer;              // Timer per aggiornare il tempo totale di gioco
    private Timer countdownTimer;         // Timer per gestire il countdown del livello
    private long startTime;               // Timestamp di inizio del gioco (in millisecondi)
    private int remainingSeconds;         // Secondi rimanenti per il countdown del livello
    private Engine gameEngine;

    /**
     * Costruttore della classe.Chiama il costruttore della superclasse UI_Abstract
     * per inizializzare l'interfaccia grafica.
     * @param playerName
     */
    public UI_Game(String playerName) {
        super();
        initEngineAfterUI(playerName);
    }
    
    public UI_Game() {
        super();
        initEngineAfterUI("NONE");
    }

    /**
     * Implementazione del metodo astratto initComponents() della superclasse UI_Abstract.
     * Inizializza tutti i componenti dell'interfaccia utente, inclusi i pannelli, i pulsanti,
     * le etichette e i timer.
     */
    @Override
    protected void initComponents() {
        setupTimers();  // Configura i timer di gioco
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);  // Disabilita la chiusura automatica

        // Imposta le dimensioni fisse della finestra e il colore di sfondo
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        getContentPane().setBackground(UI_Config.BACKGROUND_COLOR);

        // Configura il layout principale della finestra con spaziatura e bordi
        setLayout(new BorderLayout(15, 15));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Inizializzazione del pannello sinistro (log di gioco e input comandi)
        JPanel leftPanel = createLeftPanel();

        // Inizializzazione del pannello destro (immagine della stanza e informazioni)
        JPanel rightPanel = createRightPanel();

        // Aggiunge i pannelli sinistro e destro alla finestra principale
        add(leftPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        pack();  // Ridimensiona la finestra per adattarsi ai componenti
    }
    
    private void initEngineAfterUI(String playerName) {
        OutputHandler guiOutput = new GUIOutputHandler(gameOutputArea);
        ErrorHandler errHandler = new GUIErrorHandler();
        InputHandler inHandler = new GUIInputHandler(commandInput);
        try {
            this.gameEngine = EngineFactory.createNewGame(playerName, guiOutput, inHandler, errHandler);
        } catch (Exception ex) {
            errHandler.handleFatalError(UI_Game.class.getName() + ": Errore inizializzazione gioco, ",ex);
        }
        playerNameLabel.setText(playerName);
        commandInput.addActionListener(e -> processCommand());
        sendButton.addActionListener(e -> processCommand());
    }

    /**
     * Crea e configura il pannello sinistro, che contiene:
     * - Un'area di testo scrollabile per i messaggi di gioco
     * - Un campo di input per i comandi
     * - Un pulsante per inviare i comandi
     *
     * @return JPanel configurato per il lato sinistro della finestra
     */
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);  // Rende il pannello trasparente

        // Configurazione dell'area di testo per i messaggi di gioco
        gameOutputArea = new JTextPane();
        gameOutputArea.setEditable(false);  // Impedisce la modifica del testo
        gameOutputArea.setFont(UI_Config.getNormalFont().deriveFont(18f));
        gameOutputArea.setForeground(Color.WHITE);
        gameOutputArea.setBackground(new Color(60, 60, 60));  // Colore di sfondo scuro
        outputScrollPane = new JScrollPane(gameOutputArea);
        outputScrollPane.setBorder(createSectionBorder("Log Gioco"));  // Aggiunge un bordo con titolo

        // Configurazione del pannello di input comandi
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setOpaque(false);
        commandInput = new JTextField();
        sendButton = createButton("INVIA", UI_Config.BUTTON_BASE_COLOR, 14f);

        inputPanel.add(commandInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        panel.add(outputScrollPane, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
    * Crea e configura il pannello destro, che contiene:
    * - Un'immagine della stanza corrente
    * - Informazioni temporali (timer di gioco e countdown)
    * - Pulsanti di controllo (salva/esci)
    *
    * @return JPanel configurato per il lato destro della finestra
    */
   private JPanel createRightPanel() {
       JPanel panel = new JPanel(new BorderLayout(10, 10));
       panel.setOpaque(false);

       // Pannello per l'immagine della stanza
       imagePanel = new JPanel(new BorderLayout());
       imagePanel.setBackground(new Color(60, 60, 60));
       outputImagePane = new JPanel(new BorderLayout());
       outputImagePane.setBackground(UI_Config.BACKGROUND_COLOR);
       outputImagePane.add(imagePanel);
       outputImagePane.setBorder(createSectionBorder("Stanza Corrente"));
       outputImagePane.setPreferredSize(new Dimension(
           (int)(WINDOW_WIDTH * 0.35),
           (int)(WINDOW_HEIGHT * 0.45)
       ));

       // Pannello per le informazioni e i pulsanti
       JPanel infoPanel = new JPanel(new BorderLayout(10, 20));
       infoPanel.setOpaque(false);

       // Pannello per le etichette (nome, tempo, countdown)
       JPanel timePanel = new JPanel();
       timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS));
       timePanel.setOpaque(false);

       // Inizializza le etichette
       timeLabel = new JLabel("Tempo: 00:00:00");
       countdownLabel = new JLabel("Countdown: --:-- ##TODO");
       playerNameLabel = new JLabel("NONE"); 
       playerNameLabel.setFont(UI_Config.getBoldFont().deriveFont(20f));
       timeLabel.setFont(UI_Config.getNormalFont().deriveFont(16f));
       countdownLabel.setFont(UI_Config.getNormalFont().deriveFont(16f));
       playerNameLabel.setForeground(ColorText.WHITE.getSwingColor());
       timeLabel.setForeground(ColorText.WHITE.getSwingColor());
       countdownLabel.setForeground(ColorText.RED.getSwingColor());

       // Player name centrato
       JPanel playerNameContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
       playerNameContainer.setOpaque(false);
       playerNameContainer.add(playerNameLabel);

       // Pannelli per allineare tempo e countdown a sinistra
       JPanel timeLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
       timeLabelPanel.setOpaque(false);
       timeLabelPanel.add(timeLabel);

       JPanel countdownLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
       countdownLabelPanel.setOpaque(false);
       countdownLabelPanel.add(countdownLabel);

       // Aggiungi gli elementi al timePanel con spaziatura
       timePanel.add(playerNameContainer);
       timePanel.add(Box.createVerticalStrut(15)); 
       timePanel.add(timeLabelPanel);
       timePanel.add(Box.createVerticalStrut(8)); 
       timePanel.add(countdownLabelPanel);

       // Pannello per i pulsanti (Salva/Esci)
       JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
       buttonPanel.setOpaque(false);

       // Crea pulsanti con dimensioni fisse
       JButton saveButton = createButton("SALVA", ColorText.NAVY.getSwingColor(), 18f);
       saveButton.setPreferredSize(new Dimension(160, 45));

       JButton exitButton = createButton("ESCI", ColorText.CRIMSON.getSwingColor(), 18f);
       exitButton.setPreferredSize(new Dimension(160, 45));

       saveButton.addActionListener(e -> saveGame());
       exitButton.addActionListener(e -> confirmExit());

       buttonPanel.add(saveButton);
       buttonPanel.add(exitButton);

       // Assembla i componenti
       infoPanel.add(timePanel, BorderLayout.NORTH);
       infoPanel.add(buttonPanel, BorderLayout.CENTER);

       // Aggiungi i pannelli principali
       panel.add(outputImagePane, BorderLayout.CENTER);
       panel.add(infoPanel, BorderLayout.SOUTH);

       return panel;
   }

    /**
     * Implementazione del metodo astratto getWindowTitle() della superclasse UI_Abstract.
     * Restituisce il titolo della finestra.
     *
     * @return Stringa contenente il titolo della finestra
     */
    @Override
    protected String getWindowTitle() {
        return "PoggioAdventure - Gioco";
    }

    /**
     * Crea un bordo personalizzato per le sezioni dell'interfaccia.
     *
     * @param title Titolo da visualizzare nel bordo
     * @return Border configurato con stile coerente
     */
    private Border createSectionBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 2),
            title,
            0, 0,
            UI_Config.getItalicFont().deriveFont(14f),
            UI_Config.TEXT_COLOR
        );
    }

    /**
     * Ferma tutti i timer attivi prima della chiusura della finestra.
     */
    private void shutdown() {
        if (gameTimer != null) gameTimer.stop();
        if (countdownTimer != null) countdownTimer.stop();
    }

    /**
     * Override del metodo dispose() per garantire che tutti i timer vengano fermati
     * prima della chiusura della finestra.
     */
    @Override
    public void dispose() {
        shutdown();
        super.dispose();
    }

    /**
     * Crea un pulsante con stile personalizzato, inclusi effetti hover.
     *
     * @param text Testo da visualizzare sul pulsante
     * @param bgColor Colore di sfondo del pulsante
     * @param fontSize Dimensione del font del testo
     * @return JButton configurato
     */
    private JButton createButton(String text, Color bgColor, float fontSize) {
        JButton button = new JButton(text);
        button.setFont(UI_Config.getNormalFont().deriveFont(fontSize));
        button.setForeground(UI_Config.TEXT_COLOR);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        // Effetto hover: cambia colore quando il mouse passa sopra il pulsante
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }

    /**
     * Configura i timer per il tempo di gioco e il countdown.
     */
    private void setupTimers() {
        startTime = System.currentTimeMillis();  // Memorizza il momento di inizio del gioco
        gameTimer = new Timer(1000, e -> updateGameTime());  // Timer che aggiorna ogni secondo
        gameTimer.start();
    }

    /**
     * Aggiorna il timer principale del gioco ogni secondo.
     */
    private void updateGameTime() {
        long elapsed = System.currentTimeMillis() - startTime;  // Calcola il tempo trascorso
        timeLabel.setText("Tempo: " + new SimpleDateFormat("HH:mm:ss").format(new Date(elapsed)));
    }

    /**
     * Avvia il countdown per il livello corrente.
     *
     * @param seconds Durata iniziale del countdown in secondi
     */
    public void startCountdown(int seconds) {
        remainingSeconds = seconds;
        countdownTimer = new Timer(1000, e -> {
            if (remainingSeconds <= 0) {
                countdownTimer.stop();
                //gameOutputArea.append("\nTempo scaduto!");
            } else {
                remainingSeconds--;
                countdownLabel.setText("Countdown: " + 
                    String.format("%02d:%02d", TimeUnit.SECONDS.toMinutes(remainingSeconds), remainingSeconds % 60));
            }
        });
        countdownTimer.start();
    }

    /**
     * Gestisce l'invio dei comandi da parte dell'utente.
     *
     * @param e Evento di azione generato dal pulsante "INVIA"
     */
    private void processCommand() {
        String command = commandInput.getText().trim();
        if (!command.isEmpty()) {
            gameEngine.getOutput().write("\nComando Inserito: ", ColorText.WHITE);
            gameEngine.getOutput().writeln(command, ColorText.ORANGE);
            gameEngine.processCommand(command);
            commandInput.setText("");
        }
    }

    /**
     * Aggiorna l'immagine della stanza corrente.
     *
     * @param imagePath Percorso del file immagine da visualizzare
     */
    public void updateRoomImage(String imagePath) {
        imagePanel.removeAll();  // Rimuove l'immagine precedente
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));  // Carica l'immagine
            Image scaled = image.getScaledInstance(
                imagePanel.getWidth(),
                imagePanel.getHeight(),
                Image.SCALE_SMOOTH  // Ridimensiona l'immagine in modo fluido
            );
            imagePanel.add(new JLabel(new ImageIcon(scaled)), BorderLayout.CENTER);
        } catch (IOException ex) {
            imagePanel.add(new JLabel("Immagine non trovata", JLabel.CENTER));  // Messaggio di errore
        }
        imagePanel.revalidate();
        imagePanel.repaint();
    }

    /**
     * Mostra un dialogo di conferma per il salvataggio del gioco.
     */
    private void saveGame() {
        new Thread(() -> {
            try {
                SaveGame.saveGame(gameEngine, gameEngine.getOutput());

                // Aggiornamento thread-safe della GUI
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                        UI_Game.this, 
                        "Salvataggio completato!", 
                        "Successo", 
                        JOptionPane.INFORMATION_MESSAGE
                    );
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                        UI_Game.this, 
                        "Errore durante il salvataggio:\n" + ex.getMessage(), 
                        "Errore", 
                        JOptionPane.ERROR_MESSAGE
                    );
                });
            }
        }).start();
    }

    /**
     * Mostra un dialogo di conferma per l'uscita dal gioco. Riaprendo la schermata iniziale.
     */
    private void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Vuoi salvare prima di uscire?",
            "Conferma uscita",
            JOptionPane.YES_NO_CANCEL_OPTION);

        if (choice == JOptionPane.YES_OPTION) saveGame();
        if (choice != JOptionPane.CANCEL_OPTION) dispose();
        if (choice == JOptionPane.CANCEL_OPTION) return;
        
        // outputScrollPane.set
        UI_Init init = new UI_Init();
        init.setVisible(true);
    }

    /**
     * Metodo main per avviare l'applicazione.
     *
     * @param args Argomenti della riga di comando (non utilizzati)
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            UI_Game gameScreen = new UI_Game();
            gameScreen.setVisible(true);
            gameScreen.startCountdown(300);  // Avvia un countdown di 5 minuti
            gameScreen.updateRoomImage("./resources/img/room1.jpg");  // Carica l'immagine iniziale
        });
    }

    public JTextPane getGameOutputArea() {
        return gameOutputArea;
    }

    public JTextField getCommandInput() {
        return commandInput;
    }

    public void setGameOutputArea(JTextPane gameOutputArea) {
        this.gameOutputArea = gameOutputArea;
    }

    public void setCommandInput(JTextField commandInput) {
        this.commandInput = commandInput;
    }

    private void setPlayerNameLabel(String text) {
        this.playerNameLabel.setText(text);
    }

    public void setGameEngine(Engine gameEngine) {
        setPlayerNameLabel(gameEngine.getPlayerName());
        this.gameEngine = gameEngine;
    }
}