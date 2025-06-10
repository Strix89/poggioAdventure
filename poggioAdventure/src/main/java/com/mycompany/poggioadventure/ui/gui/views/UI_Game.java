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
 * Interfaccia principale gioco GUI con gestione completa gameplay e UI.
 * 
 * <p>Implementa finestra principale con layout responsive e componenti integrati:
 * game output area, command input, room images, timers sincronizzati e controls.
 * Gestisce Engine integration e real-time updates con thread-safe operations.
 * 
 * <p><b>Componenti principali:</b>
 * <ul>
 *   <li>Game output area con scrolling automatico</li>
 *   <li>Command input field con processing real-time</li>
 *   <li>Room image display con scaling automatico</li>
 *   <li>Dual timer system: game time + level countdown</li>
 *   <li>Save/Exit controls con confirmation dialogs</li>
 * </ul>
 * 
 * <p><b>Pattern implementati:</b>
 * <ul>
 *   <li>MVC: UI separata da game logic via Engine</li>
 *   <li>Observer: real-time updates via Swing Timer</li>
 *   <li>Template Method: estende UI_Abstract structure</li>
 *   <li>Factory: Engine creation via EngineFactory</li>
 * </ul>
 */
public class UI_Game extends UI_Abstract {
    
    // Layout constants per responsive design
    private static final int WINDOW_WIDTH = 1100;
    private static final int WINDOW_HEIGHT = 550;
    private static final float IMAGE_PANEL_RATIO = 0.35f;
    private static final float IMAGE_HEIGHT_RATIO = 0.45f;

    // Core UI components
    private JTextPane gameOutputArea;
    private JScrollPane outputScrollPane;
    private JTextField commandInput;
    private JButton sendButton;
    private JPanel imagePanel;
    private JLabel timeLabel;
    private JLabel countdownLabel;
    private JPanel outputImagePane;
    private JLabel playerNameLabel;

    // Game engine integration e timer management
    private Timer mainTimer;
    private Engine gameEngine;

    /**
     * Costruttore principale con player name initialization.
     * 
     * @param playerName Nome giocatore per UI display e Engine setup
     */
    public UI_Game(String playerName) {
        super();
        initEngineAfterUI(playerName);
    }
    
    /** Costruttore default per testing con placeholder name */
    public UI_Game() {
        super();
        initEngineAfterUI("NONE");
    }

    /**
     * Inizializza componenti UI come richiesto da UI_Abstract.
     * Configura layout responsive, timer management e window behavior.
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
     * Inizializza Engine dopo UI setup con dependency injection pattern.
     * Configura handlers e establishes initial room state.
     * 
     * @param playerName Nome per Engine initialization
     */
    private void initEngineAfterUI(String playerName) {
        OutputHandler guiOutput = new GUIOutputHandler(gameOutputArea);
        ErrorHandler errHandler = new GUIErrorHandler();
        InputHandler inHandler = new GUIInputHandler(commandInput);
        
        try {
            this.gameEngine = EngineFactory.createNewGame(
                playerName, guiOutput, inHandler, errHandler, new LoggerInput(errHandler)
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

    /**
     * Factory method per left panel con game output e command input.
     * Configura scrollable text area e input controls con styling.
     */
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        gameOutputArea = new JTextPane();
        gameOutputArea.setEditable(false);
        gameOutputArea.setFont(UI_Config.getNormalFont().deriveFont(18f));
        gameOutputArea.setForeground(Color.WHITE);
        gameOutputArea.setBackground(new Color(60, 60, 60));
        
        outputScrollPane = new JScrollPane(gameOutputArea);
        outputScrollPane.setBorder(createSectionBorder("Log Gioco"));

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
     * Factory method per right panel con room image, player info e controls.
     * Implementa layout responsive con image scaling e timer displays.
     */
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        // Image panel con responsive sizing
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

        // Info panel con timer displays
        JPanel infoPanel = new JPanel(new BorderLayout(10, 20));
        infoPanel.setOpaque(false);

        JPanel timePanel = new JPanel();
        timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS));
        timePanel.setOpaque(false);
        
        timeLabel = new JLabel("Tempo: 00:00:00");
        countdownLabel = new JLabel("Countdown: --:--");
        playerNameLabel = new JLabel("NONE"); 
        
        // Styling configuration
        playerNameLabel.setFont(UI_Config.getBoldFont().deriveFont(20f));
        timeLabel.setFont(UI_Config.getNormalFont().deriveFont(16f));
        countdownLabel.setFont(UI_Config.getNormalFont().deriveFont(16f));
        playerNameLabel.setForeground(ColorText.WHITE.getSwingColor());
        timeLabel.setForeground(ColorText.WHITE.getSwingColor());
        countdownLabel.setForeground(ColorText.RED.getSwingColor());

        // Layout assembly con spacing
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

        // Control buttons con event handlers
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

        infoPanel.add(timePanel, BorderLayout.NORTH);
        infoPanel.add(buttonPanel, BorderLayout.CENTER);

        panel.add(outputImagePane, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.SOUTH);

        return panel;
    }

    /** Template method implementation per window title */
    @Override
    protected String getWindowTitle() {
        return "PoggioAdventure - Gioco";
    }

    /**
     * Factory method per section borders con title styling.
     * 
     * @param title Titolo per border display
     * @return Border configurato con styling consistente
     */
    private Border createSectionBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 2),
            title, 0, 0,
            UI_Config.getItalicFont().deriveFont(14f),
            UI_Config.TEXT_COLOR
        );
    }

    /**
     * Factory method per styled buttons con hover effects.
     * 
     * @param text Button text
     * @param bgColor Background color
     * @param fontSize Font size per text
     * @return JButton con styling e hover behavior
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

    /**
     * Configura unified timer per sincronizzazione all time displays.
     * Evita drift between multiple timers con single update source.
     */
    private void setupTimers() {
        if (mainTimer != null) {
            mainTimer.stop();
        }
        
        mainTimer = new Timer(1000, e -> updateAllTimeDisplays());
        mainTimer.start();
    }

    /**
     * Update method per synchronized time displays.
     * Aggiorna game time e level countdown atomicamente per consistency.
     */
    private void updateAllTimeDisplays() {
        if (gameEngine != null) {
            timeLabel.setText("Tempo: " + gameEngine.getFormattedGameTime());
            
            if (gameEngine.getGameStateManager() != null) {
                long remainingSecondsCurrentLevel = gameEngine.getGameStateManager().getCurrentLevelRemainingTimeSeconds();
                
                if (remainingSecondsCurrentLevel >= 0) {
                    String countdownText = String.format("%02d:%02d",
                            TimeUnit.SECONDS.toMinutes(remainingSecondsCurrentLevel),
                            remainingSecondsCurrentLevel % 60);
                    countdownLabel.setText("Countdown: " + countdownText);
                } else {
                    countdownLabel.setText("Countdown: 00:00");
                }
            } else {
                countdownLabel.setText("Countdown: --:--");
            }
        } else {
            timeLabel.setText("Tempo: 00:00:00");
            countdownLabel.setText("Countdown: --:--");
        }
    }

    /**
     * Command processor con Engine delegation e UI updates.
     * Gestisce input validation, command execution e room state refresh.
     */
    private void processCommand() {
        String command = commandInput.getText().trim();
        if (!command.isEmpty()) {
            gameEngine.getOutput().write("\n[NEON_ORANGE]-[/]Comando Inserito[NEON_ORANGE]-[/]: ", ColorText.WHITE);
            gameEngine.getOutput().writeln(command, ColorText.NEON_ORANGE);
            gameEngine.processCommand(command);

            Room currentRoom = gameEngine.getGame().getCurrentRoom();
            if (currentRoom != null && currentRoom.getImagePath() != null) {
                updateRoomImage(currentRoom.getImagePath());
            }

            commandInput.setText("");
        }
    }

    /**
     * Updates room image display con error handling e scaling.
     * Utilizza ResourceLoader per consistent image loading.
     * 
     * @param imagePath Path relativo per room image
     */
    public void updateRoomImage(String imagePath) {
        imagePanel.removeAll();
        try {
            BufferedImage image = ResourceLoader.loadImage(imagePath);
            Image scaled = image.getScaledInstance(
                imagePanel.getWidth(), imagePanel.getHeight(), Image.SCALE_SMOOTH
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

    /**
     * Async save operation con user feedback via dialogs.
     * Utilizza background thread per non bloccare EDT.
     */
    private void saveGame() {
        new Thread(() -> {
            try {
                this.gameEngine.saveGame();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                        UI_Game.this, "Salvataggio completato!", 
                        "Successo", JOptionPane.INFORMATION_MESSAGE
                    );
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                        UI_Game.this, 
                        "Errore durante il salvataggio:\n" + ex.getMessage(), 
                        "Errore", JOptionPane.ERROR_MESSAGE
                    );
                });
            }
        }).start();
    }

    /**
     * Exit confirmation con optional save before quit.
     * Gestisce navigation flow verso main menu.
     */
    private void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Vuoi salvare prima di uscire?", "Conferma uscita",
            JOptionPane.YES_NO_CANCEL_OPTION);

        if (choice == JOptionPane.YES_OPTION) saveGame();
        if (choice != JOptionPane.CANCEL_OPTION) {
            dispose();
            new UI_Init().setVisible(true);
        }
    }

    /** Cleanup method per timer shutdown e resource release */
    private void shutdown() {
        if (mainTimer != null) {
            mainTimer.stop();
        }
    }

    /** Override dispose per guaranteed cleanup */
    @Override
    public void dispose() {
        shutdown();
        super.dispose();
    }

    // Accessors per component access
    public JTextPane getGameOutputArea() { return gameOutputArea; }
    public JTextField getCommandInput() { return commandInput; }
    public Engine getGameEngine() { return gameEngine; }

    /**
     * Engine setter con timer resync e UI refresh.
     * Aggiorna player name display e room image per consistency.
     * 
     * @param gameEngine Nuovo Engine instance
     */
    public void setGameEngine(Engine gameEngine) {
        this.playerNameLabel.setText(gameEngine.getPlayerName());
        this.gameEngine = gameEngine;

        if (mainTimer != null) {
            mainTimer.stop();
        }
        
        setupTimers();

        Room currentRoom = gameEngine.getGame().getCurrentRoom();
        if (currentRoom != null && currentRoom.getImagePath() != null) {
            updateRoomImage(currentRoom.getImagePath());
        }
    }

    /** Main per testing e development */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            UI_Game gameScreen = new UI_Game();
            gameScreen.setVisible(true);
            gameScreen.updateRoomImage("./resources/img/Hall.jpg");
        });
    }
}