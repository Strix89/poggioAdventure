package com.mycompany.poggioadventure.ui;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;

/**
 * Classe principale per l'inizializzazione dell'interfaccia utente del gioco PoggioAdventure.
 * Estende JFrame per creare la finestra principale dell'applicazione.
 */
public class UI_Init extends JFrame {
    // Componenti dell'interfaccia utente
    private JLabel titleLabel;
    private JButton newGameButton;
    private JButton loadGameButton;
    private JButton rankingButton;
    private JButton exitButton;
    private JLabel shieldCenter;
    private String nameFont = "Crismon Pro"; // Font utilizzato per il testo

    /**
     * Costruttore della classe UI_Init.
     * Inizializza i componenti dell'interfaccia utente.
     */
    public UI_Init() {
        initComponents();
    }

    /**
     * Metodo per inizializzare i componenti dell'interfaccia utente.
     */
    private void initComponents() {
        setTitle("PoggioAdventure");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Ottieni le dimensioni dello schermo
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) (screenSize.getWidth() * 0.35); // 35% della larghezza dello schermo
        int screenHeight = (int) (screenSize.getHeight() * 0.85); // 85% dell'altezza dello schermo
        setSize(screenWidth, screenHeight);
        setLocationRelativeTo(null); // Centra la finestra
        this.setResizable(false);

        // Imposta il colore di sfondo e il layout del contenitore principale
        getContentPane().setBackground(new Color(45, 45, 45));
        getContentPane().setLayout(new BorderLayout());

        // Scudi decorativi
        ImageIcon shieldIcon = new ImageIcon("./resources/img/scudopoggiolevante.png");
        Image image = shieldIcon.getImage().getScaledInstance(
            (int) (screenWidth * 0.25), // 25% della larghezza della finestra
            (int) (screenHeight * 0.25), // 25% dell'altezza della finestra
            Image.SCALE_SMOOTH
        );
        shieldCenter = new JLabel(new ImageIcon(image));

        // Pannello per lo scudo con margine superiore
        JPanel shieldPanel = new JPanel(new BorderLayout());
        shieldPanel.setOpaque(false);
        shieldPanel.setBorder(BorderFactory.createEmptyBorder(
            (int) (screenHeight * 0.02), // 2% dell'altezza della finestra come margine superiore
            0, 0, 0
        ));
        shieldPanel.add(shieldCenter, BorderLayout.CENTER);
        add(shieldPanel, BorderLayout.NORTH);

        // Pannello contenitore principale
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(true);
        contentPanel.setBackground(new Color(45, 45, 45));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 20, 15, 20);
        gbc.ipadx = 20;
        gbc.ipady = 10;

        // Titolo
        titleLabel = new JLabel("PoggioAdventure");
        titleLabel.setFont(new Font(nameFont, Font.ITALIC, (int) (screenHeight * 0.07))); // 7% dell'altezza della finestra
        titleLabel.setForeground(Color.WHITE);
        contentPanel.add(titleLabel, gbc);

        // Pulsanti
        gbc.fill = GridBagConstraints.HORIZONTAL;

        newGameButton = createStyledButton("NUOVA PARTITA", screenWidth, screenHeight);
        contentPanel.add(newGameButton, gbc);

        loadGameButton = createStyledButton("CARICA PARTITA", screenWidth, screenHeight);
        contentPanel.add(loadGameButton, gbc);

        rankingButton = createStyledButton("CLASSIFICA", screenWidth, screenHeight);
        contentPanel.add(rankingButton, gbc);

        gbc.insets = new Insets(30, 20, 0, 20);
        exitButton = createStyledButton("ESCI", screenWidth, screenHeight);
        contentPanel.add(exitButton, gbc);

        add(contentPanel, BorderLayout.CENTER);
        setupEventListeners();
    }

    /**
     * Metodo per creare un pulsante con uno stile predefinito.
     *
     * @param text Il testo da visualizzare sul pulsante
     * @param screenWidth La larghezza dello schermo
     * @param screenHeight L'altezza dello schermo
     * @return Un JButton con lo stile applicato
     */
    private JButton createStyledButton(String text, int screenWidth, int screenHeight) {
        JButton button = new JButton(text);
        button.setFont(new Font(nameFont, Font.PLAIN, (int) (screenHeight * 0.03))); // 3% dell'altezza della finestra
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(100, 100, 100));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 150, 150), 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        button.setPreferredSize(new Dimension(
            (int) (screenWidth * 0.3), // 30% della larghezza della finestra
            (int) (screenHeight * 0.07) // 7% dell'altezza della finestra
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Aggiunge un effetto hover al pulsante
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(130, 130, 130));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 100, 100));
            }
        });
        return button;
    }

    /**
     * Metodo per configurare gli eventi dei pulsanti.
     */
    private void setupEventListeners() {
        // Chiude l'applicazione quando viene premuto il pulsante "ESCI"
        exitButton.addActionListener(e -> System.exit(0));

        // Apre la finestra della classifica quando viene premuto il pulsante "CLASSIFICA"
        rankingButton.addActionListener(e -> {
            UI_Ranking ranking = new UI_Ranking();
            ranking.setLocationRelativeTo(this);
            ranking.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            ranking.setVisible(true);
        });

        // Apre la finestra per caricare una nuova partita quando viene premuto il pulsante "NUOVA PARTITA"
        newGameButton.addActionListener(e -> {
            UI_LoadGame loadGame = new UI_LoadGame();
            loadGame.setLocationRelativeTo(this);
            loadGame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            loadGame.setVisible(true);
        });
    }

    /**
     * Metodo main per avviare l'applicazione.
     *
     * @param args Argomenti della riga di comando (non utilizzati)
     */
    public static void main(String[] args) {
        FlatLightLaf.setup(); // Imposta il look and feel FlatLaf
        EventQueue.invokeLater(() -> {
            new UI_Init().setVisible(true); // Crea e mostra la finestra principale
        });
    }
}