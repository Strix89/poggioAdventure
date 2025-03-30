package com.mycompany.poggioadventure.ui.gui.views;

import com.formdev.flatlaf.FlatLightLaf;
import com.mycompany.poggioadventure.ui.UI_Abstract;
import di.uniba.map.b.adventure.ErrorHandler;
import com.mycompany.poggioadventure.persistence.SaveGame;
import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.ui.gui.GUIErrorHandler;
import com.mycompany.poggioadventure.ui.gui.GUIInputHandler;
import com.mycompany.poggioadventure.ui.gui.GUIOutputHandler;
import com.mycompany.poggioadventure.persistence.LoggerInput;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Interfaccia per il caricamento delle partite salvate in PoggioAdventure.
 * 
 * <p>Responsabilità principali:
 * <ul>
 *   <li>Visualizza lista dei salvataggi disponibili</li>
 *   <li>Permette selezione e caricamento di un salvataggio</li>
 *   <li>Gestisce eliminazione dei salvataggi</li>
 *   <li>Fornisce opzione per tornare al menu principale</li>
 * </ul>
 * 
 * <p>Caratteristiche:
 * <ul>
 *   <li>Design responsive basato sulle dimensioni dello schermo</li>
 *   <li>Gestione errori durante il caricamento</li>
 *   <li>Interfaccia intuitiva con feedback visivo</li>
 * </ul>
 *
 * @author Strix89
 */
public class UI_LoadGame extends UI_Abstract {

    // ============== COMPONENTI UI ==============
    private JList<String> saveList;      // Lista visualizzazione salvataggi
    private JButton loadButton;         // Pulsante per caricare
    private JButton backButton;         // Pulsante per tornare indietro
    private List<String> saveSlots;     // Lista dei nomi dei salvataggi

    // ============== COSTRUTTORE ==============
    
    /**
     * Inizializza la finestra di caricamento.
     * Configura l'interfaccia utente e carica la lista dei salvataggi.
     */
    public UI_LoadGame() {
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
     * - 90% della larghezza standard
     * - 70% dell'altezza standard
     */
    private Dimension calculateWindowSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new Dimension(
            (int)(screenSize.width * UI_Config.WINDOW_WIDTH_RATIO * 0.9),
            (int)(screenSize.height * UI_Config.WINDOW_HEIGHT_RATIO * 0.7)
        );
    }

    // ============== CREAZIONE COMPONENTI ==============
    
    /**
     * Crea e configura i componenti principali:
     * - Lista dei salvataggi
     * - Pulsanti di azione
     */
    private void createComponents() {
        saveSlots = SaveGame.getSaveList();
        saveList = new JList<>(saveSlots.toArray(String[]::new));
        saveList.setFont(UI_Config.getNormalFont().deriveFont(18f));
        saveList.setForeground(UI_Config.TEXT_COLOR);
        saveList.setBackground(UI_Config.BUTTON_BASE_COLOR);
        saveList.setSelectionBackground(UI_Config.BUTTON_HOVER_COLOR);
        saveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        saveList.setBorder(createListBorder());
        
        // Gestione tasto cancella
        saveList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    handleDelete();
                }
            }
        });

        loadButton = new JButton("CARICA");
        styleButton(loadButton);

        backButton = new JButton("INDIETRO");
        styleButton(backButton);
    }

    // ============== GESTIONE ELIMINAZIONE ==============
    
    /**
     * Gestisce la richiesta di eliminazione salvataggio.
     * Mostra dialogo di conferma ed esegue l'eliminazione.
     */
    private void handleDelete() {
        int selectedIndex = saveList.getSelectedIndex();
        if (selectedIndex == -1) return;

        String saveName = saveSlots.get(selectedIndex);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Eliminare il salvataggio '" + saveName + "'?",
            "Conferma eliminazione",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (SaveGame.deleteSave(saveName)) {
                saveSlots = SaveGame.getSaveList();
                saveList.setListData(saveSlots.toArray(new String[0]));
                JOptionPane.showMessageDialog(this, "Salvataggio eliminato!");
            } else {
                new GUIErrorHandler().handleRecoverableError("Errore eliminazione");
            }
        }
    }

    // ============== STILE COMPONENTI ==============
    
    /**
     * Applica stile standard ai pulsanti.
     * @param button Pulsante da stilizzare
     */
    private void styleButton(JButton button) {
        button.setFont(UI_Config.getBoldFont().deriveFont(20f));
        button.setForeground(UI_Config.TEXT_COLOR);
        button.setBackground(UI_Config.BUTTON_BASE_COLOR);
        button.setBorder(createButtonBorder());
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addButtonHoverEffect(button);
    }

    // ============== LAYOUT ==============
    
    /**
     * Configura il layout della finestra:
     * - Titolo in alto
     * - Lista salvataggi al centro
     * - Pulsanti in basso
     */
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Titolo
        JLabel titleLabel = new JLabel("SELEZIONA SALVATAGGIO");
        titleLabel.setFont(UI_Config.getBoldFont().deriveFont(
            getPreferredSize().height * UI_Config.TITLE_FONT_RATIO * 0.6f));
        titleLabel.setForeground(UI_Config.TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Lista con scroll
        JScrollPane scrollPane = new JScrollPane(saveList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UI_Config.BACKGROUND_COLOR);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Pannello pulsanti
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        loadButton.setPreferredSize(new Dimension(
            (int)(getPreferredSize().width * UI_Config.BUTTON_WIDTH_RATIO),
            (int)(getPreferredSize().height * UI_Config.BUTTON_HEIGHT_RATIO)
        ));
        backButton.setPreferredSize(loadButton.getPreferredSize());

        buttonPanel.add(loadButton);
        buttonPanel.add(backButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    // ============== BORDI ==============
    
    /**
     * Crea bordo per la lista salvataggi.
     */
    private Border createListBorder() {
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

    // ============== EFFETTI UI ==============
    
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
     * - Caricamento salvataggio
     * - Ritorno al menu
     */
    private void setupEventListeners() {
        loadButton.addActionListener(e -> handleLoad());
        backButton.addActionListener(e -> {
            (new UI_Init()).setVisible(true); 
            dispose();
        });
    }

    // ============== GESTIONE CARICAMENTO ==============
    
    /**
     * Gestisce il caricamento del salvataggio selezionato.
     * Avvia la partita o mostra errori.
     */
    private void handleLoad() {
        int selectedIndex = saveList.getSelectedIndex();
        if (selectedIndex == -1) {
            new GUIErrorHandler().handleRecoverableError("Seleziona un salvataggio per caricarlo.");
            return;
        }

        String saveName = saveSlots.get(selectedIndex);
        ErrorHandler errorha = new GUIErrorHandler();
        UI_Game guiGame = new UI_Game();
        LoggerInput.deleteLogFile(guiGame.getGameEngine().getLogger().getFileName());
        new GUIOutputHandler(guiGame.getGameOutputArea()).clear();
        
        SaveGame.loadSave(saveName,
            engine -> { // onSuccess
                EventQueue.invokeLater(() -> {
                    guiGame.setGameEngine(engine);
                    guiGame.setVisible(true);
                    dispose();
                });
            },
            error -> { // onError
                new GUIErrorHandler().handleRecoverableError(error);
            }, errorha, new GUIInputHandler(guiGame.getCommandInput()), 
            new GUIOutputHandler(guiGame.getGameOutputArea())
        );
    }

    // ============== METODI OVERRIDE ==============
    
    @Override
    protected String getWindowTitle() {
        return "PoggioAdventure - Carica partita";
    }

    // ============== MAIN PER TEST ==============
    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();
            EventQueue.invokeLater(() -> {
                UI_LoadGame selector = new UI_LoadGame();
                selector.setVisible(true);
            });
        } catch (Exception ex) {
            new GUIErrorHandler().handleFatalError("Errore avvio interfaccia:", ex);
            Utils.exitApplication(Utils.EXIT_CODE_CRITICAL);
        }
    }
}