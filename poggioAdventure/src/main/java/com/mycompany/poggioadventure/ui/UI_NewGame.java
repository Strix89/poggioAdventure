package com.mycompany.poggioadventure.ui;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UI_NewGame extends JFrame {

    private JTextField nameField;
    private JButton startButton;
    private JLabel titleLabel;

    public UI_NewGame() {
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
        setTitle("Nuova Partita - PoggioAdventure");
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
            (int)(screenSize.width * UI_Config.WINDOW_WIDTH_RATIO * 0.8),
            (int)(screenSize.height * UI_Config.WINDOW_HEIGHT_RATIO * 0.6)
        );
    }

    private void createComponents() {
        // Titolo
        titleLabel = new JLabel("INSERISCI IL TUO NOME");
        titleLabel.setFont(UI_Config.getBoldFont().deriveFont(
            getPreferredSize().height * UI_Config.TITLE_FONT_RATIO * 0.7f));
        titleLabel.setForeground(UI_Config.TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Campo di testo
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

        // Campo testo
        gbc.gridy = 1;
        gbc.ipady = 20;
        nameField.setPreferredSize(new Dimension(
            (int)(getPreferredSize().width * 0.7),
            (int)(getPreferredSize().height * 0.1)
        ));
        mainPanel.add(nameField, gbc);

        // Pulsante
        gbc.gridy = 2;
        gbc.ipady = 0;
        gbc.fill = GridBagConstraints.NONE;
        startButton.setPreferredSize(new Dimension(
            (int)(getPreferredSize().width * UI_Config.BUTTON_WIDTH_RATIO),
            (int)(getPreferredSize().height * UI_Config.BUTTON_HEIGHT_RATIO)
        ));
        mainPanel.add(startButton, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    private Border createTextFieldBorder() {
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
        startButton.addActionListener(e -> handleStartGame());
    }

    private void handleStartGame() {
        String playerName = nameField.getText().trim();
        if(playerName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Inserisci un nome valido!",
                "Errore",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Logica per avviare il gioco
        dispose();
        //new UI_Game().setVisible(true);
    }
    
    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();
            EventQueue.invokeLater(() -> {
                UI_NewGame mainWindow = new UI_NewGame();
                mainWindow.setVisible(true);
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                "Errore critico nell'inizializzazione dell'interfaccia: " + ex.getMessage(),
                "Errore", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}