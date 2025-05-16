package com.mycompany.poggioadventure.ui.gui.views;

import com.mycompany.poggioadventure.ui.ErrorHandler;
import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.core.Engine;
import com.mycompany.poggioadventure.core.utils.EngineFactory;
import com.mycompany.poggioadventure.model.Room;
import com.mycompany.poggioadventure.ui.InputHandler;
import com.mycompany.poggioadventure.ui.OutputHandler;
import com.mycompany.poggioadventure.ui.UI_Abstract;
import com.mycompany.poggioadventure.ui.gui.GUIOutputHandler;
import com.mycompany.poggioadventure.ui.gui.GUIErrorHandler;
import com.mycompany.poggioadventure.ui.gui.GUIInputHandler;
import com.mycompany.poggioadventure.persistence.LoggerInput;
import com.mycompany.poggioadventure.persistence.ResourceLoader;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Finestra principale del gioco che gestisce l'interfaccia utente completa.
 * 
 * <p>Responsabilità principali:
 * <ul>
 *   <li>Visualizzazione del log di gioco con scrolling</li>
 *   <li>Gestione input comandi del giocatore</li>
 *   <li>Visualizzazione immagini delle stanze</li>
 *   <li>Gestione timer di gioco e countdown</li>
 *   <li>Funzioni di salvataggio / Esci</li>
 * </ul>
 * 
 * <p>Estende UI_Abstract per ereditare la struttura base dell'interfaccia.
 * 
 * @author Strix89
 */
public class UI_Game extends UI_Abstract {
    
    // ============== COSTANTI LAYOUT ==============
    private static final int WINDOW_WIDTH = 1100;
    private static final int WINDOW_HEIGHT = 550;
    private static final float IMAGE_PANEL_RATIO = 0.35f;
    private static final float IMAGE_HEIGHT_RATIO = 0.45f;

    // ============== COMPONENTI UI ==============
    private JTextPane gameOutputArea;
    private JScrollPane outputScrollPane;
    private JTextField commandInput;
    private JButton sendButton;
    private JPanel imagePanel;
    private JLabel timeLabel;
    private JLabel countdownLabel;
    private JPanel outputImagePane;
    private JLabel playerNameLabel;

    // ============== GESTIONE TEMPO ==============
    private Timer countdownTimer;
    private Timer gameTimer;
    private int remainingSeconds;
    private Engine gameEngine;

    // ============== COSTRUTTORI ==============
    
    /**
     * Crea una nuova finestra di gioco con il nome del giocatore specificato.
     * @param playerName Nome del giocatore da visualizzare nell'interfaccia
     */
    public UI_Game(String playerName) {
        super();
        initEngineAfterUI(playerName);
    }
    
    /**
     * Costruttore di default per testing o casi speciali.
     * Utilizza "NONE" come nome giocatore.
     */
    public UI_Game() {
        super();
        initEngineAfterUI("NONE");
    }

    // ============== INIZIALIZZAZIONE ==============
    
    /**
     * Inizializza i componenti dell'interfaccia come richiesto da UI_Abstract.
     * Configura:
     * - Layout principale
     * - Timer di gioco
     * - Pannelli laterali
     * - Comportamento finestra
     */
    @Override
    protected void initComponents() {
        setupTimers();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        getContentPane().setBackground(UI_Config.BACKGROUND_COLOR);
        
        setLayout(new BorderLayout(15, 15));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        add(createLeftPanel(), BorderLayout.CENTER);
        add(createRightPanel(), BorderLayout.EAST);
        
        pack();
    }
    
    /**
     * Inizializza il motore di gioco dopo il setup dell'interfaccia.
     * @param playerName Nome del giocatore per la partita
     */
    private void initEngineAfterUI(String playerName) {
        OutputHandler guiOutput = new GUIOutputHandler(gameOutputArea);
        ErrorHandler errHandler = new GUIErrorHandler();
        InputHandler inHandler = new GUIInputHandler(commandInput);
        
        try {
            this.gameEngine = EngineFactory.createNewGame(
                playerName, 
                guiOutput, 
                inHandler, 
                errHandler, 
                new LoggerInput(errHandler)
            );
        } catch (Exception ex) {
            errHandler.handleFatalError("Errore inizializzazione gioco", ex);
        }
        playerNameLabel.setText(playerName);
        commandInput.addActionListener(e -> processCommand());
        sendButton.addActionListener(e -> processCommand());
        Room startingRoom = gameEngine.getGame().getCurrentRoom();
        if (startingRoom != null && startingRoom.getImagePath() != null) {
            updateRoomImage(startingRoom.getImagePath());
        }
    }

    // ============== CREAZIONE PANNELLI ==============
    
    /**
     * Crea il pannello sinistro contenente:
     * - Area di testo per il log di gioco
     * - Campo di input per i comandi
     * - Pulsante di invio
     */
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        // Configurazione area di output
        gameOutputArea = new JTextPane();
        gameOutputArea.setEditable(false);
        gameOutputArea.setFont(UI_Config.getNormalFont().deriveFont(18f));
        gameOutputArea.setForeground(Color.WHITE);
        gameOutputArea.setBackground(new Color(60, 60, 60));
        
        outputScrollPane = new JScrollPane(gameOutputArea);
        outputScrollPane.setBorder(createSectionBorder("Log Gioco"));

        // Configurazione area di input
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
     * Crea il pannello destro contenente:
     * - Immagine della stanza corrente
     * - Informazioni giocatore e timer
     * - Pulsanti di controllo
     */
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        // Pannello immagine
        imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(new Color(60, 60, 60));
        
        outputImagePane = new JPanel(new BorderLayout());
        outputImagePane.setBackground(UI_Config.BACKGROUND_COLOR);
        outputImagePane.add(imagePanel);
        outputImagePane.setBorder(createSectionBorder("Stanza Corrente"));
        outputImagePane.setPreferredSize(new Dimension(
            (int)(WINDOW_WIDTH * IMAGE_PANEL_RATIO),
            (int)(WINDOW_HEIGHT * IMAGE_HEIGHT_RATIO)
        ));

        // Pannello informazioni
        JPanel infoPanel = new JPanel(new BorderLayout(10, 20));
        infoPanel.setOpaque(false);

        // Configurazione etichette
        JPanel timePanel = new JPanel();
        timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS));
        timePanel.setOpaque(false);
        
        timeLabel = new JLabel("Tempo: 00:00:00");
        countdownLabel = new JLabel("Countdown: --:--");
        playerNameLabel = new JLabel("NONE"); 
        
        // Stilizzazione etichette
        playerNameLabel.setFont(UI_Config.getBoldFont().deriveFont(20f));
        timeLabel.setFont(UI_Config.getNormalFont().deriveFont(16f));
        countdownLabel.setFont(UI_Config.getNormalFont().deriveFont(16f));
        playerNameLabel.setForeground(ColorText.WHITE.getSwingColor());
        timeLabel.setForeground(ColorText.WHITE.getSwingColor());
        countdownLabel.setForeground(ColorText.RED.getSwingColor());

        // Layout etichette
        JPanel playerNameContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        playerNameContainer.setOpaque(false);
        playerNameContainer.add(playerNameLabel);

        JPanel timeLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timeLabelPanel.setOpaque(false);
        timeLabelPanel.add(timeLabel);

        JPanel countdownLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        countdownLabelPanel.setOpaque(false);
        countdownLabelPanel.add(countdownLabel);

        timePanel.add(playerNameContainer);
        timePanel.add(Box.createVerticalStrut(15)); 
        timePanel.add(timeLabelPanel);
        timePanel.add(Box.createVerticalStrut(8)); 
        timePanel.add(countdownLabelPanel);

        // Pulsanti di controllo
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);

        JButton saveButton = createButton("SALVA", ColorText.NAVY.getSwingColor(), 18f);
        saveButton.setPreferredSize(new Dimension(160, 45));

        JButton exitButton = createButton("ESCI", ColorText.CRIMSON.getSwingColor(), 18f);
        exitButton.setPreferredSize(new Dimension(160, 45));

        saveButton.addActionListener(e -> saveGame());
        exitButton.addActionListener(e -> confirmExit());

        buttonPanel.add(saveButton);
        buttonPanel.add(exitButton);

        // Assemblaggio finale
        infoPanel.add(timePanel, BorderLayout.NORTH);
        infoPanel.add(buttonPanel, BorderLayout.CENTER);

        panel.add(outputImagePane, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ============== METODI UTILITY ==============
    
    @Override
    protected String getWindowTitle() {
        return "PoggioAdventure - Gioco";
    }

    /**
     * Crea un bordo personalizzato per le sezioni UI.
     * @param title Titolo da mostrare nel bordo
     * @return Border configurato
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
     * Crea un pulsante con stile standard e effetti hover.
     */
    private JButton createButton(String text, Color bgColor, float fontSize) {
        JButton button = new JButton(text);
        button.setFont(UI_Config.getNormalFont().deriveFont(fontSize));
        button.setForeground(UI_Config.TEXT_COLOR);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

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

    // ============== GESTIONE GAMEPLAY ==============
    
    private void setupTimers() {
        gameTimer = new Timer(1000, e -> updateGameTime());
        gameTimer.start();
    }

    private void updateGameTime() {
        timeLabel.setText("Tempo: " + gameEngine.getFormattedGameTime());
    }

    /**
     * Avvia il countdown per il livello corrente.
     * @param seconds Durata iniziale in secondi
     */
    public void startCountdown(int seconds) {
        remainingSeconds = seconds;
        countdownTimer = new Timer(1000, e -> {
            if (remainingSeconds <= 0) {
                countdownTimer.stop();
            } else {
                remainingSeconds--;
                countdownLabel.setText("Countdown: " + 
                    String.format("%02d:%02d", 
                        TimeUnit.SECONDS.toMinutes(remainingSeconds), 
                        remainingSeconds % 60));
            }
        });
        countdownTimer.start();
    }

    /**
     * Elabora il comando inserito dal giocatore.
     */
    private void processCommand() {
        String command = commandInput.getText().trim();
        if (!command.isEmpty()) {
            gameEngine.getOutput().write("\nComando Inserito: ", ColorText.WHITE);
            gameEngine.getOutput().writeln(command, ColorText.GOLD);
            gameEngine.processCommand(command);

            Room currentRoom = gameEngine.getGame().getCurrentRoom();
            if (currentRoom != null && currentRoom.getImagePath() != null) {
                updateRoomImage(currentRoom.getImagePath());
            }

            commandInput.setText("");
        }
    }

    /**
     * Aggiorna l'immagine della stanza visualizzata.
     * @param imagePath Percorso del file immagine
     */
    public void updateRoomImage(String imagePath) {
        imagePanel.removeAll();
        try {
            // Usa ResourceLoader invece di ImageIO.read diretto
            BufferedImage image = ResourceLoader.loadImage(imagePath);
            Image scaled = image.getScaledInstance(
                imagePanel.getWidth(),
                imagePanel.getHeight(),
                Image.SCALE_SMOOTH
            );
            imagePanel.add(new JLabel(new ImageIcon(scaled)), BorderLayout.CENTER);
        } catch (IOException | IllegalArgumentException ex) {
            JLabel errorLabel = new JLabel("Immagine non disponibile");
            errorLabel.setForeground(UI_Config.TEXT_COLOR);
            errorLabel.setHorizontalAlignment(JLabel.CENTER);
            imagePanel.add(errorLabel, BorderLayout.CENTER);
        }
        imagePanel.revalidate();
        imagePanel.repaint();
    }

    // ============== GESTIONE FILE ==============
    
    /**
     * Salva lo stato corrente del gioco in un file.
     */
    private void saveGame() {
        new Thread(() -> {
            try {
                this.gameEngine.saveGame();
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
     * Mostra dialogo di conferma per l'uscita dal gioco.
     */
    private void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Vuoi salvare prima di uscire?",
            "Conferma uscita",
            JOptionPane.YES_NO_CANCEL_OPTION);

        if (choice == JOptionPane.YES_OPTION) saveGame();
        if (choice != JOptionPane.CANCEL_OPTION) {
            dispose();
            new UI_Init().setVisible(true);
        }
    }

    // ============== METODI DI CHIUSURA ==============
    
    private void shutdown() {
        if (countdownTimer != null) countdownTimer.stop();
        if (gameTimer != null) gameTimer.stop();
    }

    @Override
    public void dispose() {
        shutdown();
        super.dispose();
    }

    // ============== GETTER/SETTER ==============
    
    public JTextPane getGameOutputArea() {
        return gameOutputArea;
    }

    public JTextField getCommandInput() {
        return commandInput;
    }

    public Engine getGameEngine() {
        return gameEngine;
    }

    public void setGameEngine(Engine gameEngine) {
        this.playerNameLabel.setText(gameEngine.getPlayerName());
        this.gameEngine = gameEngine;
        Room currentRoom = gameEngine.getGame().getCurrentRoom();
            if (currentRoom != null && currentRoom.getImagePath() != null) {
                updateRoomImage(currentRoom.getImagePath());
        }
    }

    // ============== MAIN PER TEST ==============
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            UI_Game gameScreen = new UI_Game();
            gameScreen.setVisible(true);
            gameScreen.startCountdown(300);
            gameScreen.updateRoomImage("./resources/img/room1.jpg");
        });
    }
}