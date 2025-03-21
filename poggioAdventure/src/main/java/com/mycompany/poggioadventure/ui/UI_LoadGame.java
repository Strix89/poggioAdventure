package com.mycompany.poggioadventure.ui;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UI_LoadGame extends JFrame {

    private JList<String> saveList;
    private JButton loadButton;
    private JButton backButton;
    private static String[] SAVE_SLOTS = {
            "[AUTO] Salvataggio 1 - Lvl 3 - 10:30 01/06",
            "[MANUALE] Salvataggio 3 - Lvl 5 - 18:45 31/05",
        };

    public UI_LoadGame() {
        initComponents();
    }

    private void initComponents() {
        configureFrame();
        createComponents();
        setupLayout();
        setupEventListeners();
        pack();
    }

    private void configureFrame() {
        setTitle("Carica Partita - PoggioAdventure");
        setPreferredSize(calculateWindowSize());
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UI_Config.BACKGROUND_COLOR);
        getContentPane().setLayout(new BorderLayout());
    }

    private Dimension calculateWindowSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new Dimension(
            (int)(screenSize.width * UI_Config.WINDOW_WIDTH_RATIO * 0.9),
            (int)(screenSize.height * UI_Config.WINDOW_HEIGHT_RATIO * 0.7)
        );
    }

    private void createComponents() {
        // Lista salvataggi
        saveList = new JList<>(SAVE_SLOTS);
        saveList.setFont(UI_Config.getNormalFont().deriveFont(
            getPreferredSize().height * UI_Config.BUTTON_FONT_RATIO * 0.8f));
        saveList.setForeground(UI_Config.TEXT_COLOR);
        saveList.setBackground(UI_Config.BUTTON_BASE_COLOR);
        saveList.setSelectionBackground(UI_Config.BUTTON_HOVER_COLOR);
        saveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        saveList.setBorder(createListBorder());
        
        // Pulsante Carica
        loadButton = new JButton("CARICA");
        styleButton(loadButton);
        
        // Pulsante Indietro
        backButton = new JButton("INDIETRO");
        styleButton(backButton);
    }

    private void styleButton(JButton button) {
        button.setFont(UI_Config.getBoldFont().deriveFont(
            getPreferredSize().height * UI_Config.BUTTON_FONT_RATIO));
        button.setForeground(UI_Config.TEXT_COLOR);
        button.setBackground(UI_Config.BUTTON_BASE_COLOR);
        button.setBorder(createButtonBorder());
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addButtonHoverEffect(button);
    }

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

    private Border createListBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        );
    }

    private Border createButtonBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(5, 25, 5, 25)
        );
    }

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

    private void setupEventListeners() {
        loadButton.addActionListener(e -> handleLoad());
        backButton.addActionListener(e -> dispose());
    }
    
    public static void main(String[] args) {
        try {
            // Configura il look and feel FlatLaf
            FlatLightLaf.setup();
            
            EventQueue.invokeLater(() -> {
                UI_LoadGame selector = new UI_LoadGame();
                selector.setVisible(true);
                
                // Caricamento dati fittizio (simulazione salvataggi)
                selector.SAVE_SLOTS = loadSaveData(); // Metodo dummy
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                "Errore critico nell'avvio dell'interfaccia:\n" + ex.getMessage(),
                "Errore di sistema",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private static String[] loadSaveData() {
        // Metodo dummy per il testing
        return new String[] {
            "[AUTO] Salvataggio 1 - Lvl 3 - 10:30 01/06",
            "Slot 2 - Nuovo gioco",
            "[MANUALE] Salvataggio 3 - Lvl 5 - 18:45 31/05",
            "Slot 4 - Nuovo gioco"
        };
    }

    private void handleLoad() {
        int selectedIndex = saveList.getSelectedIndex();
        if(selectedIndex == -1) {
            JOptionPane.showMessageDialog(this,
                "Seleziona un salvataggio!",
                "Errore",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String selectedSave = SAVE_SLOTS[selectedIndex];
        // Logica per caricare il salvataggio
        dispose();
        //new UI_Game().setVisible(true);
    }
}