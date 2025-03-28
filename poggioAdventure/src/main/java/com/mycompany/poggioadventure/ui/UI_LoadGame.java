package com.mycompany.poggioadventure.ui;

import com.formdev.flatlaf.FlatLightLaf;
import di.uniba.map.b.adventure.ErrorHandler;
import di.uniba.map.b.adventure.SaveGame;
import di.uniba.map.b.adventure.Utils;
import di.uniba.map.b.adventure.impl.GUIErrorHandler;
import di.uniba.map.b.adventure.impl.GUIInputHandler;
import di.uniba.map.b.adventure.impl.GUIOutputHandler;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Classe che rappresenta l'interfaccia utente per il caricamento di una partita salvata.
 * Mostra una lista di salvataggi disponibili e permette all'utente di selezionarne uno
 * per caricarlo o tornare indietro. Estende la classe astratta UI_Abstract per ereditare
 * la struttura di base dell'interfaccia grafica.
 */
public class UI_LoadGame extends UI_Abstract {

    // Componenti dell'interfaccia utente
    private JList<String> saveList;  // Lista dei salvataggi disponibili
    private JButton loadButton;      // Pulsante per caricare il salvataggio selezionato
    private JButton backButton;      // Pulsante per tornare indietro
    private List<String> saveSlots;

    /**
     * Costruttore della classe. Chiama il costruttore della superclasse UI_Abstract
     * per inizializzare l'interfaccia grafica.
     */
    public UI_LoadGame() {
        super();
    }

    /**
     * Implementazione del metodo astratto initComponents() della superclasse UI_Abstract.
     * Inizializza tutti i componenti dell'interfaccia utente, inclusi la lista dei salvataggi
     * e i pulsanti di caricamento e indietro.
     */
    @Override
    protected void initComponents() {
        configureFrame();          // Configura la finestra principale
        createComponents();       // Crea i componenti dell'interfaccia
        setupLayout();            // Configura il layout della finestra
        setupEventListeners();    // Imposta gli eventi dei pulsanti
        pack();                   // Ridimensiona la finestra per adattarsi ai componenti
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
            (int)(screenSize.width * UI_Config.WINDOW_WIDTH_RATIO * 0.9),  // Larghezza ridotta del 90%
            (int)(screenSize.height * UI_Config.WINDOW_HEIGHT_RATIO * 0.7)  // Altezza ridotta del 70%
        );
    }

    /**
     * Crea i componenti dell'interfaccia utente, inclusi la lista dei salvataggi
     * e i pulsanti di caricamento e indietro.
     */
    private void createComponents() {
        saveSlots = SaveGame.getSaveList();
        saveList = new JList<>(saveSlots.toArray(String[]::new));
        saveList.setFont(UI_Config.getNormalFont().deriveFont(18f));  // Scala il font
        saveList.setForeground(UI_Config.TEXT_COLOR);  // Colore del testo
        saveList.setBackground(UI_Config.BUTTON_BASE_COLOR);  // Colore di sfondo
        saveList.setSelectionBackground(UI_Config.BUTTON_HOVER_COLOR);  // Colore di selezione
        saveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);  // Selezione singola
        saveList.setBorder(createListBorder());  // Bordo personalizzato
        
        saveList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    handleDelete();
                }
            }
        });

        // Pulsante Carica
        loadButton = new JButton("CARICA");
        styleButton(loadButton);  // Applica lo stile al pulsante
        

        // Pulsante Indietro
        backButton = new JButton("INDIETRO");
        styleButton(backButton);  // Applica lo stile al pulsante
    }
    
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

    /**
     * Applica uno stile personalizzato a un pulsante.
     *
     * @param button Pulsante da personalizzare
     */
    private void styleButton(JButton button) {
        button.setFont(UI_Config.getBoldFont().deriveFont(20f));  // Scala il font
        button.setForeground(UI_Config.TEXT_COLOR);  // Colore del testo
        button.setBackground(UI_Config.BUTTON_BASE_COLOR);  // Colore di sfondo
        button.setBorder(createButtonBorder());  // Bordo personalizzato
        button.setFocusPainted(false);  // Disabilita l'effetto di focus
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));  // Cambia il cursore al passaggio del mouse
        addButtonHoverEffect(button);  // Aggiunge l'effetto hover
    }

    /**
     * Configura il layout della finestra, posizionando i componenti nei pannelli appropriati.
     */
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout());  // Pannello principale
        mainPanel.setOpaque(false);  // Rende il pannello trasparente
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));  // Aggiunge un padding interno

        // Titolo
        JLabel titleLabel = new JLabel("SELEZIONA SALVATAGGIO");
        titleLabel.setFont(UI_Config.getBoldFont().deriveFont(
            getPreferredSize().height * UI_Config.TITLE_FONT_RATIO * 0.6f));  // Scala il font
        titleLabel.setForeground(UI_Config.TEXT_COLOR);  // Colore del testo
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);  // Allinea il testo al centro
        mainPanel.add(titleLabel, BorderLayout.NORTH);  // Aggiunge il titolo al pannello

        // Lista con scroll
        JScrollPane scrollPane = new JScrollPane(saveList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());  // Rimuove il bordo predefinito
        scrollPane.getViewport().setBackground(UI_Config.BACKGROUND_COLOR);  // Colore di sfondo
        mainPanel.add(scrollPane, BorderLayout.CENTER);  // Aggiunge la lista al pannello

        // Pannello pulsanti
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));  // Layout a griglia per i pulsanti
        buttonPanel.setOpaque(false);  // Rende il pannello trasparente
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));  // Aggiunge un padding superiore

        // Imposta le dimensioni preferite dei pulsanti
        loadButton.setPreferredSize(new Dimension(
            (int)(getPreferredSize().width * UI_Config.BUTTON_WIDTH_RATIO),
            (int)(getPreferredSize().height * UI_Config.BUTTON_HEIGHT_RATIO)
        ));
        backButton.setPreferredSize(loadButton.getPreferredSize());

        buttonPanel.add(loadButton);  // Aggiunge il pulsante Carica
        buttonPanel.add(backButton);  // Aggiunge il pulsante Indietro

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);  // Aggiunge il pannello pulsanti al pannello principale

        add(mainPanel, BorderLayout.CENTER);  // Aggiunge il pannello principale alla finestra
    }

    /**
     * Crea un bordo personalizzato per la lista dei salvataggi.
     *
     * @return Border Bordo configurato
     */
    private Border createListBorder() {
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
     * - "CARICA" avvia il caricamento del salvataggio selezionato.
     * - "INDIETRO" chiude la finestra.
     */
    private void setupEventListeners() {
        loadButton.addActionListener(e -> handleLoad());  // Gestisce il caricamento del salvataggio
        backButton.addActionListener(e -> {
            (new UI_Init()).setVisible(true); 
            dispose();
        });  // Chiude la finestra
    }

    /**
     * Metodo main di esempio per testare l'interfaccia.Da rimuovere in produzione o utilizzare solo per scopi dimostrativi.
     * @param args
     */
    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();  // Configura il tema FlatLaf light
            EventQueue.invokeLater(() -> {
                UI_LoadGame selector = new UI_LoadGame();
                selector.setVisible(true);
            });
        } catch (Exception ex) {
            new GUIErrorHandler().handleFatalError("Errore critico nell'avvio dell'interfaccia:\n", ex);  // Mostra un messaggio di errore
            Utils.exitApplication(Utils.EXIT_CODE_CRITICAL);  // Termina l'applicazione
        }
    }

    /**
     * Gestisce il caricamento del salvataggio selezionato.
     * Se nessun salvataggio è selezionato, mostra un messaggio di errore.
     */
    private void handleLoad() {
        int selectedIndex = saveList.getSelectedIndex();  // Ottieni l'indice del salvataggio selezionato
        if (selectedIndex == -1) {
            new GUIErrorHandler().handleRecoverableError("Seleziona un salvataggio per caricarlo.");
            return;
        }

        String saveName = saveSlots.get(selectedIndex);
        ErrorHandler errorha = new GUIErrorHandler();
        UI_Game guiGame = new UI_Game();
        new GUIOutputHandler(guiGame.getGameOutputArea()).clear();
        
        // Carica il salvataggio
        SaveGame.loadSave(saveName,
            engine -> { // onSuccess
                EventQueue.invokeLater(() -> {
                    guiGame.setGameEngine(engine);
                    guiGame.setVisible(true);
                    dispose();
                });
            },
            error -> { // onError
                new GUIErrorHandler().handleRecoverableError(error);  // Mostra errore se il caricamento fallisce
            }, errorha, new GUIInputHandler(guiGame.getCommandInput()), new GUIOutputHandler(guiGame.getGameOutputArea())
        );
    }

    /**
     * Implementazione del metodo astratto getWindowTitle() della superclasse UI_Abstract.
     * Restituisce il titolo della finestra.
     *
     * @return Stringa contenente il titolo della finestra
     */
    @Override
    protected String getWindowTitle() {
        return "PoggioAdventure - Carica partita";
    }
}