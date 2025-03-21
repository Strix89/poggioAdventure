package com.mycompany.poggioadventure.ui;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.border.Border;

/**
 * Classe principale per l'interfaccia utente del gioco PoggioAdventure
 */
public class UI_Init extends JFrame {
    
    private JButton newGameButton;
    private JButton loadGameButton;
    private JButton rankingButton;
    private JButton exitButton;
    private JLabel shieldCenter;

    public UI_Init() {
        initComponents();
    }

    private void initComponents() {
        configureMainFrame();
        addShieldComponent();
        addMainContentComponents();
        setupEventListeners();
        pack();
    }

    private void configureMainFrame() {
        setTitle("PoggioAdventure");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(calculateWindowSize());
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UI_Config.BACKGROUND_COLOR);
        getContentPane().setLayout(new BorderLayout());
    }

    private Dimension calculateWindowSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new Dimension(
            (int)(screenSize.width * UI_Config.WINDOW_WIDTH_RATIO),
            (int)(screenSize.height * UI_Config.WINDOW_HEIGHT_RATIO)
        );
    }

    private void addShieldComponent() {
        ImageIcon shieldIcon = loadShieldImage();
        shieldCenter = new JLabel(scaleImage(shieldIcon, UI_Config.SHIELD_SIZE_RATIO));

        JPanel shieldPanel = new JPanel(new BorderLayout());
        shieldPanel.setOpaque(false);
        shieldPanel.setBorder(BorderFactory.createEmptyBorder(
            (int)(getPreferredSize().height * UI_Config.TOP_MARGIN_RATIO / 100f),
            0, 0, 0
        ));
        shieldPanel.add(shieldCenter, BorderLayout.CENTER);
        add(shieldPanel, BorderLayout.NORTH);
    }

    private ImageIcon loadShieldImage() {
        BufferedImage image = UI_Config.getShieldImage();
        if(image == null) {
            JOptionPane.showMessageDialog(this, 
                "Errore critico: immagine dello scudo non caricata!");
            System.exit(1);
        }
        return new ImageIcon(image);
    }

    private ImageIcon scaleImage(ImageIcon icon, float ratio) {
        Dimension windowSize = getPreferredSize();
        Image scaled = icon.getImage().getScaledInstance(
            (int)(windowSize.width * ratio),
            (int)(windowSize.height * ratio),
            Image.SCALE_SMOOTH
        );
        return new ImageIcon(scaled);
    }

    private void addMainContentComponents() {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = UI_Config.BUTTON_INSETS;

        contentPanel.add(createTitleLabel(), gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        newGameButton = createButton("NUOVA PARTITA");
        loadGameButton = createButton("CARICA PARTITA");
        rankingButton = createButton("CLASSIFICA");
        exitButton = createButton("ESCI");

        contentPanel.add(newGameButton, gbc);
        contentPanel.add(loadGameButton, gbc);
        contentPanel.add(rankingButton, gbc);

        gbc.insets = UI_Config.EXIT_BUTTON_INSETS;
        contentPanel.add(exitButton, gbc);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JLabel createTitleLabel() {
        JLabel label = new JLabel("PoggioAdventure");
        label.setFont(scaleFont(UI_Config.getItalicFont(), UI_Config.TITLE_FONT_RATIO));
        label.setForeground(UI_Config.TEXT_COLOR);
        return label;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(scaleFont(UI_Config.getNormalFont(), UI_Config.BUTTON_FONT_RATIO));
        button.setForeground(UI_Config.TEXT_COLOR);
        button.setBackground(UI_Config.BUTTON_BASE_COLOR);
        button.setFocusPainted(false);
        button.setBorder(createButtonBorder());
        button.setPreferredSize(new Dimension(
            (int)(getPreferredSize().width * UI_Config.BUTTON_WIDTH_RATIO),
            (int)(getPreferredSize().height * UI_Config.BUTTON_HEIGHT_RATIO)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(UI_Config.BUTTON_HOVER_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(UI_Config.BUTTON_BASE_COLOR);
            }
        });
        
        return button;
    }

    private Font scaleFont(Font baseFont, float ratio) {
        return baseFont.deriveFont(getPreferredSize().height * ratio);
    }

    private void setupEventListeners() {
        exitButton.addActionListener(e -> System.exit(0));
        rankingButton.addActionListener(e -> showRanking());
        newGameButton.addActionListener(e -> showNewGame());
        loadGameButton.addActionListener(e -> showLoadGame());
    }

    private void showRanking() {
        JFrame ranking = new UI_Rank();
        ranking.setLocationRelativeTo(this);
        ranking.setVisible(true);
    }

    private void showNewGame() {
        JFrame newGame = new UI_NewGame();
        newGame.setLocationRelativeTo(this);
        newGame.setVisible(true);
    }

    private void showLoadGame() {
        JFrame loadGame = new UI_LoadGame();
        loadGame.setLocationRelativeTo(this);
        loadGame.setVisible(true);
    }
    
    public Border createButtonBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        );
    }

    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();
            EventQueue.invokeLater(() -> {
                UI_Init mainWindow = new UI_Init();
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