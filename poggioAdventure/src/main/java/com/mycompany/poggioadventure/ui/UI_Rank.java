package com.mycompany.poggioadventure.ui;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class UI_Rank extends JFrame {

    private JList<String> rankingList;
    private JLabel titleLabel;

    // Dummy data per la classifica
    private final String[] RANKING_DATA = {
        "1. Giocatore1 - Punti: 9500 - Lvl 10",
        "2. Giocatore2 - Punti: 8700 - Lvl 9",
        "3. Giocatore3 - Punti: 8200 - Lvl 8",
        "4. Giocatore4 - Punti: 7800 - Lvl 8",
        "5. Giocatore5 - Punti: 7200 - Lvl 7"
    };

    public UI_Rank() {
        initComponents();
    }

    private void initComponents() {
        configureFrame();
        createComponents();
        setupLayout();
        pack();
    }

    private void configureFrame() {
        setTitle("Classifica - PoggioAdventure");
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
            (int)(screenSize.width * UI_Config.WINDOW_WIDTH_RATIO),
            (int)(screenSize.height * UI_Config.WINDOW_HEIGHT_RATIO)
        );
    }

    private void createComponents() {
        // Titolo
        titleLabel = new JLabel("CLASSIFICA");
        titleLabel.setFont(UI_Config.getBoldFont().deriveFont(
            getPreferredSize().height * UI_Config.TITLE_FONT_RATIO));
        titleLabel.setForeground(UI_Config.TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Lista classifica
        rankingList = new JList<>(RANKING_DATA);
        rankingList.setFont(UI_Config.getNormalFont().deriveFont(
            getPreferredSize().height * UI_Config.BUTTON_FONT_RATIO));
        rankingList.setForeground(UI_Config.TEXT_COLOR);
        rankingList.setBackground(UI_Config.BUTTON_BASE_COLOR);
        rankingList.setSelectionBackground(UI_Config.BUTTON_HOVER_COLOR);
        rankingList.setBorder(createListBorder());
        rankingList.setEnabled(false);

        // Personalizzazione del renderer per allineare il testo e aggiungere spazio
        rankingList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                         boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setHorizontalAlignment(SwingConstants.CENTER); // Allinea il testo al centro
                label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Aggiunge spazio sopra e sotto
                return label;
            }
        });
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(
            UI_Config.BUTTON_INSETS.top,
            UI_Config.BUTTON_INSETS.left,
            UI_Config.BUTTON_INSETS.bottom,
            UI_Config.BUTTON_INSETS.right
        ));

        // Aggiunta titolo
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // ScrollPane per la lista
        JScrollPane scrollPane = new JScrollPane(rankingList);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UI_Config.BACKGROUND_COLOR);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private Border createListBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(
                UI_Config.BUTTON_INSETS.top,
                UI_Config.BUTTON_INSETS.left,
                UI_Config.BUTTON_INSETS.bottom,
                UI_Config.BUTTON_INSETS.right
            )
        );
    }

    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();
            EventQueue.invokeLater(() -> {
                UI_Rank ranking = new UI_Rank();
                ranking.setVisible(true);
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                "Errore nell'apertura della classifica: " + ex.getMessage(),
                "Errore",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}